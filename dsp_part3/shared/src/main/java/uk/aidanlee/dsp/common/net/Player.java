package uk.aidanlee.dsp.common.net;

/**
 * Player data container used by the game simulation.
 * Stores the complete information about a player in the game.
 */
public class Player {
    /**
     * This players name.
     */
    private String name;

    /**
     * The ship index for the ship texture atlas.
     */
    private int shipIndex;

    /**
     * The normalized RGB triplet for the players ship color.
     */
    private float[] shipColor;

    /**
     * The normalized RGB triplet for the players trail color.
     */
    private float[] trailColor;

    /**
     * The ready state of this player.
     */
    private boolean ready;

    /**
     * The x position of this player.
     */
    private float x;

    /**
     * The y position of this player.
     */
    private float y;

    /**
     * The rotation of this player.
     */
    private float rotation;

    /**
     * Creates a new player with default ship index and colors.
     * @param _name The name of this player.
     */
    public Player(String _name) {
        name       = _name;
        shipIndex  = 0;
        shipColor  = new float[] { 1, 1, 1 };
        trailColor = new float[] { 1, 1, 1 };
        ready      = false;
        x          = 0;
        y          = 0;
        rotation   = 0;
    }

    /**
     * Creates a new player with no default values.
     * @param _name       The name of this player.
     * @param _index      The ship index for this player.
     * @param _shipColor  The ship color for this player.
     * @param _trailColor The trail color for this player.
     */
    public Player(String _name, int _index, float[] _shipColor, float[] _trailColor) {
        name       = _name;
        shipIndex  = _index;
        shipColor  = _shipColor;
        trailColor = _trailColor;
    }

    // whole lot of getters and setters.

    public String getName() {
        return name;
    }

    public int getShipIndex() {
        return shipIndex;
    }
    public void setShipIndex(int shipIndex) {
        this.shipIndex = shipIndex;
    }

    public float[] getShipColor() {
        return shipColor;
    }
    public void setShipColor(float[] shipColor) {
        this.shipColor = shipColor;
    }

    public float[] getTrailColor() {
        return trailColor;
    }
    public void setTrailColor(float[] trailColor) {
        this.trailColor = trailColor;
    }

    public boolean isReady() {
        return ready;
    }
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public float getX() {
        return x;
    }
    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }
    public void setY(float y) {
        this.y = y;
    }

    public float getRotation() {
        return rotation;
    }
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }
}
