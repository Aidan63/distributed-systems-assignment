package uk.aidanlee.dsp.server.states;

import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.server.Server;

public class LobbyActiveState extends State {
    LobbyActiveState(String _name) {
        super(_name);
    }

    @Override
    public void onUpdate() {
        if (Server.connections.getNumClientsConnected() > 0 && allReady()) {
            changeState("lobby-countdown", null, null);
        }
    }

    private boolean allReady() {
        for (Player plyr : Server.game.getPlayers()) {
            if (plyr == null) continue;
            if (!plyr.isReady()) return false;
        }

        return true;
    }
}
