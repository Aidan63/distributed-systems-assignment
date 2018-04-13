package uk.aidanlee.dsp;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.TimeUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ClientRunner extends ApplicationAdapter {

    /**
     *
     */
    private double accumulator;

    /**
     *
     */
    private double currentTime;

    /**
     *
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