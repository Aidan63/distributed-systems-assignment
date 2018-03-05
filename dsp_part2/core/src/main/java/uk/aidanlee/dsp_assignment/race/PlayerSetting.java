package uk.aidanlee.dsp_assignment.race;

public class PlayerSetting {
    private final int craftIndex;
    private final float[] craftColor;
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
