package uk.aidanlee.dsp_assignment.components.craft;

import uk.aidanlee.dsp_assignment.components.AABBComponent;
import uk.aidanlee.dsp_assignment.components.PolygonComponent;
import uk.aidanlee.dsp_assignment.components.QuadtreeComponent;
import uk.aidanlee.dsp_assignment.components.track.BoostPadComponent;
import uk.aidanlee.dsp_assignment.race.Race;
import uk.aidanlee.dsp_assignment.structural.ec.Component;
import uk.aidanlee.jDiffer.Collision;

import java.util.LinkedList;
import java.util.List;

public class BoostPadCollisionComponent extends Component {
    private String lastBoostID;

    public BoostPadCollisionComponent(String _name) {
        super(_name);
        lastBoostID = "";
    }

    @Override
    public void update(float _dt) {
        AABBComponent    aabb = (AABBComponent)    get("aabb");
        PolygonComponent poly = (PolygonComponent) get("polygon");

        List<QuadtreeComponent> collisions = new LinkedList<>();
        Race.circuit.getPadTree().getCollisions(aabb.getBox(), collisions);

        if (collisions.size() == 0) lastBoostID = "";

        for (QuadtreeComponent tile : collisions) {
            if (!tile.has("boost_pad")) continue;

            // If the boost pad is active.
            BoostPadComponent boost = (BoostPadComponent) tile.get("boost_pad");
            if (!boost.active) continue;

            // Check for precise collision
            PolygonComponent padPoly = (PolygonComponent) tile.get("polygon");
            if (Collision.shapeWithShape(poly.getShape(), padPoly.getShape(), null) == null) {
                lastBoostID = "";
                continue;
            }

            // If we haven't passed this boost pad recently, BOOST!
            if (!lastBoostID.equals(boost.entity.getId())) {
                if (has("boost")) remove("boost");
                add(new BoostComponent("boost", 90, 5));

                lastBoostID = tile.entity.getId();
                boost.deactivate();
            }
        }
    }
}
