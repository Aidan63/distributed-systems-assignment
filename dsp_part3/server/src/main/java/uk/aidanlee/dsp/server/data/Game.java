package uk.aidanlee.dsp.server.data;

import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.structural.StateMachine;

public class Game {
    private StateMachine state;
    private Player[] players;

    public Game(int _maxPlayers) {
        players = new Player[_maxPlayers];
        state   = new StateMachine();
    }

    public void simulate(float _dt) {
        //
    }

    public void addPlayer(int _id, String _name) {
        players[_id] = new Player(_name);
    }
    public void removePlayer(int _id) {
        players[_id] = null;
    }
    public Player getPlayer(int _id) {
        return players[_id];
    }
}
