package uk.aidanlee.dsp.data.events;

import uk.aidanlee.dsp.common.net.Packet;

public class EvConnectionResponse {
    public final Packet data;

    public EvConnectionResponse(Packet _data) {
        data = _data;
    }
}
