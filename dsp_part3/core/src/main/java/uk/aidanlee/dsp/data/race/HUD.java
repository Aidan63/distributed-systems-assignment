package uk.aidanlee.dsp.data.race;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import uk.aidanlee.dsp.common.components.StatsComponent;
import uk.aidanlee.dsp.common.structural.ec.Entity;
import uk.aidanlee.dsp.data.Resources;

public class HUD {

    private final Resources resources;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch batch;

    //

    private final TextureRegion uiArrow;
    private final NinePatchDrawable lapBox;

    //

    private float totalTime;
    private int currentLap;

    public HUD(Resources _resources) {
        resources = _resources;
        batch     = new SpriteBatch();
        camera    = new OrthographicCamera();
        camera.setToOrtho(true);
        viewport  = new ExtendViewport(1920, 1080, camera);

        TextureRegion patchSprite = new TextureRegion(resources.hudElements, 0,  0, 40, 40);

        uiArrow = new TextureRegion(resources.hudElements, 0, 40, 40, 40);
        lapBox  = new NinePatchDrawable(new NinePatch(patchSprite, 12, 12, 12, 12));
    }

    public void resize() {
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    public void render(Entity _entity) {

        // Get the stats component for the local player.
        StatsComponent stats = (StatsComponent) _entity.get("stats");

        viewport.apply();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.enableBlending();

        // Draw semi transparent elements
        batch.setColor(0.84f, 0.84f, 0.84f, 0.5f);

        // Speed bar
        lapBox.draw(batch, 1060, 960, 820, 80);

        // Lap time backgrounds
        lapBox.draw(batch, 60  , 900, 332, 40);
        lapBox.draw(batch, 432 , 900, 332, 40);
        lapBox.draw(batch, 804 , 900, 332, 40);
        lapBox.draw(batch, 1176, 900, 332, 40);
        lapBox.draw(batch, 1548, 900, 332, 40);

        // Draw solid grey speed bar.
        batch.setColor(0.84f, 0.84f, 0.84f, 1.0f);
        lapBox.draw(batch, 1100, 960, (stats.engineSpeed / stats.maxSpeed) * 780, 80);

        // Draw solid blue elements.
        batch.setColor(0.16f, 0.59f, 1, 1);

        // Draw lap, total time, and speed box elements.
        lapBox.draw(batch, 40  , 40 , 120, 80);
        lapBox.draw(batch, 40  , 960, 280, 80);
        lapBox.draw(batch, 1000, 960, 120, 80);

        // Lap time markers
        lapBox.draw(batch, 40  , 900, 40, 40);
        lapBox.draw(batch, 412 , 900, 40, 40);
        lapBox.draw(batch, 784 , 900, 40, 40);
        lapBox.draw(batch, 1156, 900, 40, 40);
        lapBox.draw(batch, 1528, 900, 40, 40);

        // Draw ui arrow motifs
        batch.draw(uiArrow, 40, 120, 20, 20, 40, 40, 1, 1, 270);
        batch.draw(uiArrow, 40, 860, 20, 20, 40, 40, 1, 1, 90);
        batch.draw(uiArrow, 320, 960, 20, 20, 40, 40, 1, 1, 180);
        batch.draw(uiArrow, 960, 960);

        // Draw Text
        resources.helvetica19.setColor(0.16f, 0.59f, 1, 1);

        // Draw motif text
        resources.helvetica19.draw(batch, "laps", 80, 134, 0, Align.topLeft, false);
        resources.helvetica19.draw(batch, "total time", 366, 971, 0, Align.topLeft, false);
        resources.helvetica19.draw(batch, "mph", 922, 971, 0, Align.topLeft, false);
        resources.helvetica19.draw(batch, "lap times", 80, 871, 0, Align.topLeft, false);

        // Draw time, speed, and current lap counter.
        resources.helvetica48.draw(batch, "00:00:00", 180 , 982, 0, Align.center, true);
        resources.helvetica48.draw(batch, String.valueOf(Math.round(stats.engineSpeed * 7)), 1060, 982, 0, Align.center, true);

        batch.disableBlending();
        batch.end();
    }
}
