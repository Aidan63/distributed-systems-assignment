package uk.aidanlee.dsp.server.net;

import uk.aidanlee.dsp.common.data.ClientInfo;
import uk.aidanlee.dsp.common.net.*;
import uk.aidanlee.dsp.common.net.commands.*;
import uk.aidanlee.dsp.server.Server;

import java.util.Timer;
import java.util.TimerTask;

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
     * Timer for each client to check for time outs.
     */
    private Timer[] timeouts;

    /**
     * Dummy snapshot. Used to generate full updates.
     */
    private Snapshot dummySnapshot;

    /**
     * Creates a new connection manager.
     * @param _maxClients Max number of clients.
     */
    public Connections(int _maxClients) {
        maxClients          = _maxClients;
        numClientsConnected = 0;

        clientConnected = new boolean[maxClients];
        clients         = new NetChan[maxClients];
        timeouts        = new Timer[maxClients];

        // Create the dummy snapshot and fill it with players with all fields ZEROed.
        dummySnapshot = new Snapshot();
        for (int i = 0; i < maxClients; i++) {
            dummySnapshot.addPlayer(i, new Player("dummy"));
        }
    }

    // Public API

    /**
     * Processes UDP packets received from the NetManager.
     * @param _packet Bit packed message data.
     */
    public void processPacket(Packet _packet) {
        if (_packet.getData().readBoolean()) {

            // If first bit is 1 then the packet is OOB
            processOOBPacket(_packet);
        } else {

            // If that first bit is 0 then its a net chan packet.
            // Find the right netchan based on the sender and process the packet.
            int id = findExistingClientIndex(_packet.getEndpoint());
            if (id == -1) return;

            for (Command cmd : clients[id].receive(_packet)) {
                switch (cmd.id) {
                    case Command.CHAT_MESSAGE:
                        addReliableCommandAllExcept(cmd, ((CmdChatMessage) cmd).clientID);
                        break;

                    default:
                        Server.game.addCommand(cmd);
                        break;
                }
            }
        }
    }

    /**
     * Send a net chan message out to all clients.
     */
    public void update() {

        // Generate the new master snapshot.
        Snapshot master = new Snapshot();
        for (int i = 0; i < maxClients; i++) {
            if (!isClientConnected(i)) continue;

            master.addPlayer(i, Server.game.getPlayers()[i]);
        }

        // Add the new snapshot in each client netchan and send a netchan update.
        for (int i = 0; i < maxClients; i++) {
            if (!isClientConnected(i)) continue;

            // Add the latest master snapshot to each clients netchan.
            clients[i].addSnapshot(master);

            // Generate a new netchan packet.
            Packet packet = clients[i].send();
            if (packet == null) continue;

            Server.netManager.send(packet);
        }

        // Send out LAN broadcast packets about our server
        if (Server.game.getState().equals("lobby-active")) {
            Packet broadcast = Packet.Discovery("Name", numClientsConnected, maxClients);
            Server.netManager.send(broadcast);
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
        for (int i = 0; i < maxClients; i++) {
            if (isClientConnected(i) && i != _id) {
                clients[i].addReliableCommand(_c);
            }
        }
    }

    // Processing OOB Packets

    /**
     * Decodes the processes the out of band message.
     * @param _packet
     */
    private void processOOBPacket(Packet _packet) {
        switch (_packet.getData().readByte()) {
            case Packet.CONNECTION:
                onConnection(_packet);
                break;

            case Packet.DISCONNECTION:
                onDisconnection(_packet);
                break;

            case Packet.HEARTBEAT:
                onHeartbeat(_packet);
                break;

            default:
                System.out.println("Unknown OOB command received from NetManager");
        }
    }

    /**
     * If there is space in the server and there are no other clients with the same endpoint accept the connection request.
     * @param _packet
     */
    private void onConnection(Packet _packet) {
        int clientID;

        // Check if we've already accepted a client from this endpoint.
        // If we have we send another accepted packet in case the others got lost.
        clientID = findExistingClientIndex(_packet.getEndpoint());
        if (clientID != -1) {
            Server.netManager.send(Packet.ConnectionAccepted(
                    clientID,
                    maxClients,
                    numClientsConnected,
                    0,
                    getClientInfo(), _packet.getEndpoint()));
            return;
        }

        // Decline connection if the game is not in a active lobby
        if (!Server.game.getState().equals("lobby-active")) {
            Server.netManager.send(Packet.ConnectionDenied(_packet.getEndpoint()));
            return;
        }

        // Decline connection if server is full
        if (numClientsConnected == maxClients) {
            Server.netManager.send(Packet.ConnectionDenied(_packet.getEndpoint()));
            return;
        }

        // Accept the new client!
        clientID = findFreeClientID();
        clientConnected[clientID] = true;
        clients[clientID] = new NetChan(_packet.getEndpoint());
        numClientsConnected++;

        System.out.println("New Client Connected : " + clientID);

        // Read the name and add a new player to the game.
        String name = _packet.getData().readString();
        Server.game.addPlayer(clientID, name);

        // Start the timeout checker
        resetTimeout(_packet);

        // Send the connection response packet.
        // Client will receive info about all other clients except itself.
        System.out.println("New client Connected with ID : " + clientID + " from " + _packet.getEndpoint().getAddress().toString() + ":" + _packet.getEndpoint().getPort());
        Server.netManager.send(Packet.ConnectionAccepted(
                clientID,
                maxClients,
                numClientsConnected,
                0,
                getClientInfo(), _packet.getEndpoint()));

        // Tell all clients (except the newly connected client) a new client has connected.
        addReliableCommandAllExcept(new CmdClientConnected(getClientInfoFor(clientID)), clientID);
    }

    /**
     *
     * @param _packet
     */
    private void onDisconnection(Packet _packet) {
        int clientID = findExistingClientIndex(_packet.getEndpoint());
        if (clientID != -1 && getClientEndpoint(clientID).equals(_packet.getEndpoint())) {
            clientConnected[clientID] = false;
            clients[clientID] = null;
            numClientsConnected--;

            System.out.println("Client Disconnected : " + clientID);

            // Remove the player from the game.
            Server.game.removePlayer(clientID);

            // Cancel the heartbeat timeout
            timeouts[clientID].cancel();
            timeouts[clientID] = null;

            // Tell all other clients that another client disconnected.
            CmdClientDisconnected cmd = new CmdClientDisconnected(clientID);
            addReliableCommandAll(cmd);
            Server.game.addCommand(cmd);
        }
    }

    /**
     * Gets the client ID for the heartbeat packet and resets the timeout.
     * Responds to the client with a heartbeat packet.
     * @param _packet
     */
    private void onHeartbeat(Packet _packet) {
        int clientID = findExistingClientIndex(_packet.getEndpoint());
        if (clientID != -1) {
            resetTimeout(_packet);
            Server.netManager.send(Packet.Heartbeat(_packet.getEndpoint()));
        }
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
                Player player = Server.game.getPlayers()[i];
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
        Player player = Server.game.getPlayers()[_clientID];
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

    /**
     * Resets the timeout for a client when a heart beat packet is received.
     * @param _packet
     */
    private void resetTimeout(Packet _packet) {
        //
        int id = findExistingClientIndex(_packet.getEndpoint());

        // Task to disconnect the client once its timed out.
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println(id + " timed out");
                onDisconnection(_packet);
            }
        };

        // Cancel an existing timer.
        if (timeouts[id] != null) {
            timeouts[id].cancel();
        }

        // Add a new timer for 5 seconds.
        timeouts[id] = new Timer();
        timeouts[id].schedule(task, 5000);
    }
}
