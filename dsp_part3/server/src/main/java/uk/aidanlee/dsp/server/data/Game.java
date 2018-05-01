package uk.aidanlee.dsp.server.data;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.structural.StateMachine;
import uk.aidanlee.dsp.server.data.events.EvClientConnected;
import uk.aidanlee.dsp.server.data.events.EvClientDisconnected;
import uk.aidanlee.dsp.server.states.LobbyActiveState;
import uk.aidanlee.dsp.server.states.LobbyCountdownState;
import uk.aidanlee.dsp.server.states.RaceState;

import java.util.HashMap;
import java.util.Map;

/**
 * Game simulation. Game has no dependencies on networking and could in theory be ran without a server.
 */
public class Game {

    /**
     * All of the players data in this game.
     */
    private final Map<Integer, Player> players;

    /**
     * The state machine for the game.
     */
    private final StateMachine states;

    /**
     * Game event bus. Allows various parts of the game simulation to communicate without being tied to each other.
     */
    private final EventBus events;

    // Constructors

    /**
     * Creates a new game simulation with a maximum number of players.
     */
    public Game() {
        players = new HashMap<>();

        events = new EventBus();
        events.register(this);

        // Setup the game state machine.
        states = new StateMachine();
        states.add(new LobbyActiveState("lobby-active", events, players));
        states.add(new LobbyCountdownState("lobby-countdown", events));
        states.add(new RaceState("game", events, players));
        states.set("lobby-active", null, null);
    }

    // Events Functions

    /**
     * Gets the games main event bus.
     * @return EventBus.
     */
    public EventBus getEvents() {
        return events;
    }

    /**
     * When a client connects, add a new player into the structure, keyed by the clientID.
     * @param _event connection event.
     */
    @Subscribe
    public void eventPlayerConnected(EvClientConnected _event) {
        players.put(_event.clientID, new Player(_event.name));
    }

    /**
     * When a client disconnects, remove them from the structure.
     * @param _event disconnection event.
     */
    @Subscribe
    public void eventPlayerDisconnected(EvClientDisconnected _event) {
        players.remove(_event.clientID);
    }

    // Public API

    /**
     * Returns info on all players in the game.
     * @return Player
     */
    public Player getPlayer(int _clientID) {
        return players.get(_clientID);
    }

    /**
     * Returns the name of the active state.
     * @return String
     */
    public String getState() {
        return states.getActiveState().getName();
    }

    /**
     * Progress the game simulation forward one step.
     */
    public void update() {
        states.update();
    }
}
