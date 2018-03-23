package uk.aidanlee.dsp.server.states;

import com.badlogic.gdx.math.Rectangle;
import uk.aidanlee.dsp.common.components.AABBComponent;
import uk.aidanlee.dsp.common.components.InputComponent;
import uk.aidanlee.dsp.common.components.PolygonComponent;
import uk.aidanlee.dsp.common.data.circuit.Circuit;
import uk.aidanlee.dsp.common.data.circuit.TreeTileWall;
import uk.aidanlee.dsp.common.net.Player;
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
    }

    /**
     * Processes commands which have come into the game state.
     * In this state we are only interested in the client input command.
     * @param _cmds List of commands to process.
     */
    private void processCmds(LinkedList<Command> _cmds) {
        while (_cmds.size() > 0) {
            Command cmd = _cmds.removeFirst();
            if (cmd.id == Command.CLIENT_INPUT) {
                // Apply the input to the correct player entity.
                CmdClientInput c = (CmdClientInput) cmd;
                InputComponent ip = (InputComponent) craft.getPlayerEntity(c.clientID).get("input");
                ip.accelerate = c.accel;
                ip.decelerate = c.decel;
                ip.steerLeft  = c.steerLeft;
                ip.steerRight = c.steerRight;
                ip.airBrakeLeft  = c.abLeft;
                ip.airBrakeRight = c.abRight;
            } else {
                System.out.println("Command " + cmd.id + " not used by the game state");
            }
        }
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
     *
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
}
