package uk.aidanlee.dsp.common.components;

import uk.aidanlee.dsp.common.structural.ec.Component;

/**
 * Stats components holds all of the stats for the ships physics.
 */
public class StatsComponent extends Component {

    // Stats which do not change.
    public final float maxSpeed = 30;
    public final float topSpeed = 35;
    public final float accSpeed = 40;
    public final float grip = 30;
    public final float turnSpeed      = 0.4f;
    public final float abTurnSpeed    = 0.2f;
    public final float turnSpeedLimit = 20;
    public final int   turnMod = 90;

    public final float speedDecay    = 0.98f;
    public final float rotationDecay = 0.85f;

    // Stats which change.

    public float acceleration = 0;
    public float direction = 0;

    public float wheelDir = 0;
    public float engineSpeed = 0;
    public float skid = 0;

    public StatsComponent(String _name) {
        super(_name);
    }

    public void stop() {
        engineSpeed = 0;
        skid = 0;
        acceleration = 0;
    }
}
