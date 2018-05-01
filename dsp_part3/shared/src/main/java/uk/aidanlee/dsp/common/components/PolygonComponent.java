package uk.aidanlee.dsp.common.components;

import uk.aidanlee.dsp.common.structural.ec.Component;
import uk.aidanlee.jDiffer.math.Vector;
import uk.aidanlee.jDiffer.shapes.Polygon;

/**
 * Holds a collision polygon.
 */
public class PolygonComponent extends Component {

    /**
     * The collision polygon.
     */
    private Polygon shape;

    /**
     * Creates a new collision polygon using the provided verts.
     * @param _name  Name of this component.
     * @param _verts Vertices to create the polygon from.
     */
    public PolygonComponent(String _name, Vector[] _verts) {
        super(_name);
        shape = new Polygon(0, 0, _verts);
    }

    /**
     * Applies transformation to the collision polygon according to the attached entity and returns it.
     * @return Transformed polygon.
     */
    public Polygon getShape() {
        shape.set_position(new Vector(entity.pos.x + entity.origin.x, entity.pos.y + entity.origin.y));
        shape.set_rotation(entity.rotation);
        return shape;
    }
}
