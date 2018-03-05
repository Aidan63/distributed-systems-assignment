package uk.aidanlee.dsp_assignment.components.craft;

import com.badlogic.gdx.math.Rectangle;
import uk.aidanlee.dsp_assignment.components.AABBComponent;
import uk.aidanlee.dsp_assignment.components.PolygonComponent;
import uk.aidanlee.dsp_assignment.race.Race;
import uk.aidanlee.dsp_assignment.structural.ec.Component;
import uk.aidanlee.dsp_assignment.structural.ec.Entity;
import uk.aidanlee.jDiffer.Collision;
import uk.aidanlee.jDiffer.data.ShapeCollision;

public class CraftCollisionsComponent extends Component {
    public CraftCollisionsComponent(String _name) {
        super(_name);
    }

    @Override
    public void update(float _dt) {
        if (!has("aabb") || !has("polygon")) return;

        AABBComponent    aabb    = (AABBComponent) get("aabb");
        PolygonComponent polygon = (PolygonComponent) get("polygon");

        for (Entity craft : Race.craft.getLocalPlayers()) {
            if (craft.getName().equals(entity.getName())) continue;

            Rectangle otherBox = ((AABBComponent) craft.get("aabb")).getBox();
            if (!aabb.getBox().overlaps(otherBox)) continue;

            PolygonComponent otherPoly = (PolygonComponent) craft.get("polygon");
            ShapeCollision col = Collision.shapeWithShape(polygon.getShape(), otherPoly.getShape(), null);
            while (col != null) {
                entity.pos.x += (float)col.unitVectorX;
                entity.pos.y += (float)col.unitVectorY;
                craft.pos.x -= (float)col.otherUnitVectorX;
                craft.pos.y -= (float)col.otherUnitVectorY;

                col = Collision.shapeWithShape(polygon.getShape(), otherPoly.getShape(), null);
            }
        }
    }
}
