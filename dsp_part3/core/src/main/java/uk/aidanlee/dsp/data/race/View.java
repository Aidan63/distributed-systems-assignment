package uk.aidanlee.dsp.data.race;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Manages the view and camera for the client.
 */
public class View {

    /**
     * The camera used to determine what the client sees.
     */
    private OrthographicCamera camera;

    /**
     * Viewport which describes how the camera is scaled when the window is resized.
     */
    private Viewport viewport;

    public View() {
        camera = new OrthographicCamera();
        camera.setToOrtho(true);
        camera.zoom = 1.2f;

        viewport = new ExtendViewport(1920, 1080, camera);
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public void resize() {
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport.setScreenPosition(0, 0);
        viewport.apply();
    }
}
