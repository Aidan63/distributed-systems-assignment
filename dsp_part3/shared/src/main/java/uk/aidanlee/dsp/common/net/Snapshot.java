package uk.aidanlee.dsp.common.net;

import java.util.LinkedList;
import java.util.List;

public class Snapshot {
    private List<PlayerEntity> players;

    public Snapshot() {
        players = new LinkedList<>();
    }

    public List<PlayerEntity> getPlayers() {
        return players;
    }
    public void addPlayer(int _id, float _x, float _y, float _angle) {
        players.add(new PlayerEntity(_id, _x, _y, _angle));
    }

    public class PlayerEntity {
        public final int id;
        public final float x;
        public final float y;
        public final float angle;

        public PlayerEntity(int _id, float _x, float _y, float _angle) {
            id = _id;
            x  = _x;
            y  = _y;
            angle = _angle;
        }
    }
}
