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
     *
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

    @Subscribe
    public void eventPlayerConnected(EvClientConnected _event) {
        players.put(_event.clientID, new Player(_event.name));
    }

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
     * Process and commands and step forward the game simulation.
     */
    public void update() {
        states.update();
    }
}
