package uk.aidanlee.dsp.net;

import uk.aidanlee.dsp.common.net.EndPoint;

public class ConnectionSettings {
    private String name;
    private EndPoint ep;

    public ConnectionSettings(String _name, EndPoint _ep) {
        name = _name;
        ep   = _ep;
    }

    public String getName() {
        return name;
    }

    public EndPoint getEp() {
        return ep;
    }
}
