package uk.aidanlee.dsp.common.data.circuit;

import com.badlogic.gdx.math.Vector2;

public class CircuitSpawn {
    public int startIndex;
    public Spawn[] spawns;

    public class Spawn {
        public Vector2 position;
        public Vector2 tangent;
    }
}
