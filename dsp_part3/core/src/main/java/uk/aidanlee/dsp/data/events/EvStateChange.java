package uk.aidanlee.dsp.data.events;

public class EvStateChange {
    public final String state;
    public final Object enterWith;
    public final Object leaveWith;

    public EvStateChange(String _state, Object _enterWith, Object _leaveWith) {
        state = _state;
        enterWith = _enterWith;
        leaveWith = _leaveWith;
    }
}
