package uk.aidanlee.dsp_assignment.race;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class Resources {
    public TextureAtlas trackAtlas;
    public TextureAtlas craftAtlas;

    public Texture trackTexture;
    public Texture craftTexture;

    public Resources() {
        trackAtlas = new TextureAtlas(Gdx.files.internal("assets/textures/track.atlas"));
        craftAtlas = new TextureAtlas(Gdx.files.internal("assets/textures/craft.atlas"));

        trackTexture = trackAtlas.getTextures().first();
        craftTexture = craftAtlas.getTextures().first();
    }

    public void dispose() {
        trackAtlas.dispose();
        craftAtlas.dispose();
    }
}
