package uk.aidanlee.dsp_assignment.components;

import uk.aidanlee.dsp_assignment.structural.ec.Component;

/**
 * Holds the x and y component of a velocity vector for an entity.
 */
public class VelocityComponent extends Component {
    public float x;
    public float y;

    public VelocityComponent(String _name) {
        super(_name);
        x = 0;
        y = 0;
    }

    @Override
    public void update(float _dt) {
        entity.pos.x += x;
        entity.pos.y += y;
        x = 0;
        y = 0;
    }
}
