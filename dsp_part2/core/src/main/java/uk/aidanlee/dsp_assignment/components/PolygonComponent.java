package uk.aidanlee.dsp_assignment.components;

import uk.aidanlee.dsp_assignment.structural.ec.Component;
import uk.aidanlee.jDiffer.math.Vector;
import uk.aidanlee.jDiffer.shapes.Polygon;

public class PolygonComponent extends Component {
    private Polygon shape;

    public PolygonComponent(String _name, Vector[] _verts) {
        super(_name);
        shape = new Polygon(0, 0, _verts);
    }

    public Polygon getShape() {
        shape.set_position(new Vector(entity.pos.x + entity.origin.x, entity.pos.y + entity.origin.y));
        shape.set_rotation(entity.rotation);
        return shape;
    }
}
