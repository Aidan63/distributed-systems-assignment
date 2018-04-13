package uk.aidanlee.dsp.server.data.events;

public class EvClientConnected {
    public final int clientID;
    public final String name;

    public EvClientConnected(int _id, String _name) {
        clientID = _id;
        name     = _name;
    }
}
