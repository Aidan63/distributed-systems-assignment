package uk.aidanlee.dsp.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.data.Game;
import uk.aidanlee.dsp.net.ConnectionState;

public class ConnectingState extends State {
    private String playerName;

    public ConnectingState(String _name) {
        super(_name);
    }

    @Override
    public void onEnter(Object _enterWith) {
        playerName = (String) _enterWith;
        System.out.println("connecting as " + _enterWith);

        Game.connections.setState(ConnectionState.Connecting);
    }

    @Override
    public void onUpdate() {
        // Send a connection packet!
        Game.netManager.send(Packet.Connection(playerName));
    }

    @Override
    public void onRender() {
        Gdx.gl.glClearColor(0.47f, 0.56f, 0.61f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Nothing is explicitly drawn since everything in this state is part of ImGui and drawn by that instead.
    }
}
