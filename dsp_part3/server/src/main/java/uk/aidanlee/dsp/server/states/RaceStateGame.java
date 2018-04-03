package uk.aidanlee.dsp.server.states;

import com.badlogic.gdx.math.Rectangle;
import uk.aidanlee.dsp.common.components.AABBComponent;
import uk.aidanlee.dsp.common.components.PolygonComponent;
import uk.aidanlee.dsp.common.components.craft.LapTracker;
import uk.aidanlee.dsp.common.data.Times;
import uk.aidanlee.dsp.common.data.circuit.Circuit;
import uk.aidanlee.dsp.common.data.circuit.TreeTileWall;
import uk.aidanlee.dsp.common.net.commands.Command;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.common.structural.ec.Entity;
import uk.aidanlee.dsp.server.data.Craft;
import uk.aidanlee.jDiffer.Collision;
import uk.aidanlee.jDiffer.data.ShapeCollision;
import uk.aidanlee.jDiffer.shapes.Polygon;

import java.util.LinkedList;
import java.util.List;

class RaceStateGame extends State {
    private Circuit circuit;
    private Craft craft;
    private Times times;

    RaceStateGame(String _name, Circuit _circuit, Craft _craft, Times _times) {
        super(_name);

        circuit = _circuit;
        craft   = _craft;
        times   = _times;
    }

    @Override
    public void onEnter(Object _enterWith) {
        super.onEnter(_enterWith);
    }

    @Override
    public void onUpdate(LinkedList<Command> _cmds) {

        // Progress each player entity in the game simulation
        simulatePlayers();

        // Resolve any wall collisions
        resolveWallCollisions();

        // Resolve any craft - craft collisions
        resolveCraftCollisions();

        // Checks for laps and records times.
        checkLapTimes();
    }

    @Override
    public void onLeave(Object _leaveWith) {
        super.onLeave(_leaveWith);
    }

    //

    /**
     * Steps forward the game simulation my moving each player according to the inputs pressed by the remote client.
     */
    private void simulatePlayers() {
        for (Entity craft : craft.getRemotePlayers()) {
            if (craft == null) continue;

            craft.update(0);
        }
    }


    /**
     * Resolve any wall collisions between the player craft.
     */
    private void resolveWallCollisions() {
        for (int i = 0; i < craft.getRemotePlayers().length; i++) {

            // Get the entity and ensure it has the AABB and poly components
            Entity e = craft.getPlayerEntity(i);
            if (e == null) continue;
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
        for (int i = 0; i < craft.getRemotePlayers().length; i++) {

            // Get the entity and ensure it has the AABB and poly components
            Entity e = craft.getPlayerEntity(i);
            if (e == null) continue;
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
    private void checkLapTimes() {
        // Check and update all players lap times info.
        for (int i = 0; i < craft.getRemotePlayers().length; i++) {

            Entity e = craft.getPlayerEntity(i);
            if (e == null) continue;
            if (!e.has("lap_tracker")) continue;

            ((LapTracker) e.get("lap_tracker")).check(circuit.getCheckpoints(), times);
        }
    }
}
