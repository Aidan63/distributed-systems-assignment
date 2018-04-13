package uk.aidanlee.dsp.data.events;

import uk.aidanlee.dsp.common.net.Packet;

public class EvOOBData {
    public final Packet packet;

    public EvOOBData(Packet _packet) {
        packet = _packet;
    }
}
