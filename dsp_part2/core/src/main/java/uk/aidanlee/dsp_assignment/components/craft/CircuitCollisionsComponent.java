package uk.aidanlee.dsp_assignment.components.craft;

import com.badlogic.gdx.math.Vector2;
import uk.aidanlee.dsp_assignment.components.AABBComponent;
import uk.aidanlee.dsp_assignment.components.PolygonComponent;
import uk.aidanlee.dsp_assignment.components.VelocityComponent;
import uk.aidanlee.dsp_assignment.data.circuit.TreeTileWall;
import uk.aidanlee.dsp_assignment.race.Race;
import uk.aidanlee.dsp_assignment.structural.ec.Component;
import uk.aidanlee.jDiffer.Collision;
import uk.aidanlee.jDiffer.data.RayCollision;
import uk.aidanlee.jDiffer.data.ShapeCollision;
import uk.aidanlee.jDiffer.shapes.Polygon;

import java.util.LinkedList;
import java.util.List;

public class CircuitCollisionsComponent extends Component {
    public CircuitCollisionsComponent(String _name) {
        super(_name);
    }

    @Override
    public void update(float _dt) {
        if (!has("aabb") || !has("polygon") || !has("velocity") || !has("stats")) return;

        AABBComponent    aabb    = (AABBComponent)    get("aabb");
        PolygonComponent polygon = (PolygonComponent) get("polygon");

        List<TreeTileWall> collisions = new LinkedList<>();
        Race.circuit.getWallTree().getCollisions(aabb.getBox(), collisions);

        if (collisions.size() == 0) return;

        for (TreeTileWall col : collisions) {
            Polygon transformedPoly = polygon.getShape();

            ShapeCollision wallCol = Collision.shapeWithShape(transformedPoly, col.wall, null);
            if (wallCol == null) continue;

            RayCollision rayCol = Collision.rayWithShape(col.ray, transformedPoly, null);
            entity.pos.x += wallCol.separationX;
            entity.pos.y += wallCol.separationY;

            if (rayCol == null) continue;

            // Calculate the reflection
            VelocityComponent vel = (VelocityComponent) get("velocity");
            Vector2 dir = new Vector2(vel.x, vel.y);

            double dotProduct = dir.dot(col.tangent.nor());
            Vector2 reflection = new Vector2(
                    (float)(-2 * (dotProduct) * col.tangent.nor().x + dir.x),
                    (float)(-2 * (dotProduct) * col.tangent.nor().y + dir.y));

            // Calculate the angle we hit the wall to see if we want to scrape along or bounce off.
            double deg = Math.acos(dir.nor().dot(reflection.nor()) * 180 / Math.PI);

            // Do we bounce?
            if (deg < 140) {
                StatsComponent stats = (StatsComponent) get("stats");
                double newDir = Math.abs((Math.atan2(reflection.y, reflection.x) * 180 / Math.PI) + 180);
                stats.direction    = (float)newDir;
                stats.engineSpeed -= (stats.engineSpeed / 4);
            }
        }
    }
}
