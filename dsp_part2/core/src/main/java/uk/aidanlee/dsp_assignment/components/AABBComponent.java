package uk.aidanlee.dsp_assignment.components;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import uk.aidanlee.dsp_assignment.structural.ec.Component;

/**
 * Holds an AABB bounding box.
 */
public class AABBComponent extends Component {
    /**
     * The size of the box.
     */
    public Vector2 size;

    /**
     * If the box is centered.
     */
    public boolean centred;

    /**
     * Creates a new AABB component.
     * @param _name    Name of the component.
     * @param _width   Width of the AABB.
     * @param _height  Height of the AABB.
     * @param _centred If the AABB is centered.
     */
    public AABBComponent(String _name, float _width, float _height, boolean _centred) {
        super(_name);
        centred = _centred;
        size    = new Vector2(_width, _height);
    }

    /**
     * Returns a libGDX rectangle instance of this AABB.
     * @return libGDX rectangle.
     */
    public Rectangle getBox() {
        if (centred) {
            return new Rectangle(entity.pos.x - (size.x / 2) + entity.origin.x, entity.pos.y - (size.y / 2) + entity.origin.y, size.x, size.y);
        }
        return new Rectangle(entity.pos.x, entity.pos.y, size.x, size.y);
    }
}
