package uk.aidanlee.dsp.server.states;

import com.google.common.eventbus.EventBus;
import uk.aidanlee.dsp.common.data.ServerEvent;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.server.data.events.EvGameEvent;

class RaceStateResults extends State {

    private EventBus events;

    private int timer;

    RaceStateResults(String _name, EventBus _events) {
        super(_name);
        events = _events;
    }

    @Override
    public void onEnter(Object _enterWith) {
        timer = 0;
    }

    @Override
    public void onUpdate() {
        timer++;
        if (timer == 60 * 10) {
            events.post(new EvGameEvent(ServerEvent.EVENT_LOBBY_ENTER));
        }
    }

    @Override
    public void onLeave(Object _leaveWith) {
        super.onLeave(_leaveWith);
    }
}
