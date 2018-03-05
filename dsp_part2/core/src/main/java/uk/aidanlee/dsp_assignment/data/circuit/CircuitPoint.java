package uk.aidanlee.dsp_assignment.data.circuit;

import com.badlogic.gdx.math.Vector2;

public class CircuitPoint {
    public CircuitPoint next;
    public CircuitPoint prev;

    public Vector2 position;
    public Vector2 tangent;
    public Vector2 normal;

    public Vector2 negativePoint;
    public Vector2 positivePoint;

    public float negativeOffset;
    public float positiveOffset;

    public CircuitTile negativeTile;
    public CircuitTile positiveTile;
}
