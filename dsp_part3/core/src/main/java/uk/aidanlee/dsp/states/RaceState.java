package uk.aidanlee.dsp.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.data.Game;

public class RaceState extends State {
    public RaceState(String _name) {
        super(_name);
    }

    @Override
    public void onUpdate() {
        // Send a netchan update out.
        Packet packet = Game.netChan.send();
        if (packet != null) {
            Game.netManager.send(packet);
        }
    }

    @Override
    public void onRender() {
        Gdx.gl.glClearColor(0.47f, 0.56f, 0.61f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
