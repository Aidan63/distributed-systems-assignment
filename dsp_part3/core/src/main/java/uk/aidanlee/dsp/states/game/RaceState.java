package uk.aidanlee.dsp.states.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import uk.aidanlee.dsp.common.components.AABBComponent;
import uk.aidanlee.dsp.common.components.InputComponent;
import uk.aidanlee.dsp.common.components.PolygonComponent;
import uk.aidanlee.dsp.common.data.circuit.Circuit;
import uk.aidanlee.dsp.common.data.circuit.TreeTileWall;
import uk.aidanlee.dsp.common.net.NetChan;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.net.Snapshot;
import uk.aidanlee.dsp.common.net.commands.CmdClientInput;
import uk.aidanlee.dsp.common.net.commands.CmdServerState;
import uk.aidanlee.dsp.common.net.commands.CmdSnapshot;
import uk.aidanlee.dsp.common.net.commands.Command;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.common.structural.ec.Entity;
import uk.aidanlee.dsp.common.structural.ec.Visual;
import uk.aidanlee.dsp.common.utils.MathsUtil;
import uk.aidanlee.dsp.data.ChatLog;
import uk.aidanlee.dsp.data.race.Craft;
import uk.aidanlee.dsp.data.race.View;
import uk.aidanlee.dsp.data.states.LobbyData;
import uk.aidanlee.jDiffer.Collision;
import uk.aidanlee.jDiffer.data.ShapeCollision;
import uk.aidanlee.jDiffer.shapes.Polygon;

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
    private SpriteBatch batch;

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

        circuit = new Circuit("/media/aidan/BFE6-24C6/dsp/dsp_part2/assets/tracks/track.p2");
        craft   = new Craft(players, circuit.getSpawn(), ourID);
        view    = new View();

        batch = new SpriteBatch();
    }

    @Override
    public void onLeave(Object _leaveWith) {
        super.onLeave(_leaveWith);
    }

    @Override
    public void onUpdate(LinkedList<Command> _cmds) {

        // Send our currently pressed inputs to the server.
        netChan.addCommand(new CmdClientInput(ourID, (InputComponent) craft.getRemotePlayers()[ourID].get("input")));

        readCommands(_cmds);

        simulatePlayer();

        resolveWallCollisions();

        resolveCraftCollisions();

        interpolate();
    }

    @Override
    public void onRender() {
        Gdx.gl.glClearColor(0.47f, 0.56f, 0.61f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        view.getViewport().apply();
        Matrix4 projCamera = view.getCamera().combined;

        batch.setProjectionMatrix(projCamera);
        batch.begin();

        for (Visual v : craft.getRemotePlayers()) {
            if (v == null) continue;

            v.draw(batch);
        }

        batch.end();
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
            }
        }
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
        netChan.addSnapshot(_cmd.master);

        for (int i = 0; i < _cmd.master.getPlayers(); i++) {
            Player player = _cmd.master.getPlayer(i);

            int id = _cmd.master.getID(i);

            players[id].setX(player.getX());
            players[id].setY(player.getY());
            players[id].setRotation(player.getRotation());
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
        //
    }

    /**
     * Performs basic linear interpolation on the player entities.
     * This will smooth out their movements instead of the jittering that occurs if the player were to be snapped to the latest server position.
     */
    private void interpolate() {
        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) continue;

            Visual p   = craft.getRemotePlayers()[i];
            p.pos.x    = MathUtils.lerp(p.pos.x, players[i].getX(), 0.1f);
            p.pos.y    = MathUtils.lerp(p.pos.y, players[i].getY(), 0.1f);
            p.rotation = MathUtils.lerp(p.rotation, players[i].getRotation(), 0.1f);
        }
    }
}
