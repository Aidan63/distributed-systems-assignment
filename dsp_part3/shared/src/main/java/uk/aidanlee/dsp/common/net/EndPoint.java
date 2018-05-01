package uk.aidanlee.dsp.common.net;

import java.net.InetAddress;

/**
 * Represents a specific location on the network.
 * Contains an IP address and port.
 */
public class EndPoint {

    /**
     * The IP address of this location
     */
    private InetAddress address;

    /**
     * The port the device at the IP is listening on.
     */
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
