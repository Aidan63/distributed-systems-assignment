package uk.aidanlee.dsp.common.data.circuit;

import com.badlogic.gdx.math.Vector2;

/**
 * Class used by GSON to read a circuit point from the track json file.
 * Each track is made up of a circular doubly linked list of circuit points.
 */
public class CircuitPoint {
    /**
     * Position of this point in the world.
     */
    public Vector2 position;

    /**
     * Tangent of this circuit point.
     */
    public Vector2 tangent;

    /**
     * Normal of this circuit point.
     */
    public Vector2 normal;

    /**
     * Negative offset point for this circuit point.
     */
    public Vector2 negativePoint;

    /**
     * Positive offset point for this circuit point.
     */
    public Vector2 positivePoint;

    /**
     * The negative offset distance from this point.
     */
    public float negativeOffset;

    /**
     * The positive offset distance from this point.
     */
    public float positiveOffset;

    // Holds references to other serialized data.
    // These are set after the track JSON has been serialized.

    public CircuitPoint next;
    public CircuitPoint prev;

    public CircuitTile negativeTile;
    public CircuitTile positiveTile;
}
