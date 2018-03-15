package uk.aidanlee.dsp.common.components;

import uk.aidanlee.dsp.common.structural.ec.Component;

public class InputComponent extends Component {
    public boolean accelerate = false;
    public boolean decelerate = false;
    public boolean steerLeft  = false;
    public boolean steerRight = false;
    public boolean airBrakeLeft  = false;
    public boolean airBrakeRight = false;

    public InputComponent(String _name) {
        super(_name);
    }
}