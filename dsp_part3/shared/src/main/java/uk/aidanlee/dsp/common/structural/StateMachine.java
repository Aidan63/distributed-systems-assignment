package uk.aidanlee.dsp.common.structural;

import com.google.common.eventbus.EventBus;
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
     * Event bus to sent events to the individual states.
     */
    private EventBus events;

    /**
     * Create a new empty state machine.
     */
    public StateMachine() {
        states = new HashMap<>();
        events = new EventBus();
    }

    // Public API

    public State getActiveState() {
        return activeState;
    }

    public EventBus getEvents() {
        return events;
    }

    /**
     * Adds a new state to the machine.
     * @param _state The state to add.
     */
    public StateMachine add(State _state) {
        states.put(_state.getName(), _state);
        _state.setMachine(this);

        return this;
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
        if (!states.containsKey(_stateName)) {
            return;
        }
        if (activeState != null && activeState.getName().equals(_stateName)) {
            return;
        }

        unset(_leaveWith);

        activeState = states.get(_stateName);
        activeState.onEnter(_enterWith);

        events.register(activeState);
    }

    /**
     * Leaves the currently active state.
     * @param _leaveWith The data to send to the states onLeave method.
     */
    public void unset(Object _leaveWith)
    {
        if (activeState != null) {
            events.unregister(activeState);

            activeState.onLeave(_leaveWith);
            activeState = null;
        }
    }

    /**
     *
     */
    public void update() {
        if (activeState != null) {
            activeState.onUpdate();
        }
    }

    /**
     *
     */
    public void render() {
        if (activeState != null) {
            activeState.onRender();
        }
    }
}
