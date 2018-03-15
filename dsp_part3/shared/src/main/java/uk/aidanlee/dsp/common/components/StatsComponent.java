package uk.aidanlee.dsp.common.components;

import uk.aidanlee.dsp.common.structural.ec.Component;

public class StatsComponent extends Component {
    public float acceleration = 0;
    public float direction = 0;

    public float maxSpeed = 30;
    public float topSpeed = 35;
    public float accSpeed = 40;
    public float grip = 30;
    public float turnSpeed      = 0.4f;
    public float abTurnSpeed    = 0.2f;
    public float turnSpeedLimit = 20;

    public float speedDecay    = 0.98f;
    public float rotationDecay = 0.85f;

    public int turnMod = 90;
    public float wheelDir = 0;
    public float engineSpeed = 0;
    public float skid = 0;

    public StatsComponent(String _name) {
        super(_name);
    }
}
