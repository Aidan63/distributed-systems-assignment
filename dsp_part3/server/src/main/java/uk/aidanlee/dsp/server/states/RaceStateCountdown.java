package uk.aidanlee.dsp.server.states;

import uk.aidanlee.dsp.common.data.ServerEvent;
import uk.aidanlee.dsp.common.net.commands.CmdServerEvent;
import uk.aidanlee.dsp.common.net.commands.Command;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.server.Server;

import java.util.LinkedList;

class RaceStateCountdown extends State {
    private int timer;

    RaceStateCountdown(String _name) {
        super(_name);
    }

    @Override
    public void onEnter(Object _enterWith) {
        timer = 0;
    }

    @Override
    public void onUpdate(LinkedList<Command> _cmds) {
        timer++;

        // 3 second countdown
        if (timer == 180) {
            // Change to the game and tell all clients.
            changeState("game", null, null);
            Server.connections.addReliableCommandAll(new CmdServerEvent(ServerEvent.EVENT_RACE_START));
        }
    }

    @Override
    public void onLeave(Object _leaveWith) {
        super.onLeave(_leaveWith);
    }
}
