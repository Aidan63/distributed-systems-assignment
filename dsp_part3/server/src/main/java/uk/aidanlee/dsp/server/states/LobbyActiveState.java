package uk.aidanlee.dsp.server.states;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.server.data.events.EvClientSettings;

import java.util.Map;

/**
 * Active lobby state.
 * Lobby is active when some clients are still not ready meaning clients can still change their settings.
 */
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
     * Creates a new active lobby state.
     * @param _name    States name.
     * @param _events  Games event bus.
     * @param _players All of the players in the game.
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
        // If all players are ready, progress to the countdown state.
        if (numPlayers() > 0 && allPlayersReady()) {
            machine.set("lobby-countdown", null, null);
        }
    }

    // Event Functions.

    /**
     * When we receive a client settings event update the clients info to match the events.
     * @param _event settings event.
     */
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
