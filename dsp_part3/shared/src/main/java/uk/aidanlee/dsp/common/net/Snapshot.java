package uk.aidanlee.dsp.common.net;

import java.util.LinkedList;
import java.util.List;

public class Snapshot {
    /**
     * The players in this snapshot.
     */
    private List<PlayerSet> players;

    // Constructors

    public Snapshot() {
        players = new LinkedList<>();
    }

    // Getters and Setters

    // Public API

    public int getPlayers() {
        return players.size();
    }

    public Player getPlayer(int _index) {
        return players.get(_index).getPlayer();
    }

    public int getID(int _index) {
        return players.get(_index).getClientID();
    }

    public void addPlayer(int _id, Player _player) {
        players.add(new PlayerSet(_id, _player));
    }

    private class PlayerSet {
        private int clientID;
        private Player player;

        PlayerSet(int _id, Player _player) {
            clientID = _id;
            player   = _player;
        }

        public int getClientID() {
            return clientID;
        }

        public Player getPlayer() {
            return player;
        }
    }
}
