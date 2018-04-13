package uk.aidanlee.dsp.data.events;

import uk.aidanlee.dsp.common.net.Packet;

public class EvSendPacket {
    public final Packet packet;

    public EvSendPacket(Packet _packet) {
        packet = _packet;
    }
}
