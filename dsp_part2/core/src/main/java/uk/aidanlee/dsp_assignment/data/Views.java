package uk.aidanlee.dsp_assignment.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.*;
import uk.aidanlee.dsp_assignment.race.Race;

public class Views {
    private OrthographicCamera[] cameras;
    private Viewport[] viewports;
    private SplitType split;

    public OrthographicCamera[] getCameras() {
        return cameras;
    }
    public Viewport[] getViewports() {
        return viewports;
    }

    public void setup() {
        cameras   = new OrthographicCamera[Race.settings.getLocalPlayers()];
        viewports = new Viewport[Race.settings.getLocalPlayers()];
        split     = Race.settings.getSplit();

        for (int i = 0; i < Race.settings.getLocalPlayers(); i++) {
            cameras[i] = new OrthographicCamera();
            cameras[i].setToOrtho(true);
            cameras[i].zoom = 1.2f;
            viewports[i] = new ExtendViewport(1920, 1080, cameras[i]);
        }

        resize();
    }

    public void resize() {
        // All of the libGDX viewport stuff assumes 0x0 is bottom left instead of top left...

        int midx = Gdx.graphics.getWidth()  / 2;
        int midy = Gdx.graphics.getHeight() / 2;

        switch (Race.settings.getLocalPlayers()) {
            case 1:
                viewports[0].update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                viewports[0].setScreenPosition(0, 0);
                viewports[0].apply();
                break;

            case 2:
                if (split == SplitType.Vertical) {
                    viewports[0].update(midx, Gdx.graphics.getHeight());
                    viewports[0].setScreenPosition(midx, 0);
                    viewports[0].apply();

                    viewports[1].update(midx, Gdx.graphics.getHeight());
                    viewports[1].setScreenPosition(0, 0);
                    viewports[1].apply();
                } else {
                    viewports[0].update(Gdx.graphics.getWidth(), midy);
                    viewports[0].setScreenPosition(0, midy);
                    viewports[0].apply();

                    viewports[1].update(Gdx.graphics.getWidth(), midy);
                    viewports[1].setScreenPosition(0, 0);
                    viewports[1].apply();
                }
                break;

            case 3:
                if (split == SplitType.Vertical) {
                    viewports[0].update(midx, Gdx.graphics.getHeight());
                    viewports[0].setScreenPosition(0, 0);
                    viewports[0].apply();

                    viewports[1].update(midx, midy);
                    viewports[1].setScreenPosition(midx, midy);
                    viewports[1].apply();

                    viewports[2].update(midx, midy);
                    viewports[2].setScreenPosition(midx, 0);
                    viewports[2].apply();
                } else {
                    viewports[0].update(Gdx.graphics.getWidth(), midy);
                    viewports[0].setScreenPosition(0, midy);
                    viewports[0].apply();

                    viewports[1].update(midx, midy);
                    viewports[1].setScreenPosition(0, 0);
                    viewports[1].apply();

                    viewports[2].update(midx, midy);
                    viewports[2].setScreenPosition(midx, 0);
                    viewports[2].apply();
                }
                break;

            case 4:
                viewports[0].update(midx, midy);
                viewports[0].setScreenPosition(0, midy);
                viewports[0].apply();

                viewports[1].update(midx, midy);
                viewports[1].setScreenPosition(midx, midy);
                viewports[1].apply();

                viewports[2].update(midx, midy);
                viewports[2].setScreenPosition(0, 0);
                viewports[2].apply();

                viewports[3].update(midx, midy);
                viewports[3].setScreenPosition(midx, 0);
                viewports[3].apply();
                break;
        }
    }
}
