package uk.aidanlee.dsp.server.states;

import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.net.commands.CmdClientSettings;
import uk.aidanlee.dsp.common.net.commands.Command;
import uk.aidanlee.dsp.common.structural.State;

import java.util.LinkedList;

public class LobbyActiveState extends State {
    private Player[] players;

    public LobbyActiveState(String _name, Player[] _players) {
        super(_name);

        players = _players;
    }

    @Override
    public void onUpdate(LinkedList<Command> _cmds) {
        processCommands(_cmds);

        if (numPlayers() > 0 && allPlayersReady()) {
            changeState("lobby-countdown", null, null);
        }
    }

    private void processCommands(LinkedList<Command> _cmds) {
        while (_cmds.size() > 0) {
            Command cmd = _cmds.removeFirst();
            if (cmd.id == Command.CLIENT_SETTINGS) {
                CmdClientSettings c = (CmdClientSettings) cmd;
                players[c.clientID].setShipIndex(c.index);
                players[c.clientID].setShipColor(c.shipColor);
                players[c.clientID].setTrailColor(c.trailColor);
                players[c.clientID].setReady(c.ready);
            }
        }
    }
    /**
     * Returns if all connected players are ready.
     * Will also return true if no players are connected.
     * @return true / false if all players are ready.
     */
    private boolean allPlayersReady() {
        for (Player plyr : players) {
            if (plyr == null) continue;
            if (!plyr.isReady()) return false;
        }

        return true;
    }

    /**
     * Returns the number of connected players
     * @return int of connected players
     */
    private int numPlayers() {
        int count = 0;

        for (Player plyr : players) {
            if (plyr != null) count++;
        }

        return count;
    }
}
