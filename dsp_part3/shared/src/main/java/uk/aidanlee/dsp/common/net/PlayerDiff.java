package uk.aidanlee.dsp.common.net;

public class PlayerDiff {
    public int id;

    // Diff vars

    public boolean diffShipIndex;

    public boolean diffShipColR;
    public boolean diffShipColG;
    public boolean diffShipColB;

    public boolean diffTrailColR;
    public boolean diffTrailColG;
    public boolean diffTrailColB;

    public boolean diffX;
    public boolean diffY;
    public boolean diffRotation;

    // Actual Values

    public int shipIndex;

    public float shipColR;
    public float shipColG;
    public float shipColB;

    public float trailColR;
    public float trailColG;
    public float trailColB;

    public boolean ready;

    public float x;
    public float y;
    public float rotation;
}
