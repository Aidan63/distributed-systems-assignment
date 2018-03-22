package uk.aidanlee.dsp.geometry;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuadMesh {
    /**
     * Mesh instance of this quad mesh.
     */
    private Mesh geometry;

    /**
     * Map of all the quads keyed by a uuid attached to each one.
     */
    private Map<UUID, PackedQuad> quads;

    /**
     * Texture atlas for texturing the mesh.
     */
    private TextureAtlas atlas;

    /**
     * Texture used for setting the packed quad UVs.
     */
    private Texture texture;

    /**
     * Creates a new quad mesh geometry.
     * @param _static    If the mesh will be updated after created. (Used to provided optimisation hints to the renderer.)
     * @param _noOfTiles The number of quads in the mesh.
     * @param _atlas     The texture atlas used to texture the mesh.
     */
    public QuadMesh(boolean _static, int _noOfTiles, TextureAtlas _atlas) {
        quads    = new HashMap<>();
        geometry = new Mesh(_static, (_noOfTiles * 6), 0, VertexAttribute.Position(), VertexAttribute.ColorUnpacked(), VertexAttribute.TexCoords(0));

        atlas    = _atlas;
        texture  = _atlas.getTextures().first();
    }

    /**
     * Gets the LibGDX mesh object of the quad mesh.
     * @return libGDX mesh.
     */
    public Mesh getMesh() {
        return geometry;
    }

    /**
     * Frees resources used by the mesh.
     */
    public void dispose() {
        geometry.dispose();
    }

    /**
     * Adds a new quad to the geometry. UV data is obtained from the atlas region with the provided name.
     * @param _name  The atlas region to get UV data from.
     * @param _p1    Bottom left point of the quad.
     * @param _p2    Top left point of the quad.
     * @param _p3    Top right point of the quad.
     * @param _p4    Bottom right point of the quad.
     * @param _flipx If the quad texture is flipped on the x-axis.
     * @param _flipy If the quad texture is flipped on the y-axis.
     * @return UUID of the created quad.
     */
    public UUID addQuad(String _name, Vector2 _p1, Vector2 _p2, Vector2 _p3, Vector2 _p4, Color _color, boolean _flipx, boolean _flipy) {
        TextureAtlas.AtlasRegion region = atlas.findRegion(_name);

        PackedQuad quad = new PackedQuad();
        quad.uid   = UUID.randomUUID();
        quad.verts = new Vector2[] { _p1, _p2, _p3, _p4, _p1, _p3 };
        quad.flipx = _flipx;
        quad.flipy = _flipy;
        quad.color = _color.cpy();
        quad.uv    = new Vector2[] { new Vector2(), new Vector2(), new Vector2(), new Vector2(), new Vector2(), new Vector2() };
        quads.put(quad.uid, quad);

        tileUV(quad, new Rectangle(region.getRegionX(), region.getRegionY(), region.getRegionWidth(), region.getRegionHeight()));

        return quad.uid;
    }

    /**
     * Sets the colour of a packed quad.
     * @param _quadID The quad UUID
     * @param _color The new colour of the quad.
     */
    public void colorQuad(UUID _quadID, Color _color) {
        PackedQuad quad = quads.get(_quadID);
        quad.color.set(_color);
    }

    /**
     * Calculate and submit the vertices to the mesh.
     */
    public void rebuild() {
        int noOfVerts = quads.values().size() * 6;
        float[] verts = new float[noOfVerts * 9];

        int index = 0;
        for (PackedQuad quad : quads.values()) {
            for (int i = 0; i < quad.verts.length; i++) {
                verts[index++] = quad.verts[i].x;
                verts[index++] = quad.verts[i].y;
                verts[index++] = 0;

                verts[index++] = quad.color.r;
                verts[index++] = quad.color.g;
                verts[index++] = quad.color.b;
                verts[index++] = quad.color.a;

                verts[index++] = quad.uv[i].x;
                verts[index++] = quad.uv[i].y;
            }
        }

        geometry.setVertices(verts);
    }

    /**
     * Draws the triangles for each packed quad.
     * @param _renderer ShapeRenderer to draw with.
     */
    public void debugRender(ShapeRenderer _renderer) {
        for (PackedQuad quad : quads.values()) {
            _renderer.polygon(new float[] {
                    quad.verts[0].x, quad.verts[0].y,
                    quad.verts[1].x, quad.verts[1].y,
                    quad.verts[2].x, quad.verts[2].y,
                    quad.verts[3].x, quad.verts[3].y,
                    quad.verts[4].x, quad.verts[4].y,
                    quad.verts[5].x, quad.verts[5].y
            });
        }
    }

    /**
     * Create normalized UV coordinates and apply them to the quad.
     * @param _quad The quad to modify.
     * @param _uv   Un-normalized texture coordinates on the atlas.
     */
    private void tileUV(PackedQuad _quad, Rectangle _uv) {
        float tlx = _uv.x / texture.getWidth();
        float tly = _uv.y / texture.getHeight();
        float szx = _uv.width  / texture.getWidth();
        float szy = _uv.height / texture.getHeight();

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

    public class PackedQuad {
        public Vector2[] verts;

        UUID uid;
        Color color;

        private boolean flipx;
        private boolean flipy;
        private Vector2[] uv;
    }
}