package uk.aidanlee.dsp.common.utils;

/**
 * Static class which contains a handful of useful maths functions.
 */
public class MathsUtil {
    /**
     * Gets the x component of a position when given an angle and a distance.
     * @param _length    Length of the vector.
     * @param _direction Angle (degrees) of the vector.
     * @return x position of the vector.
     */
    public static double lengthdirX(double _length, double _direction) {
        return Math.cos(Math.toRadians(_direction)) * _length;
    }

    /**
     * Gets the y component of a position when given an angle and a distance.
     * @param _length    Length of the vector.
     * @param _direction Angle (degrees) of the vector.
     * @return y position of the vector.
     */
    public static double lengthdirY(double _length, double _direction) {
        return Math.sin(Math.toRadians(_direction)) * _length;
    }
}
