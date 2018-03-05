package uk.aidanlee.dsp_assignment.utils;

public class MathsUtil {
    public static double lengthdirX(double _length, double _direction) {
        return Math.cos(Math.toRadians(_direction)) * _length;
    }

    public static double lengthdirY(double _length, double _direction) {
        return Math.sin(Math.toRadians(_direction)) * _length;
    }
}
