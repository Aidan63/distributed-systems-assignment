package uk.aidanlee.dsp_assignment.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuadMesh {
    private Mesh geometry;
    private Map<UUID, PackedQuad> quads;
    private int width;
    private int height;

    public QuadMesh(boolean _static, int _noOfTiles, int _textureWidth, int _textureHeight) {
        quads    = new HashMap<>();
        width    = _textureWidth;
        height   = _textureHeight;
        geometry = new Mesh(true, (_noOfTiles * 6), 0, VertexAttribute.Position(), VertexAttribute.ColorUnpacked(), VertexAttribute.TexCoords(0));
    }

    public UUID tileAdd(Vector2 _p1, Vector2 _p2, Vector2 _p3, Vector2 _p4, boolean _flipx, boolean _flipy, Rectangle _uv) {
        PackedQuad quad = new PackedQuad();
        quad.uid   = UUID.randomUUID();
        quad.verts = new Vector2[] { _p1, _p2, _p3, _p4, _p1, _p3 };
        quad.flipx = _flipx;
        quad.flipy = _flipy;
        quad.color = Color.WHITE;
        quad.uv    = new Vector2[] { new Vector2(), new Vector2(), new Vector2(), new Vector2(), new Vector2(), new Vector2() };
        quads.put(quad.uid, quad);

        tileUV(quad, _uv);

        return quad.uid;
    }

    public void rebuild() {
        int noOfVerts = quads.values().size() * 6;
        float[] verts = new float[noOfVerts * 9];

        int index = 0;
        for (PackedQuad quad : quads.values()) {
            for (int i = 0; i < quad.verts.length; i++) {
                verts[index++] = quad.verts[i].x;
                verts[index++] = quad.verts[i].y;
                verts[index++] = 0;

                verts[index++] = 1;
                verts[index++] = 1;
                verts[index++] = 1;
                verts[index++] = 1;

                verts[index++] = quad.uv[i].x;
                verts[index++] = quad.uv[i].y;
            }
        }

        geometry.setVertices(verts);
    }

    public void render(ShaderProgram _shader, int _primitive) {
        geometry.render(_shader, _primitive);
    }

    private void tileUV(PackedQuad _quad, Rectangle _uv) {
        float tlx = _uv.x / width;
        float tly = _uv.y / height;
        float szx = _uv.width  / width;
        float szy = _uv.height / height;

        tileUvSpace(_quad, new Rectangle(tlx, tly, szx, szy));
    }

    private void tileUvSpace(PackedQuad _quad, Rectangle _uv) {
        float sz_x = _uv.width;
        float sz_y = _uv.height;

        float tl_x = _uv.x;
        float tl_y = _uv.y;

        //tr
        float tr_x = tl_x + sz_x;
        float tr_y = tl_y;
        //br
        float br_x = tl_x + sz_x;
        float br_y = tl_y + sz_y;
        //bl
        float bl_x = tl_x;
        float bl_y = tl_y + sz_y;

        float tmp_x = 0;
        float tmp_y = 0;

        //flipped y swaps tl and tr with bl and br, only on y
        if(_quad.flipy) {

            //swap tl and bl
            tmp_y = bl_y;
            bl_y = tl_y;
            tl_y = tmp_y;

            //swap tr and br
            tmp_y = br_y;
            br_y = tr_y;
            tr_y = tmp_y;

        } //flipy

        //flipped x swaps tl and bl with tr and br, only on x
        if(_quad.flipx) {

            //swap tl and tr
            tmp_x = tr_x;
            tr_x = tl_x;
            tl_x = tmp_x;

            //swap bl and br
            tmp_x = br_x;
            br_x = bl_x;
            bl_x = tmp_x;

        } //flipx

        _quad.uv[0].set( tl_x , tl_y );
        _quad.uv[1].set( tr_x , tr_y );
        _quad.uv[2].set( br_x , br_y );

        _quad.uv[3].set( bl_x , bl_y );
        _quad.uv[4].set( tl_x , tl_y );
        _quad.uv[5].set( br_x , br_y );
    }

    private class PackedQuad {
        public UUID uid;
        public Vector2[] verts;
        public Color color;
        public boolean flipx;
        public boolean flipy;
        public Vector2[] uv;
    }
}
