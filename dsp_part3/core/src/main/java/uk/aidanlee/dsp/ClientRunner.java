package uk.aidanlee.dsp;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * Client runner, extends a base LibGDX application and calls the clients update function 60 times per second (60fps)
 * The render function is drawn as fast as the computer allows.
 */
public class ClientRunner extends ApplicationAdapter {

    /**
     * Constantly increasing timer, increases by the time difference between the current and previous loop call.
     */
    private double accumulator;

    /**
     * The current system time.
     */
    private double currentTime;

    /**
     * Actual client class containing actual game code.
     */
    private Client client;

    /**
     * Ran once LibGDX has started up.
     */
    @Override
    public void create() {

        client = new Client();
        accumulator = 0;
        currentTime = 0;
    }

    /**
     * LibGDX game loop, called as fast as possible.
     */
    @Override
    public void render() {

        // game fixed time-step loop.
        double newTime   = TimeUtils.millis() / 1000.0;
        double frameTime = Math.min(newTime - currentTime, 0.25);

        currentTime = newTime;
        accumulator += frameTime;

        float step = 1.0f / 60.0f;
        while (accumulator >= step) {
            accumulator -= step;

            client.onUpdate(step);
        }

        client.onRender();
    }

    /**
     * Called when LibGDX closes.
     */
    @Override
    public void dispose() {
        client.dispose();
    }
}