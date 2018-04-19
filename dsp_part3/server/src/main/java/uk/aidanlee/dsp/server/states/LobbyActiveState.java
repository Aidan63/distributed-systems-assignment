package uk.aidanlee.dsp.server.states;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.net.commands.CmdClientSettings;
import uk.aidanlee.dsp.common.net.commands.Command;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.server.data.events.EvClientSettings;

import java.util.LinkedList;
import java.util.Map;

public class LobbyActiveState extends State {

    /**
     * Access to the games player structure.
     */
    private Map<Integer, Player> players;

    /**
     * Access to the games event bus.
     */
    private EventBus events;

    /**
     * //
     * @param _name    //
     * @param _events  //
     * @param _players //
     */
    public LobbyActiveState(String _name, EventBus _events, Map<Integer, Player> _players) {
        super(_name);

        events  = _events;
        players = _players;
    }

    @Override
    public void onEnter(Object _enterWith) {
        events.register(this);
        for (Player player : players.values()) {
            player.setReady(false);
        }
    }

    @Override
    public void onLeave(Object _leaveWith) {
        events.unregister(this);
    }

    @Override
    public void onUpdate() {
        if (numPlayers() > 0 && allPlayersReady()) {
            changeState("lobby-countdown", null, null);
        }
    }

    // Event Functions.

    @Subscribe
    public void onClientSettings(EvClientSettings _event) {
        players.get(_event.cmd.clientID).setShipIndex(_event.cmd.index);
        players.get(_event.cmd.clientID).setShipColor(_event.cmd.shipColor);
        players.get(_event.cmd.clientID).setTrailColor(_event.cmd.trailColor);
        players.get(_event.cmd.clientID).setReady(_event.cmd.ready);
    }

    // Private Helper Functions

    /**
     * Returns if all connected players are ready.
     * Will also return true if no players are connected.
     * @return true / false if all players are ready.
     */
    private boolean allPlayersReady() {
        for (Player plyr : players.values()) {
            if (!plyr.isReady()) return false;
        }

        return true;
    }

    /**
     * Returns the number of connected players
     * @return int of connected players
     */
    private int numPlayers() {
        return players.size();
    }
}
