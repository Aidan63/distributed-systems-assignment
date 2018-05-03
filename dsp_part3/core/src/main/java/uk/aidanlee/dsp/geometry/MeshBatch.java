package uk.aidanlee.dsp.geometry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows drawing multiple meshes with a single texture.
 */
public class MeshBatch {

    /**
     * The texture to draw all meshes with.
     */
    private Texture texture;

    /**
     * The current active shader to draw with.
     */
    private ShaderProgram shader;

    /**
     * Map of all shaders usable by this batcher.
     */
    private Map<String, ShaderProgram> shaders;

    public MeshBatch(Texture _texture) {
        texture = _texture;
        shaders = new HashMap<>();
    }

    // Public API

    public void begin() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        texture.bind();
    }

    public void addShader(String _shaderName, ShaderProgram _shader) {
        shaders.put(_shaderName, _shader);
    }

    public void setShader(String _shaderName, Matrix4 _projection) {
        if (shader != null) shader.end();

        shader = shaders.get(_shaderName);
        shader.begin();
        shader.setUniformMatrix("u_projTrans", _projection);
    }

    public void draw(Mesh _mesh, int _geomType) {
        _mesh.render(shader, _geomType);
    }

    public void end() {
        shader.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void dispose() {
        for (ShaderProgram s : shaders.values()) {
            s.dispose();
        }
    }
}
