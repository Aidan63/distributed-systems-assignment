package uk.aidanlee.dsp.server.states;

import com.google.common.eventbus.EventBus;
import uk.aidanlee.dsp.common.data.ServerEvent;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.server.data.events.EvGameEvent;

class RaceStateCountdown extends State {
    private int timer;

    private EventBus events;

    RaceStateCountdown(String _name, EventBus _events) {
        super(_name);
        events = _events;
    }

    @Override
    public void onEnter(Object _enterWith) {
        events.register(this);
        timer = 0;
    }

    @Override
    public void onLeave(Object _leaveWith) {
        events.unregister(this);
    }

    @Override
    public void onUpdate() {
        timer++;

        // 3 second countdown
        if (timer == 180) {
            // Change to the game and tell all clients.
            machine.set("game", null, null);
            events.post(new EvGameEvent(ServerEvent.EVENT_RACE_START));
        }
    }
}
