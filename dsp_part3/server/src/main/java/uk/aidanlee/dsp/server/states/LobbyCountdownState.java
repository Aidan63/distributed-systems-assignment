package uk.aidanlee.dsp.server.states;

import com.google.common.eventbus.EventBus;
import uk.aidanlee.dsp.common.data.ServerEvent;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.server.data.events.EvGameEvent;

/**
 * Once all clients have readied up a three second lobby countdown begins before starting the race.
 */
public class LobbyCountdownState extends State {

    /**
     * Access to the game event bus.
     */
    private EventBus events;

    /**
     * Current timer.
     */
    private int timer;

    /**
     * Creates a new lobby countdown state.
     * @param _name   State name.
     * @param _events Game event bus.
     */
    public LobbyCountdownState(String _name, EventBus _events) {
        super(_name);

        events = _events;
    }

    @Override
    public void onEnter(Object _enterWith) {
        events.register(this);
        events.post(new EvGameEvent(ServerEvent.EVENT_LOBBY_COUNTDOWN));

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
            events.post(new EvGameEvent(ServerEvent.EVENT_RACE_ENTER));
        }
    }
}
