package uk.aidanlee.dsp.data.events;

import uk.aidanlee.dsp.common.net.Packet;

public class EvNetChanData {
    public final Packet packet;

    public EvNetChanData(Packet _packet) {
        packet = _packet;
    }
}
