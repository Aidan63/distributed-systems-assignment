package uk.aidanlee.dsp.common.data.circuit;

import com.badlogic.gdx.math.Vector2;

/**
 * Class used by GSON to read circuit spawn info from the track json file.
 * Circuit spawns are all of the start positions on the track grid.
 */
public class CircuitSpawn {

    /**
     * The circuit point index the spawn points start at.
     */
    public int startIndex;

    /**
     * Position and tangents of all spawns for this track.
     */
    public Spawn[] spawns;

    public class Spawn {
        public Vector2 position;
        public Vector2 tangent;
    }
}
