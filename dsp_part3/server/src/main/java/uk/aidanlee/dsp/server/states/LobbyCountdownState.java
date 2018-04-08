package uk.aidanlee.dsp.server.states;

import uk.aidanlee.dsp.common.data.ServerEvent;
import uk.aidanlee.dsp.common.net.commands.CmdServerEvent;
import uk.aidanlee.dsp.common.net.commands.Command;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.server.Server;

import java.util.LinkedList;

public class LobbyCountdownState extends State {
    private int timer;

    public LobbyCountdownState(String _name) {
        super(_name);
    }

    @Override
    public void onEnter(Object _enterWith) {
        // Tell all clients the game countdown has started.
        Server.connections.addReliableCommandAll(new CmdServerEvent(ServerEvent.EVENT_LOBBY_COUNTDOWN));
        timer = 0;
    }

    @Override
    public void onUpdate(LinkedList<Command> _cmds) {
        timer++;

        // 3 second countdown
        if (timer == 180) {
            // Change to the game and tell all clients.
            changeState("game", null, null);
            Server.connections.addReliableCommandAll(new CmdServerEvent(ServerEvent.EVENT_RACE_ENTER));
        }
    }
}
