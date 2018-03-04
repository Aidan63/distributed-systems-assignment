package uk.aidanlee.dsp.server.net;

import uk.aidanlee.dsp.common.data.ClientInfo;
import uk.aidanlee.dsp.common.net.*;
import uk.aidanlee.dsp.common.net.commands.CmdClientConnected;
import uk.aidanlee.dsp.common.net.commands.CmdClientDisconnected;
import uk.aidanlee.dsp.common.net.commands.Command;
import uk.aidanlee.dsp.server.Server;

public class Connections {
    /**
     * The maximum number of clients which can be connected to the server.
     */
    private int maxClients;

    /**
     * The number of clients currently connected.
     */
    private int numClientsConnected;

    /**
     * Array of booleans indexed by the client ID to indicated if that client ID is connected.
     */
    private boolean[] clientConnected;

    /**
     * Array of all connected clients.
     */
    private NetChan[] clients;

    /**
     * Creates a new connection manager.
     * @param _maxClients Max number of clients.
     */
    public Connections(int _maxClients) {
        maxClients          = _maxClients;
        numClientsConnected = 0;

        clientConnected = new boolean[maxClients];
        clients         = new NetChan[maxClients];
    }

    // Public API

    /**
     * Processes UDP packets received from the NetManager.
     * @param _packet Bit packed message data.
     * @param _from   The address and port the data arrived from.
     */
    public synchronized void processPacket(BitPacker _packet, EndPoint _from) {
        // If first bit is 1 then the packet is OOB
        if (_packet.readBoolean()) {
            processOOBPacket(_packet, _from);
        } else {
            // If that first bit is 0 then its a net chan packet.
            // Find the right netchan based on the sender and process the packet.
            int id = findExistingClientIndex(_from);
            CommandProcessor.parse(clients[id].receive(_packet));
        }
    }

    /**
     * Send a net chan message out to all clients.
     */
    public void update() {
        for (int i = 0; i < maxClients; i++) {
            if (isClientConnected(i)) {
                byte[] data = clients[i].send();
                if (data == null) continue;

                EndPoint ep = clients[i].getDestination();
                Server.netManager.send(data, ep);
            }
        }
    }

    public void addCommandAll(Command _c) {
        addCommandAllExcept(_c, -1);
    }

    public void addCommandAllExcept(Command _c, int _id) {
        for (int i = 0; i < maxClients; i++) {
            if (isClientConnected(i) && i != _id) {
                clients[i].addCommand(_c);
            }
        }
    }

    public void addReliableCommandAll(Command _c) {
        addReliableCommandAllExcept(_c, -1);
    }

    public void addReliableCommandAllExcept(Command _c, int _id) {
        System.out.println("Adding reliable command");
        for (int i = 0; i < maxClients; i++) {
            if (isClientConnected(i) && i != _id) {
                clients[i].addReliableCommand(_c);
            }
        }
    }

    // Processing OOB Packets

    /**
     * Decodes the processes the out of band message.
     * @param _packet Bit packed message data.
     * @param _from   The address and port the data arrived from.
     */
    private void processOOBPacket(BitPacker _packet, EndPoint _from) {
        switch (_packet.readByte()) {
            case Packet.CONNECTION:
                onConnection(_from, _packet.readString());
                break;

            case Packet.DISCONNECTION:
                onDisconnection(_from);
                break;

            case Packet.HEARTBEAT:
                onHeartbeat(_from);
                break;

            default:
                System.out.println("Unknown OOB command received from NetManager");
        }
    }

    /**
     * If there is space in the server and there are no other clients with the same endpoint accept the connection request.
     * @param _from The address and port of the connection request.
     */
    private void onConnection(EndPoint _from, String _name) {
        int clientID;

        // Check if we've already accepted a client from this endpoint.
        // If we have we send another accepted packet in case the others got lost.
        clientID = findExistingClientIndex(_from);
        if (clientID != -1) {
            Server.netManager.send(Packet.ConnectionAccepted(
                    clientID,
                    maxClients,
                    numClientsConnected,
                    0,
                    getClientInfo()), _from);
            return;
        }

        // Decline connection if server is full
        if (numClientsConnected == maxClients) {
            Server.netManager.send(Packet.ConnectionDenied(), _from);
            return;
        }

        // Accept the new client!
        clientID = findFreeClientID();
        clientConnected[clientID] = true;
        clients[clientID] = new NetChan(_from);
        numClientsConnected++;

        // Add a new player to the game.
        Server.game.addPlayer(clientID, _name);

        // Send the connection response packet.
        // Client will receive info about all other clients except itself.
        System.out.println("New client Connected with ID : " + clientID + " from " + _from.getAddress().toString() + ":" + _from.getPort());
        Server.netManager.send(Packet.ConnectionAccepted(
                clientID,
                maxClients,
                numClientsConnected,
                0,
                getClientInfo()), _from);

        // Tell all clients (except the newly connected client) a new client has connected.
        addReliableCommandAllExcept(new CmdClientConnected(getClientInfoFor(clientID)), clientID);
    }

    /**
     * //
     * @param _from The address and port of where this disconnect packet arrived from.
     */
    private void onDisconnection(EndPoint _from) {
        int clientID = findExistingClientIndex(_from);
        if (clientID != -1 && getClientEndpoint(clientID).equals(_from)) {
            clientConnected[clientID] = false;
            clients[clientID] = null;
            numClientsConnected--;

            // Remove the player from the game.
            Server.game.removePlayer(clientID);

            // Tell all other clients that another client disconnected.
            addReliableCommandAll(new CmdClientDisconnected(clientID));
        }
    }

    /**
     *
     * @param _from
     */
    private void onHeartbeat(EndPoint _from) {
        //
    }

    // Client Helper Functions.

    /**
     * Returns an array of all connected clients info.
     * @return Client info array.
     */
    private ClientInfo[] getClientInfo() {
        ClientInfo[] info = new ClientInfo[numClientsConnected];

        int index = 0;
        for (int i = 0; i < maxClients; i++) {
            if (isClientConnected(i)) {
                Player player = Server.game.getPlayer(i);
                info[index] = new ClientInfo(i, player);

                index++;
            }
        }

        return info;
    }

    /**
     * Returns a client info class for a specific client ID.
     * @param _clientID The client to get info from.
     * @return new ClientInfo instance
     */
    private ClientInfo getClientInfoFor(int _clientID) {
        Player player = Server.game.getPlayer(_clientID);
        return new ClientInfo(_clientID, player);
    }

    /**
     * Finds the first free client ID or -1 if there is no space.
     * @return Client ID
     */
    private int findFreeClientID() {
        for (int i = 0; i < maxClients; ++i) {
            if (!clientConnected[i]) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Finds the client ID from the provided endpoint.
     * @param _from The endpoint to get the ID for.
     * @return Client ID found or -1 if not found.
     */
    private int findExistingClientIndex(EndPoint _from) {
        for (int i = 0; i < maxClients; i++) {
            if (clientConnected[i] && clients[i].getDestination().equals(_from)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Checks if the provided client ID is connected to the server.
     * @param _index Client ID to check.
     * @return Boolean connection status.
     */
    private boolean isClientConnected(int _index) {
        return clientConnected[_index];
    }

    /**
     * Gets the endpoint of the client ID.
     * @param _index The client ID to search against.
     * @return Clients endpoint.
     */
    private EndPoint getClientEndpoint(int _index) {
        return clients[_index].getDestination();
    }
}
