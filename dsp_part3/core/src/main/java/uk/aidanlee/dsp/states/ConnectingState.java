package uk.aidanlee.dsp.states;

import com.badlogic.gdx.utils.Timer;
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

    private Timer.Task timeout;

    public ConnectingState(String _name, EventBus _events) {
        super(_name);
        events = _events;
    }

    @Override
    public void onEnter(Object _enterWith) {
        settings = (ConnectionSettings) _enterWith;
        events.register(this);

        Timer.Task task = new Timer.Task() {
            @Override
            public void run() {
                machine.set("menu", null, null);
            }
        };

        timeout = Timer.schedule(task, 5);
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

    // Event Functions

    @Subscribe
    public void onOOBPacket(EvOOBData _event) {
        if (_event.packet.getData().readByte() == Packet.CONNECTION_RESPONSE) {
            if (_event.packet.getData().readBoolean()) {
                machine.set("game", new ConnectionResponse(settings.getEp(), _event.packet), null);
                timeout.cancel();
            } else {
                machine.set("menu", null, null);
                timeout.cancel();
            }
        }
    }
}
