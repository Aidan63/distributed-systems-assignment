package uk.aidanlee.dsp_assignment.components;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import uk.aidanlee.dsp_assignment.structural.IQuadtreeElement;
import uk.aidanlee.dsp_assignment.structural.ec.Component;

import static uk.aidanlee.dsp_assignment.utils.Vector2Tools.*;

public class QuadtreeComponent extends Component implements IQuadtreeElement {
    private Rectangle aabb;

    public QuadtreeComponent(String _name, Vector2[] _verts) {
        super(_name);

        Vector2 min = _verts[0].cpy();
        Vector2 max = _verts[0].cpy();
        for (Vector2 v : _verts) {
            if (v.x > max.x) max.x = v.x;
            if (v.y > max.y) max.y = v.y;
            if (v.x < min.x) min.x = v.x;
            if (v.y < min.y) min.y = v.y;
        }

        aabb = new Rectangle(min.x, min.y, subtract(max, min).x, subtract(max, min).y);
    }

    @Override
    public Rectangle box() {
        return aabb;
    }
}
