package uk.aidanlee.dsp.data.race;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class View {
    private OrthographicCamera camera;
    private Viewport viewport;

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public void setup() {
        camera = new OrthographicCamera();
        camera.setToOrtho(true);
        camera.zoom = 1.2f;

        viewport = new ExtendViewport(1290, 1080, camera);
    }

    public void resize() {
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport.setScreenPosition(0, 0);
        viewport.apply();
    }
}
