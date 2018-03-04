package uk.aidanlee.dsp.common.net;

import java.net.InetAddress;

public class EndPoint {
    private InetAddress address;
    private int port;

    public EndPoint(InetAddress _address, int _port) {
        address = _address;
        port    = _port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EndPoint)) {
            return false;
        }

        EndPoint other = (EndPoint) o;
        return (other.getAddress().toString().equals(address.toString()) && other.getPort() == port);
    }
}
