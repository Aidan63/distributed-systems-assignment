package uk.aidanlee.dsp.states.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Timer;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import uk.aidanlee.dsp.common.components.AABBComponent;
import uk.aidanlee.dsp.common.components.InputComponent;
import uk.aidanlee.dsp.common.components.PolygonComponent;
import uk.aidanlee.dsp.common.components.StatsComponent;
import uk.aidanlee.dsp.common.data.ServerEvent;
import uk.aidanlee.dsp.common.data.circuit.Circuit;
import uk.aidanlee.dsp.common.data.circuit.TreeTileWall;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.net.commands.*;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.common.structural.ec.Entity;
import uk.aidanlee.dsp.common.structural.ec.EntityStateMachine;
import uk.aidanlee.dsp.common.structural.ec.Visual;
import uk.aidanlee.dsp.common.utils.MathsUtil;
import uk.aidanlee.dsp.components.ShadowComponent;
import uk.aidanlee.dsp.components.TrailComponent;
import uk.aidanlee.dsp.data.ChatLog;
import uk.aidanlee.dsp.data.Resources;
import uk.aidanlee.dsp.data.events.EvAddUnreliableCommand;
import uk.aidanlee.dsp.data.race.Craft;
import uk.aidanlee.dsp.data.race.HUD;
import uk.aidanlee.dsp.data.race.InputBuffer;
import uk.aidanlee.dsp.data.race.View;
import uk.aidanlee.dsp.data.states.LobbyData;
import uk.aidanlee.dsp.geometry.MeshBatch;
import uk.aidanlee.dsp.geometry.QuadMesh;
import uk.aidanlee.jDiffer.Collision;
import uk.aidanlee.jDiffer.data.ShapeCollision;
import uk.aidanlee.jDiffer.shapes.Polygon;

import java.util.LinkedList;
import java.util.List;

public class RaceState extends State {

    private EventBus events;

    private Resources resources;

    //

    private ChatLog chatLog;

    private Player[] players;

    private int ourID;

    //

    private Circuit circuit;

    private Craft craft;

    private View view;

    private SpriteBatch spriteBatch;

    private MeshBatch meshBatch;

    private QuadMesh trackMesh;

    private InputBuffer inpBuff;

    private HUD hud;

    public RaceState(String _name, Resources _resources, EventBus _events) {
        super(_name);
        resources = _resources;
        events    = _events;
    }

    @Override
    public void onEnter(Object _enterWith) {
        events.register(this);

        // Read the required data from the game state.
        LobbyData data = (LobbyData) _enterWith;
        chatLog = data.chat;
        players = data.players;
        ourID   = data.ourID;

        circuit = new Circuit(Gdx.files.internal("tracks/track.p2"));
        craft   = new Craft(resources, players, circuit.getSpawn(), circuit.getCheckpoints(), ourID);
        view    = new View();

        inpBuff = new InputBuffer(64);

        // Create the batchers
        spriteBatch = new SpriteBatch();
        meshBatch   = new MeshBatch(resources.trackTexture);
        meshBatch.addShader("track", new ShaderProgram(Gdx.files.internal("shaders/mesh.vert"), Gdx.files.internal("shaders/mesh.frag")));
        meshBatch.addShader("trail", new ShaderProgram(Gdx.files.internal("shaders/trail.vert"), Gdx.files.internal("shaders/trail.frag")));

        // Build the track mesh.
        trackMesh = new QuadMesh(false, circuit.getTiles().length, resources.trackAtlas);

        for (int i = 0; i < circuit.getTiles().length; i++) {
            trackMesh.addQuad(
                    circuit.getTiles()[i].frame,
                    circuit.getTiles()[i].verts[0],
                    circuit.getTiles()[i].verts[1],
                    circuit.getTiles()[i].verts[2],
                    circuit.getTiles()[i].verts[3],
                    Color.WHITE, true, (i % 2 != 0));
        }

        trackMesh.rebuild();

        // Create a new HUD to draw game information
        hud = new HUD(resources, players);
        hud.showCountdown();

        // Countdown starts as soon as we enter the game state.
        playCountdownAudio();
    }

    @Override
    public void onLeave(Object _leaveWith) {
        events.unregister(this);
        craft.destroy();
    }

    @Override
    public void onUpdate() {

        simulatePlayer();

        resolveWallCollisions();

        resolveCraftCollisions();

        interpolate();

        // Send our currently pressed inputs to the server and add it to the input buffer.
        CmdClientInput input = new CmdClientInput(ourID, (InputComponent) craft.getRemotePlayers()[ourID].get("input"));
        events.post(new EvAddUnreliableCommand(input));
        inpBuff.addEntry(input);
    }

    @Override
    public void onRender() {

        // Get the projection matrix from our camera
        view.getViewport().apply();
        Matrix4 projCamera = view.getCamera().combined;

        // Draw the track.
        meshBatch.begin();
        meshBatch.setShader("track", projCamera);
        meshBatch.draw(trackMesh.getMesh(), GL20.GL_TRIANGLES);
        meshBatch.end();

        // Draw the shadows
        spriteBatch.setProjectionMatrix(projCamera);
        spriteBatch.begin();
        for (Visual craft : craft.getRemotePlayers()) {
            if (craft == null) continue;

            ((ShadowComponent) craft.get("shadow")).draw(spriteBatch);
        }
        spriteBatch.end();

        // Draw the trails.
        meshBatch.begin();
        meshBatch.setShader("trail", projCamera);
        for (Visual craft : craft.getRemotePlayers()) {
            if (craft == null) continue;

            TrailComponent trail = (TrailComponent) craft.get("trail");
            meshBatch.draw(trail.mesh, GL20.GL_TRIANGLE_STRIP);
        }

        // Draw the players
        spriteBatch.setProjectionMatrix(projCamera);
        spriteBatch.begin();
        for (Visual v : craft.getRemotePlayers()) {
            if (v == null) continue;
            v.draw(spriteBatch);
        }
        spriteBatch.end();

        // Draw the HUD
        hud.render();
    }

    // Event Functions

    @Subscribe
    public void onClientDisconnected(CmdClientDisconnected _cmd) {
        craft.getRemotePlayers()[_cmd.clientID].destroy();
        craft.getRemotePlayers()[_cmd.clientID] = null;
    }

    /**
     * When the server has changed state. E.g. Switching back from game to lobby.
     * @param _cmd server state command.
     */
    @Subscribe
    public void onServerEvent(CmdServerEvent _cmd) {
        switch (_cmd.state) {
            case ServerEvent.EVENT_RACE_START:
                ((EntityStateMachine) craft.getRemotePlayers()[ourID].get("fsm")).changeState("Active");
                hud.showRace(craft.getRemotePlayers()[ourID]);

                // don't play the last, different toned countdown beep until we get the command from the server.
                playStartAudio();
                break;

            case ServerEvent.EVENT_LOBBY_ENTER:
                machine.set("lobby", new LobbyData(chatLog, players, ourID), null);
                break;
        }
    }

    /**
     * Sets the players position to the new one which came in from the server.
     * @param _cmd snapshot command.
     */
    @Subscribe
    public void onSnapshot(CmdSnapshot _cmd) {

        // Fixes any predictions errors.
        predictionCorrection(_cmd);

        // Smooths the remote players movement.
        for (int i = 0; i < _cmd.snapshot.getPlayerCount(); i++) {
            Player player = _cmd.snapshot.getPlayer(i);
            int    id     = _cmd.snapshot.getID(i);

            if (id == ourID) continue;

            players[id].setShipIndex(player.getShipIndex());

            players[id].getShipColor()[0] = player.getShipColor()[0];
            players[id].getShipColor()[1] = player.getShipColor()[1];
            players[id].getShipColor()[2] = player.getShipColor()[2];

            players[id].getTrailColor()[0] = player.getTrailColor()[0];
            players[id].getTrailColor()[1] = player.getTrailColor()[1];
            players[id].getTrailColor()[2] = player.getTrailColor()[2];

            players[id].setReady(player.isReady());

            players[id].setX(player.getX());
            players[id].setY(player.getY());
            players[id].setRotation(player.getRotation());
        }
    }

    @Subscribe
    public void onPlayerFinished(CmdPlayerFinished _cmd) {
        Entity entity = craft.getRemotePlayers()[_cmd.clientID];
        if (!entity.has("fsm")) return;

        if (((EntityStateMachine) entity.get("fsm")).getState().equals("Active")) {
            ((EntityStateMachine) entity.get("fsm")).changeState("InActive");
            ((StatsComponent) entity.get("stats")).stop();
        }

        hud.showWaiting();
    }

    @Subscribe
    public void onRaceResults(CmdRaceResults _cmd) {
        hud.showResults(_cmd.times);
    }

    // Private Functions

    /**
     *
     */
    private void simulatePlayer() {
        // Update the viewport size
        view.resize();

        // Process all entities
        for (Visual v : craft.getRemotePlayers()) {
            if (v == null) continue;
            v.update(0);
        }

        // Get the player Visual and update the camera.
        Visual player = craft.getRemotePlayers()[ourID];
        view.getCamera().position.x = (float)(player.pos.x + MathsUtil.lengthdirX(200, player.rotation));
        view.getCamera().position.y = (float)(player.pos.y + MathsUtil.lengthdirY(200, player.rotation));
    }

    /**
     *
     */
    private void resolveWallCollisions() {
        // Get the entity and ensure it has the AABB and poly components
        Entity e = craft.getRemotePlayers()[ourID];
        if (!e.has("aabb") || !e.has("polygon")) return;

        // Get the components and query the circuit wall tree for collisions.
        AABBComponent aabb = (AABBComponent) e.get("aabb");
        PolygonComponent poly = (PolygonComponent) e.get("polygon");

        List<TreeTileWall> collisions = new LinkedList<>();
        circuit.getWallTree().getCollisions(aabb.getBox(), collisions);

        // If there are no collisions skip this loop.
        if (collisions.size() == 0) return;

        // Check each AABB collision for a precise collision.
        for (TreeTileWall col : collisions) {
            Polygon transformedPoly = poly.getShape();

            ShapeCollision wallCol = Collision.shapeWithShape(transformedPoly, col.getPolygon(), null);
            if (wallCol == null) continue;

            e.pos.x += wallCol.separationX;
            e.pos.y += wallCol.separationY;
        }
    }

    private void resolveCraftCollisions() {
        // Get the entity and ensure it has the AABB and poly components
        Entity e = craft.getRemotePlayers()[ourID];
        if (!e.has("aabb") || !e.has("polygon")) return;

        // Get the components and query the circuit wall tree for collisions.
        AABBComponent    aabb = (AABBComponent) e.get("aabb");
        PolygonComponent poly = (PolygonComponent) e.get("polygon");

        //for (Entity craft : craft.getRemotePlayers()) {
        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) continue;
            Entity c = craft.getRemotePlayers()[i];

            if (c== null) continue;
            if (c.getName().equals(e.getName())) continue;

            Rectangle otherBox = ((AABBComponent) c.get("aabb")).getBox();
            if (!aabb.getBox().overlaps(otherBox)) continue;

            PolygonComponent otherPoly = (PolygonComponent) c.get("polygon");
            ShapeCollision col = Collision.shapeWithShape(poly.getShape(), otherPoly.getShape(), null);
            while (col != null) {
                e.pos.x += (float)col.unitVectorX;
                e.pos.y += (float)col.unitVectorY;
                c.pos.x -= (float)col.otherUnitVectorX;
                c.pos.y -= (float)col.otherUnitVectorY;

                players[i].setX(c.pos.x);
                players[i].setY(c.pos.y);

                col = Collision.shapeWithShape(poly.getShape(), otherPoly.getShape(), null);
            }
        }
    }

    /**
     * Performs basic linear interpolation on the player entities.
     * This will smooth out their movements instead of the jittering that occurs if the player were to be snapped to the latest server position.
     */
    private void interpolate() {
        for (int i = 0; i < players.length; i++) {

            // Skip non connected IDs and the local player.
            if (players[i] == null) continue;
            if (i == ourID) continue;

            Visual p   = craft.getRemotePlayers()[i];
            p.pos.x    = MathUtils.lerp(p.pos.x, players[i].getX(), 0.25f);
            p.pos.y    = MathUtils.lerp(p.pos.y, players[i].getY(), 0.25f);
            p.rotation = MathUtils.lerp(p.rotation, players[i].getRotation(), 0.25f);
        }
    }

    /**
     * Corrects the client side prediction to agree with the snapshot the server sent.
     * We calculate the time difference between when the packet was sent and arrived and rollback the client simulation to then.
     * We then fix any incorrect predictions by re-advancing time back to the presence.
     * @param _cmd The latest snapshot received by the server.
     */
    private void predictionCorrection(CmdSnapshot _cmd) {

        Player p = _cmd.snapshot.getPlayerByID(ourID);
        Visual v = craft.getRemotePlayers()[ourID];

        v.pos.x = p.getX();
        v.pos.y = p.getY();
        v.rotation = p.getRotation();

        // Re-play any input commands which have been pressed since the servers snapshot time.
        // This re-calculates the client prediction based off what the server says.
        for (InputBuffer.InputRecord cmd : inpBuff.getInputs()) {
            if (cmd.sentTime > _cmd.sentTime) {
                InputComponent ip = (InputComponent) v.get("input");
                ip.accelerate = cmd.input.accel;
                ip.decelerate = cmd.input.decel;
                ip.steerLeft  = cmd.input.steerLeft;
                ip.steerRight = cmd.input.steerRight;
                ip.airBrakeLeft  = cmd.input.abLeft;
                ip.airBrakeRight = cmd.input.abRight;

                // Update just the physics and velocity components.
                v.get("physics").update(0);
                v.get("velocity").update(0);

                // Re-figure out any collisions.
                resolveWallCollisions();
                resolveCraftCollisions();
            }
        }

        // Store the resulting position in the client structure.
        players[ourID].setX(v.pos.x);
        players[ourID].setY(v.pos.y);
        players[ourID].setRotation(v.rotation);
    }

    /**
     * Plays the 3..2..1 countdown beeps at the beginning of the race.
     * Does not play the final beep for the start of the race.
     */
    private void playCountdownAudio() {
        Sound countdown = Gdx.audio.newSound(Gdx.files.internal("audio/COUNTDOWNNORMAL.wav"));

        // Play initial beep.
        countdown.play(0.75f);

        // Task to play the sound.
        // The two tasks are identical but libGDX does not allow the same task to be scheduled twice.
        Timer.Task task1 = new Timer.Task() {
            @Override
            public void run() {
                countdown.play(0.75f);
            }
        };

        Timer.Task task2 = new Timer.Task() {
            @Override
            public void run() {
                countdown.play(0.75f);
            }
        };

        // Play two more at one second intervals.
        Timer.schedule(task1, 1);
        Timer.schedule(task2, 2);
    }

    /**
     * Plays the final countdown beep.
     * Is played when the command is received from the server.
     */
    private void playStartAudio() {
        Sound countdown = Gdx.audio.newSound(Gdx.files.internal("audio/COUNTDOWNGO.wav"));
        countdown.play(0.75f);
    }
}
