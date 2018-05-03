package uk.aidanlee.dsp.utils;

import com.badlogic.gdx.math.Vector2;

/**
 * Tools for various vector maths functions.
 */
public class Vector2Tools {
    /**
     * Add two vectors together and return a new vector.
     * @param _p1 Vector 1
     * @param _p2 Vector 1
     * @return new Vector
     */
    public static Vector2 add(Vector2 _p1, Vector2 _p2) {
        return new Vector2(_p1.x + _p2.x, _p1.y + _p2.y);
    }

    /**
     * Subtract two vectors and return a new vector.
     * @param _p1 Vector 1
     * @param _p2 Vector 1
     * @return new Vector
     */
    public static Vector2 subtract(Vector2 _p1, Vector2 _p2) {
        return new Vector2(_p1.x - _p2.x, _p1.y - _p2.y);
    }

    /**
     * Multiplies a vector by a scalar value, returning a new vector instance.
     * @param _p1    Vector
     * @param _value Scalar value.
     * @return
     */
    public static Vector2 multiply(Vector2 _p1, float _value) {
        return new Vector2(_p1.x * _value, _p1.y * _value);
    }

    /**
     * Returns the tangent of a vector as a new vector instance.
     * @param _p Vector to get the tangent for.
     * @return vector instance.
     */
    public static Vector2 tangent2D(Vector2 _p) {
        return new Vector2(-_p.y, _p.x);
    }
}
