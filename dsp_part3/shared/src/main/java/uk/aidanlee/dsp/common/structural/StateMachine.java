package uk.aidanlee.dsp.common.structural;

import uk.aidanlee.dsp.common.net.commands.Command;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class StateMachine {
    /**
     * All of the states in this machine keyed by the states name.
     */
    private Map<String, State> states;

    /**
     * The currently active state.
     */
    private State activeState;

    /**
     * List of commands to pass to the active state next update
     */
    private LinkedList<Command> commands;

    /**
     * Create a new empty state machine.
     */
    public StateMachine() {
        states   = new HashMap<>();
        commands = new LinkedList<>();
    }

    // Public API

    public void pushCommand(Command _cmd) {
        commands.addLast(_cmd);
    }

    /**
     * Returns the name of the active state.
     * @return State name string.
     */
    public String getActiveStateName() {
        return activeState.getName();
    }

    /**
     *
     * @return
     */
    public State getActiveState() {
        return activeState;
    }

    /**
     * Adds a new state to the machine.
     * @param _state The state to add.
     */
    public void add(State _state) {
        states.put(_state.getName(), _state);
        _state.setMachine(this);
    }

    /**
     * Removes a state from the machine, calling its onLeave event if it's currently active.
     * @param _stateName The name of the state to remove.
     * @param _leaveWith The data to send to the states onLeave method.
     */
    public void remove(String _stateName, Object _leaveWith) {
        if (!states.containsKey(_stateName)) return;

        State state = states.get(_stateName);
        if (activeState == state) {
            activeState.onLeave(_leaveWith);
            activeState = null;
        }

        states.remove(_stateName);
    }

    /**
     * Sets the active state to the state with the provided name.
     * @param _stateName The state to activate.
     * @param _enterWith The data to send to the now active states onEnter method.
     * @param _leaveWith The data to send to the previously active states onEnter method.
     */
    public void set(String _stateName, Object _enterWith, Object _leaveWith)
    {
        if (!states.containsKey(_stateName)) return;

        unset(_leaveWith);

        activeState = states.get(_stateName);
        activeState.onEnter(_enterWith);
    }

    /**
     * Leaves the currently active state.
     * @param _leaveWith The data to send to the states onLeave method.
     */
    public void unset(Object _leaveWith)
    {
        if (activeState != null) {
            activeState.onLeave(_leaveWith);
            activeState = null;
        }
    }

    public void update() {
        if (activeState != null) activeState.onUpdate(commands);
        commands.clear();
    }
    public void render() {
        if (activeState != null) activeState.onRender();
    }
}
