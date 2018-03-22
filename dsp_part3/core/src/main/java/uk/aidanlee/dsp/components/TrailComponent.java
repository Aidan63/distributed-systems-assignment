package uk.aidanlee.dsp.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import uk.aidanlee.dsp.common.structural.ec.Component;
import uk.aidanlee.dsp.utils.Vector2Tools;

import java.util.LinkedList;
import java.util.List;

import static uk.aidanlee.dsp.utils.Vector2Tools.multiply;
import static uk.aidanlee.dsp.utils.Vector2Tools.subtract;
import static uk.aidanlee.dsp.utils.Vector2Tools.tangent2D;

public class TrailComponent extends Component {
    public List<Vector2> points;
    public Mesh mesh;

    private int   maxLength;
    private float startSize;
    private float endSize;
    private Color color;

    public TrailComponent(String _name, Color _color) {
        super(_name);

        maxLength = 10;
        startSize = 4;
        endSize   = 4;

        color  = _color;
        mesh   = new Mesh(false, maxLength * 2, 0,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 4, "a_color"));
        points = new LinkedList<>();
    }

    @Override
    public void onadded() {
        points.add(entity.pos.cpy().add(entity.origin));
    }

    @Override
    public void update(float _dt) {
        points.add(entity.pos.cpy().add(entity.origin));

        if (points.size() > maxLength) {
            points.remove(0);
        }

        updateGeometry();
    }

    private void updateGeometry() {
        float[] verts = new float[(maxLength * 2) * 6];
        int index = 0;

        for (int i = 0; i < points.size(); i++) {
            if (i < points.size() - 1) {
                Vector2 tangent = tangent2D(subtract(points.get(i), points.get(i + 1)).nor());
                float   offset  = MathUtils.lerp(startSize, endSize, i / points.size());

                Vector2 pointNegative = Vector2Tools.add(points.get(i), multiply(tangent, -1 * offset));
                Vector2 pointPositive = Vector2Tools.add(points.get(i), multiply(tangent, offset));

                verts[index++] = pointNegative.x;
                verts[index++] = pointNegative.y;

                verts[index++] = color.r;
                verts[index++] = color.g;
                verts[index++] = color.b;
                verts[index++] = i * 0.1f;

                //

                verts[index++] = pointPositive.x;
                verts[index++] = pointPositive.y;

                verts[index++] = color.r;
                verts[index++] = color.g;
                verts[index++] = color.b;
                verts[index++] = i * 0.1f;

                continue;
            }

            Vector2 tangent = tangent2D(subtract(points.get(i - 1), points.get(i)).nor());
            float   offset  = MathUtils.lerp(startSize, endSize, i / points.size());

            Vector2 pointNegative = Vector2Tools.add(points.get(i), multiply(tangent, -1 * offset));
            Vector2 pointPositive = Vector2Tools.add(points.get(i), multiply(tangent, offset));

            verts[index++] = pointNegative.x;
            verts[index++] = pointNegative.y;

            verts[index++] = color.r;
            verts[index++] = color.g;
            verts[index++] = color.b;
            verts[index++] = i * 0.1f;

            //

            verts[index++] = pointPositive.x;
            verts[index++] = pointPositive.y;

            verts[index++] = color.r;
            verts[index++] = color.g;
            verts[index++] = color.b;
            verts[index++] = i * 0.1f;
        }

        mesh.setVertices(verts);
    }
}
