package uk.aidanlee.dsp.server.states;

import uk.aidanlee.dsp.common.net.commands.Command;
import uk.aidanlee.dsp.common.structural.State;

import java.util.LinkedList;

class RaceStateResults extends State {
    RaceStateResults(String _name) {
        super(_name);
    }

    @Override
    public void onEnter(Object _enterWith) {
        super.onEnter(_enterWith);
    }

    @Override
    public void onUpdate(LinkedList<Command> _cmds) {
        super.onUpdate(_cmds);
    }

    @Override
    public void onLeave(Object _leaveWith) {
        super.onLeave(_leaveWith);
    }
}
