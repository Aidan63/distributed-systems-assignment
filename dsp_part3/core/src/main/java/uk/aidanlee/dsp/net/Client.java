package uk.aidanlee.dsp.net;

public class Client {
    /**
     * The unique ID of the client.
     */
    private int id;

    /**
     * This clients name.
     */
    private String name;

    /**
     * The index of the the ship they are using.
     */
    private int shipIndex;

    /**
     * The normalized RGB value of their ship
     */
    private float[] shipColor;

    /**
     * The normalized RGB value of their ships trail.
     */
    private float[] trailColor;

    public Client(int _id, String _name) {
        id   = _id;
        name = _name;
    }

    public int getId() {
        return id;
    }

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
}
