package uk.aidanlee.dsp_assignment.components;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import uk.aidanlee.dsp_assignment.structural.ec.Component;

public class AABBComponent extends Component {
    public Vector2 size;
    public boolean centred;

    public AABBComponent(String _name, float _width, float _height, boolean _centred) {
        super(_name);
        centred = _centred;
        size    = new Vector2(_width, _height);
    }

    public Rectangle getBox() {
        if (centred) {
            return new Rectangle(entity.pos.x - (size.x / 2) + entity.origin.x, entity.pos.y - (size.y / 2) + entity.origin.y, size.x, size.y);
        }
        return new Rectangle(entity.pos.x, entity.pos.y, size.x, size.y);
    }
}
