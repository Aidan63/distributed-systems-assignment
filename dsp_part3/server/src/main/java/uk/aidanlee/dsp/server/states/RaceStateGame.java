package uk.aidanlee.dsp.server.states;

import com.badlogic.gdx.math.Rectangle;
import com.google.common.eventbus.EventBus;
import uk.aidanlee.dsp.common.components.AABBComponent;
import uk.aidanlee.dsp.common.components.PolygonComponent;
import uk.aidanlee.dsp.common.data.Times;
import uk.aidanlee.dsp.common.data.circuit.Circuit;
import uk.aidanlee.dsp.common.data.circuit.TreeTileWall;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.common.structural.ec.Entity;
import uk.aidanlee.dsp.common.structural.ec.EntityStateMachine;
import uk.aidanlee.dsp.server.data.Craft;
import uk.aidanlee.dsp.server.data.events.EvPlayerFinished;
import uk.aidanlee.dsp.server.data.events.EvRaceResults;
import uk.aidanlee.jDiffer.Collision;
import uk.aidanlee.jDiffer.data.ShapeCollision;
import uk.aidanlee.jDiffer.shapes.Polygon;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Active race state. State is used when clients can freely move around.
 */
class RaceStateGame extends State {

    /**
     * Access to the game event bus.
     */
    private EventBus events;

    /**
     * Access to this races circuit.
     */
    private Circuit circuit;

    /**
     * Access to this races craft entities.
     */
    private Craft craft;

    /**
     * Access to this races times.
     */
    private Times times;

    RaceStateGame(String _name, Circuit _circuit, EventBus _events, Craft _craft, Times _times) {
        super(_name);

        events  = _events;
        circuit = _circuit;
        craft   = _craft;
        times   = _times;
    }

    @Override
    public void onEnter(Object _enterWith) {
        for (Entity e : craft.getRemotePlayers().values()) {
            if (!e.has("fsm")) continue;
            ((EntityStateMachine) e.get("fsm")).changeState("Active");
        }
    }

    @Override
    public void onUpdate() {

        // Progress each player entity in the game simulation
        simulatePlayers();

        // Resolve any wall collisions
        resolveWallCollisions();

        // Resolve any craft - craft collisions
        resolveCraftCollisions();

        //
        checkLapStatus();
    }

    @Override
    public void onLeave(Object _leaveWith) {
        super.onLeave(_leaveWith);
    }

    // Private Functions

    /**
     * Steps forward the game simulation my moving each player according to the inputs pressed by the remote client.
     */
    private void simulatePlayers() {
        for (Entity craft : craft.getRemotePlayers().values()) {
            craft.update(0);
        }
    }

    /**
     * Resolve any wall collisions between the player craft.
     */
    private void resolveWallCollisions() {
        for (Entity e : craft.getRemotePlayers().values()) {

            // Get the entity and ensure it has the AABB and poly components
            if (!e.has("aabb") || !e.has("polygon")) continue;

            // Get the components and query the circuit wall tree for collisions.
            AABBComponent aabb = (AABBComponent) e.get("aabb");
            PolygonComponent poly = (PolygonComponent) e.get("polygon");

            List<TreeTileWall> collisions = new LinkedList<>();
            circuit.getWallTree().getCollisions(aabb.getBox(), collisions);

            // If there are no collisions skip this loop.
            if (collisions.size() == 0) continue;

            // Check each AABB collision for a precise collision.
            for (TreeTileWall col : collisions) {
                Polygon transformedPoly = poly.getShape();

                ShapeCollision wallCol = Collision.shapeWithShape(transformedPoly, col.getPolygon(), null);
                if (wallCol == null) continue;

                e.pos.x += wallCol.separationX;
                e.pos.y += wallCol.separationY;
            }
        }
    }

    /**
     * Moves any colliding ship entities apart from each other.
     */
    private void resolveCraftCollisions() {
        for (Entity e : craft.getRemotePlayers().values()) {

            // Get the entity and ensure it has the AABB and poly components
            if (!e.has("aabb") || !e.has("polygon")) continue;

            // Get the components and query the circuit wall tree for collisions.
            AABBComponent    aabb = (AABBComponent) e.get("aabb");
            PolygonComponent poly = (PolygonComponent) e.get("polygon");

            // Check for collisions with all other entities
            for (Entity craft : craft.getRemotePlayers().values()) {
                if (craft == null) continue;
                if (craft.getName().equals(e.getName())) continue;

                Rectangle otherBox = ((AABBComponent) craft.get("aabb")).getBox();
                if (!aabb.getBox().overlaps(otherBox)) continue;

                PolygonComponent otherPoly = (PolygonComponent) craft.get("polygon");
                ShapeCollision col = Collision.shapeWithShape(poly.getShape(), otherPoly.getShape(), null);
                while (col != null) {
                    // If we are colliding move apart until we aren't.
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
     * Check the lap status of all entities. If an entity has completed all of its laps, disable it.
     */
    private void checkLapStatus() {
        for (Map.Entry<Integer, Entity> entry : craft.getRemotePlayers().entrySet()) {
            // If an entity has finished all laps, disable it as its finished the race.
            // Also fires an event off to tell all other players that a player finished.
            if (times.playerFinished(entry.getValue().getName())) {
                if (((EntityStateMachine) entry.getValue().get("fsm")).getState().equals("Active")) {
                    ((EntityStateMachine) entry.getValue().get("fsm")).changeState("InActive");
                    events.post(new EvPlayerFinished(entry.getKey()));
                }
            }
        }

        // If all players have finished fire off all of the players times and transition into the results sub game event.
        if (times.allPlayersFinished()) {
            events.post(new EvRaceResults(getTimes()));
            machine.set("results", null, null);
        }
    }

    /**
     * Constructs a map of clientID integers to a list of all of that clients times.
     * This map of times is then sent to all other players.
     * @return All clients times.
     */
    private Map<Integer, List<Float>> getTimes() {

        Map<Integer, List<Float>> structure = new HashMap<>();

        for (Map.Entry<String, List<Float>> entry : times.getTimes().entrySet()) {
            structure.put(findCraftClientID(entry.getKey()), entry.getValue());
        }

        return structure;
    }

    /**
     * Given a craft entities name, the corresponding clientID will be returned.
     * @param _entityName Entity name.
     * @return clientID.
     */
    private int findCraftClientID(String _entityName) {
        for (Map.Entry<Integer, Entity> entry : craft.getRemotePlayers().entrySet()) {
            if (entry.getValue().getName().equals(_entityName)) return entry.getKey();
        }

        return -1;
    }
}
