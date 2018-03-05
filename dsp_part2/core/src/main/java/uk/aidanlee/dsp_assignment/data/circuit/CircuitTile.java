package uk.aidanlee.dsp_assignment.data.circuit;

import com.badlogic.gdx.math.Vector2;

public class CircuitTile {
    public String id;
    public Vector2[] verts;
    public String frame;
    public String[] tags;

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (Vector2 v : verts) {
            buf.append(" - " + v);
        }

        return id + buf.toString();
    }
}
