package uk.aidanlee.dsp.server.states;

import uk.aidanlee.dsp.common.data.GameState;
import uk.aidanlee.dsp.common.net.commands.CmdServerState;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.server.Server;

import java.util.Timer;
import java.util.TimerTask;

public class LobbyCountdownState extends State {

    LobbyCountdownState(String _name) {
        super(_name);
    }

    @Override
    public void onEnter(Object _enterWith) {
        // Tell all clients the game countdown has started.
        Server.connections.addReliableCommandAll(new CmdServerState(GameState.LOBBY_COUNTDOWN));

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Change to the game and tell all clients.
                Server.state.set("game", null, null);
                Server.connections.addReliableCommandAll(new CmdServerState(GameState.GAME_DEBUG));
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 3000);
    }
}
