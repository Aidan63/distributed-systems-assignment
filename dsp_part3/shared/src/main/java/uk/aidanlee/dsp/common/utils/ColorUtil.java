package uk.aidanlee.dsp.common.utils;

public class ColorUtil {
    public static byte f2b(float _f) {
        return (byte) Math.abs(_f * 255);
    }

    public static float b2f(byte _b) {
        return 1 - (_b / 255);
    }
}
