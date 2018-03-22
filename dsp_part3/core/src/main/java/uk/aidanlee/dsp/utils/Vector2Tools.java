package uk.aidanlee.dsp.utils;

import com.badlogic.gdx.math.Vector2;

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
     *
     * @param _p1
     * @param _value
     * @return
     */
    public static Vector2 multiply(Vector2 _p1, float _value) {
        return new Vector2(_p1.x * _value, _p1.y * _value);
    }

    /**
     *
     * @param _p1
     * @param _p2
     * @return
     */
    public static float distance(Vector2 _p1, Vector2 _p2) {
        return Vector2Tools.subtract(_p1, _p2).len();
    }

    /**
     *
     * @param _p1
     * @param _p2
     * @return
     */
    public static float cross2D(Vector2 _p1, Vector2 _p2) {
        return (_p1.x * _p2.x) - (_p1.y * _p2.y);
    }

    /**
     *
     * @param _p
     * @return
     */
    public static Vector2 absolute(Vector2 _p) {
        return new Vector2(Math.abs(_p.x), Math.abs(_p.y));
    }

    /**
     *
     * @param _radians
     * @return
     */
    public static Vector2 setFromAngle(float _radians) {
        return new Vector2((float)Math.cos(_radians), (float)Math.sin(_radians));
    }

    /**
     *
     * @param _p
     * @return
     */
    public static Vector2 tangent2D(Vector2 _p) {
        return new Vector2(-_p.y, _p.x);
    }
}
