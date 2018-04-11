package uk.aidanlee.dsp.server.data.events;

public class EvClientDisconnected {
    public final int clientID;

    public EvClientDisconnected(int _id) {
        clientID = _id;
    }
}
