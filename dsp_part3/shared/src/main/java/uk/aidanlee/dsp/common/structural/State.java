package uk.aidanlee.dsp.common.structural;

import uk.aidanlee.dsp.common.net.commands.Command;
import java.util.LinkedList;

public class State {
    /**
     * Name of this state.
     */
    private String name;

    /**
     * The state machine this state belongs to.
     */
    private StateMachine machine;

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

    public void setMachine(StateMachine machine) {
        this.machine = machine;
    }

    public void onEnter(Object _enterWith) {
        System.out.println( name +  " entered state with : " + _enterWith);
    }
    public void onLeave(Object _leaveWith) {
        System.out.println( name + " left state with : " + _leaveWith);
    }
    public void onUpdate(LinkedList<Command> _cmds) {
        //
    }
    public void onRender() {
        //
    }

    public void changeState(String _name, Object _enterWith, Object _leaveWith) {
        machine.set(_name, _enterWith, _leaveWith);
    }
}
