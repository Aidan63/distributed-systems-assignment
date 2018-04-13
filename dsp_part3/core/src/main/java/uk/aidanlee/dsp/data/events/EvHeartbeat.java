package uk.aidanlee.dsp.data.events;

import uk.aidanlee.dsp.common.net.EndPoint;

public class EvHeartbeat {
    public final EndPoint from;

    public EvHeartbeat(EndPoint _from) {
        from = _from;
    }
}
