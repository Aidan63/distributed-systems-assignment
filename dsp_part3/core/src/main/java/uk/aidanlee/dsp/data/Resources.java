package uk.aidanlee.dsp.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class Resources {
    public final TextureAtlas trackAtlas;
    public final TextureAtlas craftAtlas;

    public final Texture trackTexture;
    public final Texture craftTexture;

    public final Texture checkbox;

    public Resources() {
        trackAtlas = new TextureAtlas(Gdx.files.internal("assets/textures/track.atlas"));
        craftAtlas = new TextureAtlas(Gdx.files.internal("assets/textures/craft.atlas"));

        trackTexture = trackAtlas.getTextures().first();
        craftTexture = craftAtlas.getTextures().first();

        checkbox = new Texture(Gdx.files.internal("assets/textures/checkmark.png"));
    }

    public void dispose() {
        trackAtlas.dispose();
        craftAtlas.dispose();
        checkbox.dispose();
    }
}
