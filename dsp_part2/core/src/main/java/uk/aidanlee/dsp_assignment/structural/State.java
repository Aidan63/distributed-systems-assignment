package uk.aidanlee.dsp_assignment.structural;

public class State {
    /**
     * Name of this state.
     */
    private String name;

    /**
     * The state machine this state belongs to.
     */
    private StateMachine machine;

    public State(String _name) {
        name = _name;
    }

    public String getName() {
        return name;
    }

    public void setMachine(StateMachine machine) {
        this.machine = machine;
    }

    public void onEnter(Object _enterWith) {
        System.out.println( name +  " entered state with : " + _enterWith);
    }
    public void onLeave(Object _leaveWith) {
        System.out.println( name + " left state with : " + _leaveWith);
    }
    public void onUpdate() {
        //
    }
    public void onRender() {
        //
    }

    public void changeState(String _name, Object _enterWith, Object _leaveWith) {
        machine.set(_name, _enterWith, _leaveWith);
    }
}