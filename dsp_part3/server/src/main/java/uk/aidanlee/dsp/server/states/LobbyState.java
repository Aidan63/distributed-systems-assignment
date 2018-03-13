package uk.aidanlee.dsp.server.states;

import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.common.structural.StateMachine;

public class LobbyState extends State {
    private StateMachine subMachine;

    public LobbyState(String _name) {
        super(_name);
    }

    @Override
    public void onEnter(Object _enterWith) {
        subMachine = new StateMachine();
        subMachine.add(new LobbyActiveState("lobby-active"));
        subMachine.add(new LobbyCountdownState("lobby-countdown"));
        subMachine.set("lobby-active", null, null);
    }

    @Override
    public String getSubStateName() {
        return subMachine.getActiveStateName();
    }

    @Override
    public void onUpdate() {
        subMachine.update();
    }

    @Override
    public void onLeave(Object _leaveWith) {
        super.onLeave(_leaveWith);
    }
}
