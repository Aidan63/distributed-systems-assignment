package uk.aidanlee.dsp.net;

import com.badlogic.gdx.utils.Timer;
import uk.aidanlee.dsp.common.net.BitPacker;
import uk.aidanlee.dsp.common.net.EndPoint;
import uk.aidanlee.dsp.common.net.NetManager;
import uk.aidanlee.dsp.common.net.Packet;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static uk.aidanlee.dsp.common.net.Packet.DISCOVERY_REQUEST;
import static uk.aidanlee.dsp.common.net.Packet.DISCOVERY_RESPONSE;

/**
 *
 */
public class ServerDiscovery {

    /**
     * Thread which receives and sends discovery requests and responses.
     */
    private NetManager listener;

    /**
     *
     */
    private Timer.Task requestSender;

    /**
     * List of all found LAN servers.
     */
    private List<ServerDetails> lanServers;

    /**
     * Returns a list of all of the LAN servers
     * @return List of servers
     */
    public List<ServerDetails> getLanServers() {
        return lanServers;
    }

    /**
     * Starts a discovery thread and an empty list of servers.
     */
    public ServerDiscovery() {
        listener   = new NetManager();
        lanServers = new LinkedList<>();

        listener.start();

        Timer.Task task = new Timer.Task() {
            @Override
            public void run() {

                Thread thread = new Thread(() -> {
                    // Build the discovery request data.
                    BitPacker packer = new BitPacker();
                    packer.writeBoolean(true);
                    packer.writeByte(DISCOVERY_REQUEST);

                    // Send it out every interfaces broadcast address.
                    try {
                        for (InetAddress address : getBroadcastAddresses()) {
                            Packet packet = new Packet(packer, new EndPoint(address, 7778));
                            listener.send(packet);
                        }
                    } catch (SocketException _ex) {
                        System.out.println("Socket Exception : " + _ex.getMessage());
                    }
                });
                thread.start();
            }
        };
        requestSender = Timer.schedule(task, 1, 1);
    }

    /**
     * Checks for any response packets from servers and sends out a discovery request out the broadcast of all network interfaces.
     */
    public void update() {
        // Read any packets from the server listener.
        Packet pck = listener.getPackets().poll();
        while (pck != null) {
            readPacket(pck);
            pck = listener.getPackets().poll();
        }
    }

    /**
     * Stops the network thread.
     */
    public void destroy() {
        listener.interrupt();
        requestSender.cancel();
    }

    /**
     * Reads a discovery response packet.
     * Updates an existing LAN server entry with the new data if one with the same IP and port exists.
     * If no existing server entry exists add a new one.
     * @param _packet Packet data and endpoint.
     */
    private void readPacket(Packet _packet) {
        // Is an OOB Packet and a discovery type.
        if (_packet.getData().readBoolean() && _packet.getData().readByte() == DISCOVERY_RESPONSE) {
            InetAddress ip   = _packet.getEndpoint().getAddress();
            String      name = _packet.getData().readString();
            int         port = _packet.getData().readInteger(16);
            int         conn = _packet.getData().readByte();
            int         max  = _packet.getData().readByte();

            ServerDetails details = lanServers.stream()
                    .filter(s -> s.getIp().equals(ip))
                    .findFirst()
                    .orElse(null);

            if (details == null) {
                details = new ServerDetails(ip, port, name);
                lanServers.add(details);
            }

            details.setConnected(conn);
            details.setMaxConnections(max);
        }
    }

    /**
     * Gets a list of all of the broadcast addesses on this computer.
     * @return List of all broadcast addresses.
     * @throws SocketException Getting interfaces.
     */
    private List<InetAddress> getBroadcastAddresses() throws SocketException {
        List<InetAddress> broadcastList = new LinkedList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            networkInterface.getInterfaceAddresses().stream()
                    .map(InterfaceAddress::getBroadcast)
                    .filter(Objects::nonNull)
                    .forEach(broadcastList::add);
        }

        return broadcastList;
    }

    /**
     * Class which stores details of a discovered LAN server.
     */
    public class ServerDetails {

        /**
         * IP address of the server.
         */
        private final InetAddress ip;

        /**
         * Port the server is listening on.
         */
        private final int port;

        /**
         * Name of the server.
         */
        private final String name;

        /**
         * Number of currently connected clients.
         */
        private int connected;

        /**
         * Max number of clients.
         */
        private int maxConnections;

        /**
         * Creates a new LAN server entry
         * @param _ip   IP of the server.
         * @param _port Port the server is listening on.
         * @param _name Name of the server.
         */
        ServerDetails(InetAddress _ip, int _port, String _name) {
            port = _port;
            ip   = _ip;
            name = _name;
        }

        public InetAddress getIp() {
            return ip;
        }

        public String getName() {
            return name;
        }

        public int getPort() {
            return port;
        }

        public int getConnected() {
            return connected;
        }

        public int getMaxConnections() {
            return maxConnections;
        }

        void setConnected(int connected) {
            this.connected = connected;
        }

        void setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
        }
    }
}
