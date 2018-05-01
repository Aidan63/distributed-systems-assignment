package uk.aidanlee.dsp.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.data.events.EvOOBData;
import uk.aidanlee.dsp.data.events.EvSendPacket;
import uk.aidanlee.dsp.net.ConnectionResponse;
import uk.aidanlee.dsp.net.ConnectionSettings;

public class ConnectingState extends State {

    private ConnectionSettings settings;

    private EventBus events;

    public ConnectingState(String _name, EventBus _events) {
        super(_name);
        events = _events;
    }

    @Override
    public void onEnter(Object _enterWith) {
        settings = (ConnectionSettings) _enterWith;
        events.register(this);
    }

    @Override
    public void onLeave(Object _leaveWith) {
        events.unregister(this);
    }

    @Override
    public void onUpdate() {
        // Send a connection request each step if we haven't heard anything back.
        events.post(new EvSendPacket(Packet.Connection(settings.getName(), settings.getEp())));
    }

    @Override
    public void onRender() {
        Gdx.gl.glClearColor(0.47f, 0.56f, 0.61f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Nothing is explicitly drawn since everything in this state is part of ImGui and drawn by that instead.
    }

    // Event Functions

    @Subscribe
    public void onOOBPacket(EvOOBData _event) {
        if (_event.packet.getData().readByte() == Packet.CONNECTION_RESPONSE) {
            if (_event.packet.getData().readBoolean()) {
                machine.set("game", new ConnectionResponse(settings.getEp(), _event.packet), null);
            } else {
                machine.set("menu", null, null);
            }
        }
    }
}
