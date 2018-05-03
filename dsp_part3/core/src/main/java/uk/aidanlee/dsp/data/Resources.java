package uk.aidanlee.dsp.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/**
 * Loads and stores references to resources used by the client.
 */
public class Resources {
    public final TextureAtlas trackAtlas;
    public final TextureAtlas craftAtlas;

    public final Texture trackTexture;
    public final Texture craftTexture;

    public final Texture checkbox;

    public final BitmapFont helvetica48;
    public final BitmapFont helvetica19;
    public final Texture hudElements;

    public Resources() {
        trackAtlas = new TextureAtlas(Gdx.files.internal("textures/track.atlas"));
        craftAtlas = new TextureAtlas(Gdx.files.internal("textures/craft.atlas"));
        trackTexture = trackAtlas.getTextures().first();
        craftTexture = craftAtlas.getTextures().first();

        checkbox = new Texture(Gdx.files.internal("textures/checkmark.png"));

        helvetica48 = new BitmapFont(Gdx.files.internal("fonts/helv48_bold.fnt"), true);
        helvetica19 = new BitmapFont(Gdx.files.internal("fonts/helv19.fnt"), true);
        hudElements = new Texture(Gdx.files.internal("ui/hud.png"));
    }

    public void dispose() {
        trackAtlas.dispose();
        craftAtlas.dispose();

        checkbox.dispose();

        helvetica48.dispose();
        helvetica19.dispose();
        hudElements.dispose();
    }
}
