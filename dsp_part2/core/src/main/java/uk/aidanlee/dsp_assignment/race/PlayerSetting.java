package uk.aidanlee.dsp_assignment.race;

public class PlayerSetting {
    /**
     * Craft index is a value 0 - 7 which maps onto a texture atlas frame for the ship image.
     */
    private final int craftIndex;

    /**
     * normalized (0 - 1) RGB colour codes.
     */
    private final float[] craftColor;

    /**
     * normalized (0 - 1) RGB colour codes.
     */
    private final float[] trailColor;

    public PlayerSetting(int _index, float[] _craftColor, float[] _trailColor) {
        craftIndex = _index;
        craftColor = _craftColor;
        trailColor = _trailColor;
    }

    public int getCraftIndex() {
        return craftIndex;
    }

    public float[] getCraftColor() {
        return craftColor;
    }

    public float[] getTrailColor() {
        return trailColor;
    }
}
