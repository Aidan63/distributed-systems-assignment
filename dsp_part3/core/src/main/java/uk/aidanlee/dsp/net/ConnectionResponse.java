package uk.aidanlee.dsp.net;

import uk.aidanlee.dsp.common.net.EndPoint;
import uk.aidanlee.dsp.common.net.Packet;

public class ConnectionResponse {
    private EndPoint ep;
    private Packet packet;

    public ConnectionResponse(EndPoint _ep, Packet _packet) {
        ep     = _ep;
        packet = _packet;
    }

    public EndPoint getEp() {
        return ep;
    }

    public Packet getPacket() {
        return packet;
    }
}
