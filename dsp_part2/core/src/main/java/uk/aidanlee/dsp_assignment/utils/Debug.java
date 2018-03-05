package uk.aidanlee.dsp_assignment.utils;

public class Debug {
    public static boolean enabled = false;

    public static boolean[] ShowRenderWindow = new boolean[] { true };

    public static boolean[] _drawPolys = new boolean[] { false };
    public static boolean drawPolys() {
        return _drawPolys[0];
    }

    public static boolean[] _drawAABBs = new boolean[] { false };
    public static boolean drawAABBs() {
        return _drawAABBs[0];
    }

    public static boolean[] _drawQuadtree = new boolean[] { false };
    public static boolean drawQuadtree() {
        return _drawQuadtree[0];
    }
}
