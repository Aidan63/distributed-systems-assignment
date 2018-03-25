package uk.aidanlee.dsp.net;

import uk.aidanlee.dsp.common.net.NetManager;
import uk.aidanlee.dsp.common.net.Packet;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

public class ServerDiscovery {
    private NetManager listener;
    private List<ServerDetails> lanServers;

    public List<ServerDetails> getLanServers() {
        return lanServers;
    }

    public ServerDiscovery() {
        listener   = new NetManager(7778);
        lanServers = new LinkedList<>();

        listener.start();
    }

    public void update() {
        // Read any packets from the server listener.
        Packet pck = listener.getPackets().poll();
        while (pck != null) {
            readPacket(pck);
            pck = listener.getPackets().poll();
        }
    }

    public void destroy() {
        listener.interrupt();
    }

    private void readPacket(Packet _packet) {
        if (_packet.getData().readBoolean() && _packet.getData().readByte() == Packet.DISCOVERY) {
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

    public class ServerDetails {
        private final InetAddress ip;
        private final int port;
        private final String name;
        private int connected;
        private int maxConnections;

        public ServerDetails(InetAddress _ip, int _port, String _name) {
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

        public void setConnected(int connected) {
            this.connected = connected;
        }

        public void setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
        }
    }
}
