package uk.aidanlee.dsp_assignment.states.race;

import com.badlogic.gdx.math.Rectangle;
import uk.aidanlee.dsp_assignment.components.AABBComponent;
import uk.aidanlee.dsp_assignment.components.PolygonComponent;
import uk.aidanlee.dsp_assignment.data.Craft;
import uk.aidanlee.dsp_assignment.data.Views;
import uk.aidanlee.dsp_assignment.data.circuit.Circuit;
import uk.aidanlee.dsp_assignment.data.circuit.TreeTileWall;
import uk.aidanlee.dsp_assignment.structural.State;
import uk.aidanlee.dsp_assignment.structural.ec.Entity;
import uk.aidanlee.dsp_assignment.structural.ec.Visual;
import uk.aidanlee.jDiffer.Collision;
import uk.aidanlee.jDiffer.data.ShapeCollision;
import uk.aidanlee.jDiffer.shapes.Polygon;

import java.util.LinkedList;
import java.util.List;

public class RaceState extends State {

    private Circuit circuit;

    private Craft craft;

    private Views views;

    public RaceState(String _name, Circuit _circuit, Craft _craft, Views _views) {
        super(_name);
        circuit = _circuit;
        craft   = _craft;
        views   = _views;
    }

    @Override
    public void onUpdate() {
        simulatePlayers();

        resolveWallCollisions();

        resolveCraftCollisions();

        checkLapStatus();
    }

    private void simulatePlayers() {
        // Resize the views in case the window size has changed and update all player entities.
        views.resize();

        // Update entities.
        for (Entity e : craft.getLocalPlayers()) {
            e.update(0);
        }
    }

    private void resolveWallCollisions() {

        for (Visual v : craft.getLocalPlayers()) {
            if (!v.has("aabb") || !v.has("polygon")) return;

            // Get the components and query the circuit wall tree for collisions.
            AABBComponent aabb = (AABBComponent)    v.get("aabb");
            PolygonComponent poly = (PolygonComponent) v.get("polygon");

            List<TreeTileWall> collisions = new LinkedList<>();
            circuit.getWallTree().getCollisions(aabb.getBox(), collisions);

            // If there are no collisions skip this loop.
            if (collisions.size() == 0) return;

            // Check each AABB collision for a precise collision.
            for (TreeTileWall col : collisions) {
                Polygon transformedPoly = poly.getShape();

                ShapeCollision wallCol = Collision.shapeWithShape(transformedPoly, col.getPolygon(), null);
                if (wallCol == null) continue;

                v.pos.x += wallCol.separationX;
                v.pos.y += wallCol.separationY;
            }
        }
    }

    private void resolveCraftCollisions() {
        for (Visual v : craft.getLocalPlayers()) {

            // Get the entity and ensure it has the AABB and poly components
            if (!v.has("aabb") || !v.has("polygon")) continue;

            // Get the components and query the circuit wall tree for collisions.
            AABBComponent    aabb = (AABBComponent)    v.get("aabb");
            PolygonComponent poly = (PolygonComponent) v.get("polygon");

            // Check for collisions with all other entities
            for (Entity craft : craft.getLocalPlayers()) {
                if (craft == null) continue;
                if (craft.getName().equals(v.getName())) continue;

                Rectangle otherBox = ((AABBComponent) craft.get("aabb")).getBox();
                if (!aabb.getBox().overlaps(otherBox)) continue;

                PolygonComponent otherPoly = (PolygonComponent) craft.get("polygon");
                ShapeCollision col = Collision.shapeWithShape(poly.getShape(), otherPoly.getShape(), null);
                while (col != null) {
                    // If we are colliding move apart until we aren't.
                    v.pos.x += (float)col.unitVectorX;
                    v.pos.y += (float)col.unitVectorY;
                    craft.pos.x -= (float)col.otherUnitVectorX;
                    craft.pos.y -= (float)col.otherUnitVectorY;

                    col = Collision.shapeWithShape(poly.getShape(), otherPoly.getShape(), null);
                }
            }
        }
    }

    private void checkLapStatus() {
        //
    }
}
