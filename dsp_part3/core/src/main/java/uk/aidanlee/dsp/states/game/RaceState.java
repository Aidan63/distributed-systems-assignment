package uk.aidanlee.dsp.states.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import uk.aidanlee.dsp.Client;
import uk.aidanlee.dsp.common.components.AABBComponent;
import uk.aidanlee.dsp.common.components.InputComponent;
import uk.aidanlee.dsp.common.components.PhysicsComponent;
import uk.aidanlee.dsp.common.components.PolygonComponent;
import uk.aidanlee.dsp.common.data.circuit.Circuit;
import uk.aidanlee.dsp.common.data.circuit.TreeTileWall;
import uk.aidanlee.dsp.common.net.NetChan;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.net.PlayerDiff;
import uk.aidanlee.dsp.common.net.Snapshot;
import uk.aidanlee.dsp.common.net.commands.*;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.common.structural.ec.Entity;
import uk.aidanlee.dsp.common.structural.ec.Visual;
import uk.aidanlee.dsp.common.utils.MathsUtil;
import uk.aidanlee.dsp.components.ShadowComponent;
import uk.aidanlee.dsp.components.TrailComponent;
import uk.aidanlee.dsp.data.ChatLog;
import uk.aidanlee.dsp.data.race.Craft;
import uk.aidanlee.dsp.data.race.InputBuffer;
import uk.aidanlee.dsp.data.race.View;
import uk.aidanlee.dsp.data.states.LobbyData;
import uk.aidanlee.dsp.geometry.MeshBatch;
import uk.aidanlee.dsp.geometry.QuadMesh;
import uk.aidanlee.jDiffer.Collision;
import uk.aidanlee.jDiffer.data.ShapeCollision;
import uk.aidanlee.jDiffer.shapes.Polygon;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;

public class RaceState extends State {

    // Data received when entering the state.

    /**
     * Connection to the server.
     */
    private NetChan netChan;

    /**
     * Chat log for this game.
     */
    private ChatLog chatLog;

    /**
     * All of the players in the server.
     */
    private Player[] players;

    /**
     * Client ID of our player.
     */
    private int ourID;

    // Data local to this state.

    /**
     * Holds all data on the circuit.
     * Does not hold any rendering information.
     */
    private Circuit circuit;

    /**
     * Holds and creates the visual entities for each player.
     */
    private Craft craft;

    /**
     * Manages the view for the local player.
     */
    private View view;

    /**
     * Batcher for efficiently drawing all players.
     */
    private SpriteBatch spriteBatch;

    /**
     * Batcher for drawing the track and trail meshes.
     */
    private MeshBatch meshBatch;

    /**
     * Track quad data.
     */
    private QuadMesh trackMesh;

    /**
     *
     */
    private InputBuffer inpBuff;

    public RaceState(String _name) {
        super(_name);
    }

    @Override
    public void onEnter(Object _enterWith) {
        super.onEnter(_enterWith);

        // Read the required data from the game state.
        LobbyData data = (LobbyData) _enterWith;
        netChan = data.chan;
        chatLog = data.chat;
        players = data.players;
        ourID   = data.ourID;

        circuit = new Circuit(Gdx.files.internal("tracks/track.p2"));
        craft   = new Craft(players, circuit.getSpawn(), ourID);
        view    = new View();

        inpBuff = new InputBuffer(64);

        // Create the batchers
        spriteBatch = new SpriteBatch();
        meshBatch   = new MeshBatch(Client.resources.trackTexture);
        meshBatch.addShader("track", new ShaderProgram(Gdx.files.internal("shaders/mesh.vert"), Gdx.files.internal("shaders/mesh.frag")));
        meshBatch.addShader("trail", new ShaderProgram(Gdx.files.internal("shaders/trail.vert"), Gdx.files.internal("shaders/trail.frag")));

        // Build the track mesh.
        trackMesh = new QuadMesh(false, circuit.getTiles().length, Client.resources.trackAtlas);

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
    }

    @Override
    public void onLeave(Object _leaveWith) {
        super.onLeave(_leaveWith);
    }

    @Override
    public void onUpdate(LinkedList<Command> _cmds) {

        readCommands(_cmds);

        simulatePlayer();

        resolveWallCollisions();

        resolveCraftCollisions();

        interpolate();

        // Send our currently pressed inputs to the server and add it to the input buffer.
        CmdClientInput input = new CmdClientInput(ourID, (InputComponent) craft.getRemotePlayers()[ourID].get("input"));
        netChan.addCommand(input);
        inpBuff.addEntry(input);
    }

    @Override
    public void onRender() {
        Gdx.gl.glClearColor(0.47f, 0.56f, 0.61f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
    }

    /**
     * Processes commands which have came in from the server.
     * @param _cmds Commands to read.
     */
    private void readCommands(LinkedList<Command> _cmds) {
        while (_cmds.size() > 0) {
            Command cmd = _cmds.removeFirst();
            switch (cmd.id) {
                case Command.SERVER_STATE:
                    cmdServerState((CmdServerState) cmd);
                    break;

                case Command.SNAPSHOT:
                    cmdSnapshot((CmdSnapshot) cmd);
                    break;

                case Command.CLIENT_DISCONNECTED:
                    cmdClientDisconnected((CmdClientDisconnected) cmd);
            }
        }
    }

    /**
     *
     * @param _cmd
     */
    private void cmdClientDisconnected(CmdClientDisconnected _cmd) {
        System.out.println("Removing entity");
        craft.getRemotePlayers()[_cmd.clientID].destroy();
        craft.getRemotePlayers()[_cmd.clientID] = null;
    }

    /**
     * When the server has changed state. E.g. Switching back from game to lobby.
     * @param _cmd server state command.
     */
    private void cmdServerState(CmdServerState _cmd) {
        // Does nothing fow now...
    }

    /**
     * Sets the players position to the new one which came in from the server.
     * @param _cmd snapshot command.
     */
    private void cmdSnapshot(CmdSnapshot _cmd) {

        // Fixes any predictions errors.
        predictionCorrection(_cmd);

        // Smooths the remote players movement.
        for (PlayerDiff player : _cmd.getDiffedPlayers()) {

            if (player.id == ourID);

            if (player.diffShipIndex) {
                players[player.id].setShipIndex(player.shipIndex);
            }

            if (player.diffShipColR) {
                players[player.id].getShipColor()[0] = player.shipColR;
            }
            if (player.diffShipColG) {
                players[player.id].getShipColor()[1] = player.shipColG;
            }
            if (player.diffShipColB) {
                players[player.id].getShipColor()[2] = player.shipColB;
            }

            if (player.diffTrailColR) {
                players[player.id].getTrailColor()[0] = player.trailColR;
            }
            if (player.diffTrailColG) {
                players[player.id].getTrailColor()[1] = player.trailColG;
            }
            if (player.diffTrailColB) {
                players[player.id].getTrailColor()[2] = player.trailColB;
            }

            players[player.id].setReady(player.ready);

            if (player.diffX) {
                players[player.id].setX(player.x);
            }
            if (player.diffY) {
                players[player.id].setY(player.y);
            }
            if (player.diffRotation) {
                players[player.id].setRotation(player.rotation);
            }
        }
    }

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

            ShapeCollision wallCol = Collision.shapeWithShape(transformedPoly, col.wall, null);
            if (wallCol == null) continue;

            e.pos.x += wallCol.separationX;
            e.pos.y += wallCol.separationY;

            // TODO: bounce collisions
        }
    }

    private void resolveCraftCollisions() {
        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) continue;

            // Get the entity and ensure it has the AABB and poly components
            Entity e = craft.getRemotePlayers()[i];
            if (!e.has("aabb") || !e.has("polygon")) continue;

            // Get the components and query the circuit wall tree for collisions.
            AABBComponent    aabb = (AABBComponent) e.get("aabb");
            PolygonComponent poly = (PolygonComponent) e.get("polygon");

            for (Entity craft : craft.getRemotePlayers()) {
                if (craft == null) continue;
                if (craft.getName().equals(e.getName())) continue;

                Rectangle otherBox = ((AABBComponent) craft.get("aabb")).getBox();
                if (!aabb.getBox().overlaps(otherBox)) continue;

                PolygonComponent otherPoly = (PolygonComponent) craft.get("polygon");
                ShapeCollision col = Collision.shapeWithShape(poly.getShape(), otherPoly.getShape(), null);
                while (col != null) {
                    e.pos.x += (float)col.unitVectorX;
                    e.pos.y += (float)col.unitVectorY;
                    craft.pos.x -= (float)col.otherUnitVectorX;
                    craft.pos.y -= (float)col.otherUnitVectorY;

                    col = Collision.shapeWithShape(poly.getShape(), otherPoly.getShape(), null);
                }
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
            p.pos.x    = MathUtils.lerp(p.pos.x, players[i].getX(), 0.5f);
            p.pos.y    = MathUtils.lerp(p.pos.y, players[i].getY(), 0.5f);
            p.rotation = MathUtils.lerp(p.rotation, players[i].getRotation(), 0.5f);
        }
    }

    /**
     * Corrects the client side prediction to agree with the snapshot the server sent.
     * We calculate the time difference between when the packet was sent and arrived and rollback the client simulation to then.
     * We then fix any incorrect predictions by re-advancing time back to the presence.
     * @param _latest The latest snapshot received by the server.
     */
    private void predictionCorrection(CmdSnapshot _latest) {

        PlayerDiff p = _latest.getDiffedPlayers().stream().filter(pd -> pd.id == ourID).findFirst().get();
        Visual     v = craft.getRemotePlayers()[ourID];

        if (p.diffX) {
            v.pos.x = p.x;
        }
        if (p.diffY) {
            v.pos.y = p.y;
        }
        if (p.diffRotation) {
            v.rotation = p.rotation;
        }

        /*
        // Set our-self to the position where the server says we should be.
        Player p = _latest.master.getPlayerByID(ourID);
        Visual v = craft.getRemotePlayers()[ourID];

        // Set the player to the position of the server snapshot.
        v.pos.x    = p.getX();
        v.pos.y    = p.getY();
        v.rotation = p.getRotation();
        */

        // Re-play any input commands which have been pressed since the servers snapshot time.
        // This re-calculates the client prediction based off what the server says.
        for (InputBuffer.InputRecord cmd : inpBuff.getInputs()) {
            if (cmd.sentTime > _latest.sentTime) {
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
}
