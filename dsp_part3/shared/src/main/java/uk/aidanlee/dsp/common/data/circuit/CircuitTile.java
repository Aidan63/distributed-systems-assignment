package uk.aidanlee.dsp.common.data.circuit;

import com.badlogic.gdx.math.Vector2;

/**
 * Class used by GSON to read a circuit tile from the track json file.
 */
public class CircuitTile {

    /**
     * Unique ID for this tile.
     */
    public String id;

    /**
     * Array of four verts for the tiles world position.
     */
    public Vector2[] verts;

    /**
     * The frame name this tile uses in the texture atlas.
     */
    public String frame;

    /**
     * Extra tag information for each tile. (Unused)
     */
    public String[] tags;
}
