package uk.aidanlee.dsp.common.structural;

/**
 * Base state class for the state machine.
 */
public class State {
    /**
     * Name of this state.
     */
    private String name;

    /**
     * The state machine this state belongs to.
     */
    protected StateMachine machine;

    /**
     *
     * @param _name
     */
    public State(String _name) {
        name = _name;
    }

    public String getName() {
        return name;
    }

    /**
     * Sets the state machine which owns this state.
     * @param _machine this states machine.
     */
    void setMachine(StateMachine _machine) {
        machine = _machine;
    }

    public void onEnter(Object _enterWith) { }
    public void onLeave(Object _leaveWith) { }
    public void onUpdate() { }
    public void onRender() { }
}
