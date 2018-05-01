package uk.aidanlee.dsp_assignment.data;

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
import uk.aidanlee.dsp_assignment.components.craft.StatsComponent;
import uk.aidanlee.dsp_assignment.structural.ec.Entity;

import java.text.SimpleDateFormat;
import java.util.*;

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
     * The current lap of the ship.
     */
    private int currentLap;

    /**
     * Ordered time data from the server.
     */
    private List<PlayerTimes> timesData;

    /**
     * Creates a new HUD for the game client.
     *
     * @param _resources Resource instance.
     */
    public HUD(Resources _resources) {
        resources = _resources;

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(true);
        viewport = new ExtendViewport(1920, 1080, camera);

        TextureRegion patchSprite = new TextureRegion(resources.hudElements, 0, 0, 40, 40);

        uiArrow = new TextureRegion(resources.hudElements, 0, 40, 40, 40);
        lapBox = new NinePatchDrawable(new NinePatch(patchSprite, 12, 12, 12, 12));

        // Initial HUD state.
        state = HudState.Countdown;

        // Setup countdown variables
        countdownValue = 3;

        // Setup race variables
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
                countdownValue--;

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
     * Display waiting for other players text.
     */
    public void showWaiting() {
        state = HudState.Waiting;
    }

    /**
     * Show the race results. Sorts time data received from the server.
     *
     * @param _times Time data to sort.
     */
    public void showResults(Map<Integer, List<Float>> _times) {
        state = HudState.Results;
        timesData = new ArrayList<>();
        for (Map.Entry<Integer, List<Float>> entry : _times.entrySet()) {
            timesData.add(new PlayerTimes(entry.getKey(), entry.getValue()));
        }

        timesData.sort((_t1, _t2) -> Float.compare(_t1.totalTime, _t2.totalTime));
    }

    public void resize(int _x, int _y, int _width, int _height) {
        //System.out.println(_x + ":" + _y + "    " + _width + ":" + _height);

        viewport.update(_width, _height, true);
        viewport.setScreenPosition(_x, _y);
        viewport.apply();
    }

    /**
     * Draws the HUD.
     */
    public void render() {

        switch (state) {
            case Countdown:
                drawCountdown();
                break;
            case InRace:
                drawRace();
                break;
            case Waiting:
                drawWaiting();
                break;
            case Results:
                drawResults();
                break;
        }
    }

    /**
     * Listens to the ships on lap event.
     * Increments the current lap when called.
     *
     * @param _lap Lap event instance.
     */
    /*
    @Subscribe
    public void onCraftLap(EvLapTime _lap) {
        currentLap++;
    }
    */

    /**
     * Draws centered, large countdown text.
     */
    private void drawCountdown() {
        viewport.apply();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.enableBlending();

        resources.helvetica48.draw(batch, String.valueOf(countdownValue), 960, 270, 0, Align.center, true);

        batch.disableBlending();
        batch.end();
    }

    /**
     * Draws the entire in game HUD.
     * Current lap number, current lap time, and speed bar are drawn.
     */
    private void drawRace() {
        // If the entity does not have the required components, exit early.
        if (!entity.has("stats")) return;

        // Cast / get the stats component and time to draw.
        StatsComponent stats = (StatsComponent) entity.get("stats");
        String displayTime = "--:--:--"; //(entity.has("lap_timer")) ? formatTime(((LapTimer) entity.get("lap_timer")).time) : "--:--:--";

        // Apply viewport and batch projection settings.
        viewport.apply();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.enableBlending();

        // Draw semi transparent elements
        batch.setColor(0.84f, 0.84f, 0.84f, 0.5f);

        // Speed bar
        lapBox.draw(batch, viewport.getWorldWidth() - 860, viewport.getWorldHeight() - 120, 820, 80);

        // Draw solid grey speed bar.
        batch.setColor(0.84f, 0.84f, 0.84f, 1.0f);
        lapBox.draw(batch, viewport.getWorldWidth() - 820, viewport.getWorldHeight() - 120, (stats.engineSpeed / stats.maxSpeed) * 780, 80);

        // Draw solid blue elements.
        batch.setColor(0.16f, 0.59f, 1, 1);

        // Draw lap, total time, and speed box elements.
        lapBox.draw(batch, 40, 40, 120, 80);
        lapBox.draw(batch, 40, viewport.getWorldHeight() - 120, 280, 80);
        lapBox.draw(batch, viewport.getWorldWidth() - 920, viewport.getWorldHeight() - 120, 120, 80);

        // Draw ui arrow motifs
        batch.draw(uiArrow, 40, 120, 20, 20, 40, 40, 1, 1, 270);
        batch.draw(uiArrow, 320, viewport.getWorldHeight() - 120, 20, 20, 40, 40, 1, 1, 180);
        batch.draw(uiArrow, viewport.getWorldWidth() - 960, viewport.getWorldHeight() - 120);

        // Draw Text
        resources.helvetica19.setColor(0.16f, 0.59f, 1, 1);

        // Draw motif text
        resources.helvetica19.draw(batch, "lap", 80, 134, 0, Align.topLeft, false);
        resources.helvetica19.draw(batch, "lap time", 366, viewport.getWorldHeight() - 109, 0, Align.topLeft, false);
        resources.helvetica19.draw(batch, "mph", viewport.getWorldWidth() - 998, viewport.getWorldHeight() - 109, 0, Align.topLeft, false);

        // Draw time, speed, and current lap counter.
        resources.helvetica48.draw(batch, displayTime, 180, viewport.getWorldHeight() - 98, 0, Align.center, true);
        resources.helvetica48.draw(batch, String.valueOf(Math.round(stats.engineSpeed * 7)), viewport.getWorldWidth() - 860, viewport.getWorldHeight() - 98, 0, Align.center, true);
        resources.helvetica48.draw(batch, String.valueOf(currentLap), 100, 62, 0, Align.center, true);

        batch.disableBlending();
        batch.end();
    }

    /**
     * Draw race complete and waiting text.
     */
    private void drawWaiting() {
        viewport.apply();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.enableBlending();

        resources.helvetica48.draw(
                batch,
                "Race Complete!\nWaiting for all other players to finish",
                960,
                270,
                0,
                Align.center,
                true
        );

        batch.disableBlending();
        batch.end();
    }

    /**
     * Draws an ImGui with each players total and lap times.
     */
    private void drawResults() {
        //
    }

    /**
     * Returns a "--:--:--" formatted time string of the provided time.
     *
     * @param _time Number of seconds
     * @return Formatted time string.
     */
    private String formatTime(float _time) {
        Date date = new Date((long) (_time * 1000));
        String formatted = new SimpleDateFormat("mm:ss:SS").format(date);
        return formatted.substring(0, Math.min(formatted.length(), 8));
    }

    /**
     * Simple class which holds all time data on a particular client.
     */
    private class PlayerTimes {
        /**
         * The clientID.
         */
        final int clientID;

        /**
         * The total time for this race.
         */
        final float totalTime;

        /**
         * All individual lap times.
         */
        final List<Float> lapTimes;

        private PlayerTimes(int _id, List<Float> _times) {
            clientID = _id;
            lapTimes = _times;
            totalTime = _times.stream().mapToInt(Float::intValue).sum();
        }
    }

    /**
     * Four possible states of the hud.
     */
    private enum HudState {
        Countdown,
        InRace,
        Waiting,
        Results
    }
}