package uk.aidanlee.dsp.server.states;

import com.badlogic.gdx.math.Rectangle;
import uk.aidanlee.dsp.common.components.AABBComponent;
import uk.aidanlee.dsp.common.components.InputComponent;
import uk.aidanlee.dsp.common.components.PolygonComponent;
import uk.aidanlee.dsp.common.components.craft.LapTracker;
import uk.aidanlee.dsp.common.data.circuit.Circuit;
import uk.aidanlee.dsp.common.data.circuit.TreeTileWall;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.net.commands.CmdClientDisconnected;
import uk.aidanlee.dsp.common.net.commands.CmdClientInput;
import uk.aidanlee.dsp.common.net.commands.Command;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.common.structural.ec.Entity;
import uk.aidanlee.dsp.server.data.Craft;
import uk.aidanlee.jDiffer.Collision;
import uk.aidanlee.jDiffer.data.ShapeCollision;
import uk.aidanlee.jDiffer.shapes.Polygon;

import java.util.LinkedList;
import java.util.List;

public class RaceState extends State {

    /**
     * The Array which stores all game information on each conected client.
     */
    private Player[] players;

    /**
     * Holds data about the race circuit.
     */
    private Circuit circuit;

    /**
     * Stores the entities used in the simulation.
     */
    private Craft craft;

    /**
     * Creates a new race state to be added to a machine.
     * @param _name    The name of this race state.
     * @param _players The players object to modify.
     */
    public RaceState(String _name, Player[] _players) {
        super(_name);

        players = _players;
    }

    @Override
    public void onEnter(Object _enterWith) {
        circuit = new Circuit("/media/aidan/BFE6-24C6/dsp/dsp_part2/assets/tracks/track.p2");
        craft   = new Craft(players, circuit.getSpawn());
    }

    @Override
    public void onUpdate(LinkedList<Command> _cmds) {
        // Process any commands which have came in.
        processCmds(_cmds);

        // Progress each player entity in the game simulation
        simulatePlayers();

        // Resolve any wall collisions
        resolveWallCollisions();

        // Resolve any craft - craft collisions
        resolveCraftCollisions();

        // Update the entities positions in the players array
        updatePlayerData();

        // Check for game events such as lap times, completing the race, etc.
        checkGameEvents();

        // Check if the game actually has clients connected.
        checkIfEmpty();
    }

    /**
     * Processes commands which have come into the game state.
     * In this state we are only interested in the client input command.
     * @param _cmds List of commands to process.
     */
    private void processCmds(LinkedList<Command> _cmds) {
        while (_cmds.size() > 0) {
            Command cmd = _cmds.removeFirst();
            switch (cmd.id) {
                case Command.CLIENT_INPUT:
                    cmdClientInput((CmdClientInput) cmd);
                    break;

                case Command.CLIENT_DISCONNECTED:
                    cmdClientDisconnected((CmdClientDisconnected) cmd);
                    break;
            }
        }
    }

    private void cmdClientInput(CmdClientInput _cmd) {
        InputComponent ip = (InputComponent) craft.getPlayerEntity(_cmd.clientID).get("input");
        ip.accelerate = _cmd.accel;
        ip.decelerate = _cmd.decel;
        ip.steerLeft  = _cmd.steerLeft;
        ip.steerRight = _cmd.steerRight;
        ip.airBrakeLeft  = _cmd.abLeft;
        ip.airBrakeRight = _cmd.abRight;
    }

    private void cmdClientDisconnected(CmdClientDisconnected _cmd) {
        System.out.println("Removing entity");
        craft.getRemotePlayers()[_cmd.clientID].destroy();
        craft.getRemotePlayers()[_cmd.clientID] = null;
    }

    /**
     * Steps forward the game simulation my moving each player according to the inputs pressed by the remote client.
     */
    private void simulatePlayers() {
        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) continue;

            Entity e = craft.getPlayerEntity(i);
            e.update(0);
        }
    }

    /**
     * Resolve any wall collisions between the player craft.
     */
    private void resolveWallCollisions() {
        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) continue;

            // Get the entity and ensure it has the AABB and poly components
            Entity e = craft.getPlayerEntity(i);
            if (!e.has("aabb") || !e.has("polygon")) continue;

            // Get the components and query the circuit wall tree for collisions.
            AABBComponent    aabb = (AABBComponent) e.get("aabb");
            PolygonComponent poly = (PolygonComponent) e.get("polygon");

            List<TreeTileWall> collisions = new LinkedList<>();
            circuit.getWallTree().getCollisions(aabb.getBox(), collisions);

            // If there are no collisions skip this loop.
            if (collisions.size() == 0) continue;

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
    }

    /**
     *
     */
    private void resolveCraftCollisions() {
        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) continue;

            // Get the entity and ensure it has the AABB and poly components
            Entity e = craft.getPlayerEntity(i);
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
     * Sets the player structure position and rotation info to that of the simulated entities.
     */
    private void updatePlayerData() {
        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) continue;

            Entity e = craft.getPlayerEntity(i);
            players[i].setX(e.pos.x);
            players[i].setY(e.pos.y);
            players[i].setRotation(e.rotation);
        }
    }

    private void checkGameEvents() {
        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) continue;

            Entity e = craft.getPlayerEntity(i);
            if (!e.has("lap_tracker")) continue;

            ((LapTracker) e.get("lap_tracker")).check(circuit.getCheckpoints());
        }
    }

    /**
     * Checks if the game is empty. If it is return to the lobby so clients can join again.
     */
    private void checkIfEmpty() {
        int playerCount = 0;
        for (Player player : players) {
            if (player != null) playerCount++;
        }

        if (playerCount == 0) {
            changeState("lobby-active", null, null);
        }
    }
}
