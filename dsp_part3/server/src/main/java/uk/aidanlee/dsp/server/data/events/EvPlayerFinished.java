package uk.aidanlee.dsp.server.data.events;

public class EvPlayerFinished {
    public final int clientID;

    public EvPlayerFinished(int _clientID) {
        clientID = _clientID;
    }
}
