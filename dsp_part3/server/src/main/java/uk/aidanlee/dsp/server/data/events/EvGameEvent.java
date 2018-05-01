package uk.aidanlee.dsp.server.data.events;

public class EvGameEvent {
    /**
     * The byte ID of this game event alert.
     */
    public final byte event;

    public EvGameEvent(byte _event) {
        event = _event;
    }
}
