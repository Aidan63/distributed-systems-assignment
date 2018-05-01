package uk.aidanlee.dsp_assignment.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class Resources {

    public final TextureAtlas craftAtlas;
    public final TextureAtlas trackAtlas;

    public final BitmapFont helvetica48;
    public final BitmapFont helvetica19;
    public final Texture hudElements;

    public Resources() {
        craftAtlas = new TextureAtlas(Gdx.files.internal("textures/craft.atlas"));
        trackAtlas = new TextureAtlas(Gdx.files.internal("textures/track.atlas"));

        helvetica48 = new BitmapFont(Gdx.files.internal("fonts/helv48_bold.fnt"), true);
        helvetica19 = new BitmapFont(Gdx.files.internal("fonts/helv19.fnt"), true);
        hudElements = new Texture(Gdx.files.internal("ui/hud.png"));
    }
}
