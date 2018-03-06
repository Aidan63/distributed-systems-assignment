package uk.aidanlee.dsp.net;

import uk.aidanlee.dsp.common.net.BitPacker;
import uk.aidanlee.dsp.common.net.EndPoint;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.data.Game;

import java.util.Timer;
import java.util.TimerTask;

public class Connections {
    /**
     * Quick access to our client instance.
     */
    private Client us;

    /**
     * Array of all clients in the server, including our-self.
     */
    private Client[] clients;

    /**
     * Servers endpoint location.
     */
    private EndPoint server;

    /**
     * The current connection state of this client.
     */
    private ConnectionState state;

    /**
     * 5 second timer to check if we still have a connection to the server.
     */
    private Timer timeout;

    /**
     * Creates a new Connections instance which is "Disconnected" from the server.
     */
    public Connections() {
        state = ConnectionState.Connecting;
    }

    // Getters and Setters

    public ConnectionState getState() {
        return state;
    }
    public void setState(ConnectionState state) {
        this.state = state;
    }

    public Client[] getClients() {
        return clients;
    }
    public Client getUs() {
        return us;
    }

    public EndPoint getServer() {
        return server;
    }
    public void setServer(EndPoint server) {
        this.server = server;
    }

    // Public API

    /**
     * If we are connecting or connected read and process a packet from the net manager thread.
     */
    public void readPackets() {
        Packet pck = Game.netManager.getPackets().poll();
        while (pck != null) {
            processPacket(pck);
            pck = Game.netManager.getPackets().poll();
        }
    }

    /**
     * Reads the initial data from a packet received from the NetManager.
     * @param _packet Packet class containing the byte data and sender.
     */
    private void processPacket(Packet _packet) {
        BitPacker data = new BitPacker();
        data.writeBytes(_packet.getData());

        // First bit indicates if the packet is OOB (1) or netchan (0).
        if (data.readBoolean()) {
            processOOBPacket(data);
        } else {
            // We cannot process net chan messages if we are not in the connected state.
            if (state != ConnectionState.Connected) return;

            // Send netchan packet to the net channel and parse any commands
            BitPacker commands = Game.netChan.receive(data);
            CommandProcessor.parse(commands);
        }
    }

    /**
     * Reads the first byte of the OOB Packet to figure out which type it is.
     * @param _data OOB packet data.
     */
    private void processOOBPacket(BitPacker _data) {
        switch (_data.readByte()) {
            case Packet.CONNECTION_RESPONSE:
                readConnectionResponse(_data);
                break;

            case Packet.DISCONNECTION:
                readDisconnection(_data);
                break;

            case Packet.HEARTBEAT:
                resetTimeout();

            default:
                //
                break;
        }
    }

    /**
     * Read the connection response packet from the server.
     * @param _data OOB connection response data.
     */
    private void readConnectionResponse(BitPacker _data) {

        // If we are not attempting to connect, ignore any connection response packets.
        if (state != ConnectionState.Connecting) return;

        // Connection Denied
        if (!_data.readBoolean()) {
            System.out.println("Connection Denied");
            Game.state.set("menu", null, null);

            return;
        }

        // Connection Accepted

        int ourID        = _data.readByte();
        int maxClients   = _data.readByte();
        int mapIndex     = _data.readByte();
        int numConnected = _data.readByte();

        // Create the client structures and add their data.
        clients = new Client[maxClients];

        for (int i = 0; i < numConnected; i++) {

            // Read Basic Info
            String name = _data.readString();
            int    id   = _data.readByte();
            int    idx  = _data.readByte();

            // Read ship color
            int sR = _data.readByte();
            int sG = _data.readByte();
            int sB = _data.readByte();

            // Read trail color
            int tR = _data.readByte();
            int tG = _data.readByte();
            int tB = _data.readByte();

            // Create a new client with the read data.
            Client c = new Client(id, name);
            c.setShipIndex (idx);
            c.setShipColor (new float[] { sR, sG, sB });
            c.setTrailColor(new float[] { tR, tG, tB });

            // Add the client into the array.
            clients[id] = c;

            // If this client is about us then add it to the "us" var for quick access.
            if (id == ourID) {
                us = c;
            }
        }

        resetTimeout();

        // Switch to the lobby state.
        state = ConnectionState.Connected;
        Game.state.set("lobby", null, null);
    }

    /**
     *
     * @param _data
     */
    private void readDisconnection(BitPacker _data) {
        System.out.println("We have been disconnected / kicked / time-out by the server");
    }

    /**
     *
     */
    private void resetTimeout() {
        if (timeout != null) {
            timeout.cancel();
            timeout = null;
        }

        // Start the timout timer.
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Server timeout.");

                Game.netStop();
                Game.state.set("menu", null, null);
            }
        };
        timeout = new Timer();
        timeout.schedule(task, 5000);
    }
}
