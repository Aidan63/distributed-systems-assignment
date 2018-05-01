package uk.aidanlee.dsp.common.net;

import java.util.LinkedList;
import java.util.List;

/**
 * A snapshot represents the current state of the game simulation at a specific moment in time.
 * The snapshot contains a list of all players and their information.
 * The clientID, name, ship index, ready state, ship and trail color, x pos, y pos, and rotation for all players are stored in a snapshot.
 */
public class Snapshot {

    /**
     * The players in this snapshot.
     */
    private List<PlayerSet> players;

    // Constructors

    /**
     * Creates a new empty snapshot.
     */
    public Snapshot() {
        players  = new LinkedList<>();
    }

    // Public API

    /**
     * The number of players contained in this snapshot.
     * @return Int
     */
    public int getPlayerCount() {
        return players.size();
    }

    /**
     * Get the player object from the index position.
     * @param _index The index of the player to get.
     * @return Player object.
     */
    public Player getPlayer(int _index) {
        return players.get(_index).getPlayer();
    }

    /**
     * Gets a player object from the provided client ID
     * @param _id Client ID of the player to get.
     * @return Player object.
     */
    public Player getPlayerByID(int _id) {
        return players.stream()
                .filter(p -> p.getClientID() == _id)
                .findFirst()
                .get().getPlayer();
    }

    /**
     * Gets the client ID of the player at the specified index.
     * @param _index Index of the player in this snapshot
     * @return Int
     */
    public int getID(int _index) {
        return players.get(_index).getClientID();
    }

    /**
     * Add a player into this snapshot.
     * @param _id     The ID of the player.
     * @param _player The player object containing all of the players state info..
     */
    public void addPlayer(int _id, Player _player) {
        players.add(new PlayerSet(_id, _player));
    }

    /**
     * Class to store the Player and their clientID.
     */
    private class PlayerSet {
        /**
         * Client ID.
         */
        private int clientID;

        /**
         * Player object.
         */
        private Player player;

        /**
         * Create a new player set.
         * @param _id     ID
         * @param _player Player
         */
        PlayerSet(int _id, Player _player) {
            clientID = _id;
            player   = _player;
        }

        /**
         * Get the client ID of this set.
         * @return Int
         */
        public int getClientID() {
            return clientID;
        }

        /**
         * get the player object of this set.
         * @return Player object
         */
        public Player getPlayer() {
            return player;
        }
    }
}
