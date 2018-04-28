package uk.aidanlee.dsp.data.race;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.google.common.eventbus.Subscribe;
import glm_.vec2.Vec2;
import imgui.Cond;
import imgui.ImGui;
import imgui.WindowFlags;
import uk.aidanlee.dsp.common.components.StatsComponent;
import uk.aidanlee.dsp.common.components.craft.LapTimer;
import uk.aidanlee.dsp.common.data.events.EvLapTime;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.structural.ec.Entity;
import uk.aidanlee.dsp.data.Resources;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * HUD class displays game data for the player on the screen during the actual game.
 * It displays the countdown, current lap, lap time, and speed data, and the end race total and lap times.
 */
public class HUD {

    /**
     * Resources instance which holds bitmap fonts and UI textures.
     */
    private final Resources resources;

    /**
     * Craft entity array.
     */
    private final Player[] players;

    /**
     * Orthographic camera for the HUD.
     */
    private final OrthographicCamera camera;

    /**
     * Viewport for the HUD.
     */
    private final Viewport viewport;

    /**
     * Batcher for drawing all of the HUD elements and fonts.
     */
    private final SpriteBatch batch;

    /**
     * Texture region of the arrow image.
     */
    private final TextureRegion uiArrow;

    /**
     * Texture region for the nine slice box image.
     */
    private final NinePatchDrawable lapBox;

    //

    /**
     * Current state of the HUD.
     */
    private HudState state;

    /**
     * Int value of the current countdown number.
     */
    private int countdownValue;

    /**
     * The ship entity to draw information about.
     */
    private Entity entity;

    /**
     * The total time of the ship.
     */
    private float totalTime;

    /**
     * The current lap of the ship.
     */
    private int currentLap;

    /**
     * The final time results from the server.
     */
    private Map<Integer, List<Float>> timesData;

    /**
     * Creates a new HUD for the game client.
     * @param _resources Resource instance.
     * @param _players   Player structure for getting player names.
     */
    public HUD(Resources _resources, Player[] _players) {
        players   = _players;
        resources = _resources;

        batch     = new SpriteBatch();
        camera    = new OrthographicCamera();
        camera.setToOrtho(true);
        viewport  = new ExtendViewport(1920, 1080, camera);

        TextureRegion patchSprite = new TextureRegion(resources.hudElements, 0,  0, 40, 40);

        uiArrow = new TextureRegion(resources.hudElements, 0, 40, 40, 40);
        lapBox  = new NinePatchDrawable(new NinePatch(patchSprite, 12, 12, 12, 12));

        // Initial HUD state.
        state = HudState.Countdown;

        // Setup countdown variables
        countdownValue = 3;

        // Setup race variables
        totalTime  = 0;
        currentLap = 1;

        // Setup results variables
    }

    /**
     * Starts a countdown timer.
     */
    public void showCountdown() {
        state = HudState.Countdown;

        // Create a series of three times which will decrement a int value.
        Timer.Task task = new Timer.Task() {
            @Override
            public void run() {
                countdownValue --;

                if (countdownValue > 0) {
                    Timer.schedule(this, 1);
                }
            }
        };

        // Start the timer series.
        Timer.schedule(task, 1);
    }

    /**
     * Show the in game HUD.
     * @param _entity The ship entity we want to get HUD data from.
     */
    public void showRace(Entity _entity) {
        state = HudState.InRace;

        entity = _entity;
        entity.getEvents().register(this);
    }

    /**
     * Show the race results.
     */
    public void showResults(Map<Integer, List<Float>> _times) {
        state     = HudState.Results;
        timesData = _times;
    }

    /**
     * Draws the HUD.
     */
    public void render() {

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        switch (state) {
            case Countdown: drawCountdown(); break;
            case InRace   : drawRace(); break;
            case Results  : drawResults(); break;
        }
    }

    /**
     * Listens to the ships on lap event.
     * Increments the current lap when called.
     * @param _lap Lap event instance.
     */
    @Subscribe
    public void onCraftLap(EvLapTime _lap) {
        currentLap++;
    }

    /**
     *
     */
    private void drawCountdown() {
        viewport.apply();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.enableBlending();

        resources.helvetica48.draw(batch, String.valueOf(countdownValue), Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 4, 0, Align.center, true);

        batch.disableBlending();
        batch.end();
    }

    /**
     *
     */
    private void drawRace() {
        // Get the stats component for the local player.
        StatsComponent stats = (StatsComponent) entity.get("stats");

        // Update the total time
        if (entity.has("lap_timer")) {
            LapTimer timer = (LapTimer) entity.get("lap_timer");
            totalTime += (timer.time - totalTime);
        }

        viewport.apply();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.enableBlending();

        // Draw semi transparent elements
        batch.setColor(0.84f, 0.84f, 0.84f, 0.5f);

        // Speed bar
        lapBox.draw(batch, 1060, 960, 820, 80);

        // Draw solid grey speed bar.
        batch.setColor(0.84f, 0.84f, 0.84f, 1.0f);
        lapBox.draw(batch, 1100, 960, (stats.engineSpeed / stats.maxSpeed) * 780, 80);

        // Draw solid blue elements.
        batch.setColor(0.16f, 0.59f, 1, 1);

        // Draw lap, total time, and speed box elements.
        lapBox.draw(batch, 40  , 40 , 120, 80);
        lapBox.draw(batch, 40  , 960, 280, 80);
        lapBox.draw(batch, 1000, 960, 120, 80);

        // Draw ui arrow motifs
        batch.draw(uiArrow, 40, 120, 20, 20, 40, 40, 1, 1, 270);
        batch.draw(uiArrow, 320, 960, 20, 20, 40, 40, 1, 1, 180);
        batch.draw(uiArrow, 960, 960);

        // Draw Text
        resources.helvetica19.setColor(0.16f, 0.59f, 1, 1);

        // Draw motif text
        resources.helvetica19.draw(batch, "lap", 80, 134, 0, Align.topLeft, false);
        resources.helvetica19.draw(batch, "lap time", 366, 971, 0, Align.topLeft, false);
        resources.helvetica19.draw(batch, "mph", 922, 971, 0, Align.topLeft, false);

        // Draw time, speed, and current lap counter.
        resources.helvetica48.draw(batch, formatTime(totalTime), 180 , 982, 0, Align.center, true);
        resources.helvetica48.draw(batch, String.valueOf(Math.round(stats.engineSpeed * 7)), 1060, 982, 0, Align.center, true);
        resources.helvetica48.draw(batch, String.valueOf(currentLap), 100, 62, 0, Align.center, true);

        batch.disableBlending();
        batch.end();
    }

    /**
     *
     */
    private void drawResults() {
        ImGui.INSTANCE.setNextWindowPos(new Vec2((Gdx.graphics.getWidth() / 2) - 300, (Gdx.graphics.getHeight() / 2) - 200), Cond.Always, new Vec2());
        ImGui.INSTANCE.setNextWindowSize(new Vec2(600, 400), Cond.Always);
        ImGui.INSTANCE.begin("Results", null, WindowFlags.NoResize.getI());

        for (Map.Entry<Integer, List<Float>> entry : timesData.entrySet()) {
            float timeSum = 0;
            for (float t : entry.getValue()) {
                timeSum += t;
            }

            // Create a collapsible header with all of that players times.
            // The header text contains the player name and total time, label under the header is a lap time.
            ImGui.INSTANCE.pushId(entry.getKey());
            if (ImGui.INSTANCE.collapsingHeader(players[entry.getKey()].getName() + " : " + formatTime(timeSum), 0)) {
                for (float t : entry.getValue()) {
                    ImGui.INSTANCE.text(formatTime(t));
                }
            }
            ImGui.INSTANCE.popId();
        }

        ImGui.INSTANCE.end();
    }

    /**
     * Returns a "--:--:--" formatted time string of the provided time.
     * @param _time Number of seconds
     * @return Formatted time string.
     */
    private String formatTime(float _time) {
        Date date = new Date((long) (_time * 1000));
        String formatted = new SimpleDateFormat("mm:ss:SS").format(date);
        return formatted.substring(0, Math.min(formatted.length(), 8));
    }

    /**
     *
     */
    private enum HudState {
        Countdown,
        InRace,
        Results;
    }
}
