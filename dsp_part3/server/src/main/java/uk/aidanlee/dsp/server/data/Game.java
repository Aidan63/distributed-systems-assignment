package uk.aidanlee.dsp.server.data;

import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.net.commands.Command;
import uk.aidanlee.dsp.common.structural.StateMachine;
import uk.aidanlee.dsp.server.states.LobbyActiveState;
import uk.aidanlee.dsp.server.states.LobbyCountdownState;
import uk.aidanlee.dsp.server.states.RaceState;
import java.util.LinkedList;

public class Game {
    /**
     * All of the players data in this game.
     */
    private final Player[] players;

    /**
     * The state machine for the game.
     */
    private final StateMachine states;

    /**
     * Queue of all commands which come into the game simulation.
     */
    private final LinkedList<Command> commands;

    // Constructors

    /**
     *
     * @param _maxPlayers
     */
    public Game(int _maxPlayers) {
        players = new Player[_maxPlayers];

        commands = new LinkedList<>();

        // Setup the game state machine.
        states = new StateMachine();
        states.add(new LobbyActiveState("lobby-active", players));
        states.add(new LobbyCountdownState("lobby-countdown"));
        states.add(new RaceState("game", players));
        states.set("lobby-active", null, null);
    }

    // Public API

    /**
     * Adds a new command for the game to process.
     * @param _cmd The command to add
     */
    public void addCommand(Command _cmd) {
        commands.addLast(_cmd);
    }

    /**
     * Adds a new player to the game simulation.
     * @param _clientID The remote client ID.
     * @param _name     The name of this player.
     */
    public void addPlayer(int _clientID, String _name) {
        players[_clientID] = new Player(_name);
    }

    /**
     * Removes a player from the game simulation.
     * @param _clientID ID of the player to remove.
     */
    public void removePlayer(int _clientID) {
        players[_clientID] = null;
    }

    /**
     * Returns info on all players in the game.
     * @return
     */
    public Player[] getPlayers() {
        return players;
    }

    /**
     * Returns the name of the active state.
     * @return
     */
    public String getState() {
        return states.getActiveState().getName();
    }

    /**
     * Process and commands and step forward the game simulation.
     */
    public void update() {
        processCmds();
        states.update();
    }

    // Internal Helpers

    /**
     * Process every command the game simulation has received since the last update.
     */
    private void processCmds() {
        while (commands.size() > 0) {
            // While there are still commands read them and push them to the state machine.
            Command cmd = commands.removeFirst();
            switch (cmd.id) {
                case Command.CLIENT_INPUT:
                case Command.CLIENT_SETTINGS:
                    states.pushCommand(cmd);
                    break;
            }
        }
    }
}
