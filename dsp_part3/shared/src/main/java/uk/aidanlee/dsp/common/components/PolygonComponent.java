package uk.aidanlee.dsp.common.components;

import com.badlogic.gdx.math.Vector2;
import uk.aidanlee.dsp.common.structural.ec.Component;
import uk.aidanlee.jDiffer.math.Vector;
import uk.aidanlee.jDiffer.shapes.Polygon;

public class PolygonComponent extends Component {
    private Polygon shape;

    public PolygonComponent(String _name, Vector2[] _verts) {
        super(_name);

        Vector[] newVerts = new Vector[_verts.length];
        for (int i = 0; i < _verts.length; i++) {
            newVerts[i] = new Vector(_verts[i].x, _verts[i].y);
        }

        shape = new Polygon(0, 0, newVerts);
    }

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
