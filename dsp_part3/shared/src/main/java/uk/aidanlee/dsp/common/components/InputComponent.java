package uk.aidanlee.dsp.common.components;

import uk.aidanlee.dsp.common.structural.ec.Component;

/**
 * Holds information about all of the inputs for this ship.
 * Note that input states are not checked here but instead set from other components.
 *
 * This remove the dependency on any one input try and we can instead just attach "keyboard" or "network" input components
 * which then modify this.
 */
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