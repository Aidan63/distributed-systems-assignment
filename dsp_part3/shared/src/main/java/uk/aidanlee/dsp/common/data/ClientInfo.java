package uk.aidanlee.dsp.common.data;

import uk.aidanlee.dsp.common.net.Player;

/**
 * Contains all info about a connected client. Associates a Player instance with a clientID.
 */
public class ClientInfo {

    /**
     * ClientID.
     */
    private int id;

    /**
     * Player data.
     */
    private Player player;

    public ClientInfo(int _id, Player _player) {
        id     = _id;
        player = _player;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return player.getName();
    }

    public int getShipIndex() {
        return player.getShipIndex();
    }

    public float[] getShipColor() {
        return player.getShipColor();
    }

    public float[] getTrailColor() {
        return player.getTrailColor();
    }

    public boolean isReady() {
        return player.isReady();
    }
}
