package uk.aidanlee.dsp.server;

import com.google.common.eventbus.Subscribe;
import uk.aidanlee.dsp.common.data.ClientInfo;
import uk.aidanlee.dsp.common.net.*;
import uk.aidanlee.dsp.common.net.commands.*;
import uk.aidanlee.dsp.server.data.Game;
import uk.aidanlee.dsp.server.data.events.*;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Main server class. Holds all data on connected clients and manages the game simulation.
 * The server can be simulated by calling the onLoop, onTick, and onStep functions.
 */
public class Server {

    /**
     * Network thread which will listen for OOB and NetChan packets.
     */
    private final NetManager netManager;

    /**
     * Network thread which will listen for discovery broadcasts.
     */
    private final NetManager netDiscover;

    /**
     * Game simulation.
     */
    private final Game game;

    /**
     * NetChan array for all connected clients.
     */
    private final NetChan[] clients;

    /**
     * Timer array for timeouts for all connected clients.
     * If the server hasn't received a packet from a client for 5 seconds it gets disconnected.
     */
    private final Timer[] timeouts;

    /**
     * The port this server is listening on.
     */
    private final int port;

    /**
     * The name of this server.
     */
    private final String name;

    /**
     * The maximum number of clients for this server.
     */
    private final int maxClients;

    /**
     * The current number of connected clients.
     */
    private int numClients;

    /**
     * Creates a new server.
     * @param _name          Name of the server.
     * @param _port          Port the server listens on.
     * @param _discoveryPort Port the server will listen to discovery packets on.
     * @param _clients       Max number of clients which can be connected.
     */
    public Server(String _name, int _port, int _discoveryPort, int _clients) {

        netManager = new NetManager(_port);
        netManager.start();

        netDiscover = new NetManager(_discoveryPort);
        netDiscover.start();

        game = new Game();
        game.getEvents().register(this);

        name = _name;
        port = _port;

        maxClients = _clients;
        numClients = 0;

        clients  = new NetChan[maxClients];
        timeouts = new Timer[maxClients];
    }

    // core server loop functions.

    /**
     * Called as fast as the process is allowed.
     * Checks the network listener threads for any packets.
     */
    public void onLoop() {
        Packet packet;

        packet = netManager.getPackets().poll();
        while (packet != null) {
            onPacket(packet);
            packet = netManager.getPackets().poll();
        }

        packet = netDiscover.getPackets().poll();
        while (packet != null) {
            onPacket(packet);
            packet = netDiscover.getPackets().poll();
        }
    }

    /**
     * Steps forward the game simulation.
     * @param _dt Step delta time.
     */
    public void onStep(float _dt) {
        game.update();
    }

    /**
     * Networking tick function. Data is sent out of the server when this function is called.
     * @param _dt Tick delta time.
     */
    public void onTick(float _dt) {

        // Generate the new master snapshot with all players data.
        Snapshot master = new Snapshot();
        for (int i = 0; i < maxClients; i++) {
            if (clients[i] == null) continue;
            master.addPlayer(i, game.getPlayer(i));
        }

        // Add this snapshot to each clients netchan.
        // Then generate a netchan packet and send it out.
        for (int i = 0; i < maxClients; i++) {
            if (clients[i] == null) continue;

            clients[i].addSnapshot(master);

            Packet packet = clients[i].send();
            if (packet == null) continue;

            netManager.send(packet);
        }
    }

    // Event Functions

    /**
     * When a game simulation even occurs send a reliable command to all connected clients alerting them to it.
     * @param _event Game event which has happened.
     */
    @Subscribe
    public void onGameEvent(EvGameEvent _event) {
        sendReliableCommandAll(new CmdServerEvent(_event.event));
    }

    /**
     * When a player has finished all their laps send a reliable command to all clients except the one for the now finished player.
     * The finished players local simulation will also track this and take appropriate action.
     *
     * @param _event Player finished event containing the clientID of the finished player.
     */
    @Subscribe
    public void onPlayerFinished(EvPlayerFinished _event) {
        sendReliableCommandAll(new CmdPlayerFinished(_event.clientID));
    }

    /**
     * When a race has been completed the race events command is called. Send the results to all clients.
     * @param _event Results event with all player times.
     */
    @Subscribe
    public void onRaceResults(EvRaceResults _event) {
        sendReliableCommandAll(new CmdRaceResults(_event.times, 3));
    }

    // Packet Functions

    /**
     * When a packet is received from either network thread the first bit is read to determine its type.
     * @param _packet Packet received.
     */
    private void onPacket(Packet _packet) {
        if (_packet.getData().readBoolean()) {
            onOOBPacket(_packet);
        } else {
            onNetChanPacket(_packet);
        }
    }

    /**
     * OOB packets contain one command each. The first byte tells you which command it is.
     * @param _packet OOB Packet.
     */
    private void onOOBPacket(Packet _packet) {
        switch (_packet.getData().readByte()) {
            case Packet.CONNECTION:
                onClientConnected(_packet);
                break;

            case Packet.DISCONNECTION:
                onClientDisconnected(_packet);
                break;

            case Packet.HEARTBEAT:
                onClientHeartbeat(_packet);
                break;

            case Packet.DISCOVERY_REQUEST:
                onDiscoveryRequest(_packet);
                break;
        }
    }

    /**
     * If it's a netchan packet we figure out which client sent it based on the packet endpoint and send the packet to
     * that clients netchan instance.
     * All of the commands from the netchan packet are then read and performed. Clients only send the server three
     * types of commands.
     * @param _packet NetChan packet.
     */
    private void onNetChanPacket(Packet _packet) {
        int id = findExistingClientID(_packet.getEndpoint());
        if (id == -1) return;

        for (Command cmd : clients[id].receive(_packet)) {
            switch (cmd.id) {
                case Command.CHAT_MESSAGE:
                    // Server does not currently log chat messages, just relays them to all clients except the sender.
                    // Might want to add server logging in future.
                    sendReliableCommandAllExcept(cmd, ((CmdChatMessage) cmd).clientID);
                    break;

                case Command.CLIENT_INPUT:
                    // Send the commands data into the game simulation.
                    game.getEvents().post(new EvClientInput((CmdClientInput) cmd));
                    break;

                case Command.CLIENT_SETTINGS:
                    // Send the commands data into the game simulation.
                    game.getEvents().post(new EvClientSettings((CmdClientSettings) cmd));
                    break;
            }
        }
    }

    // OOB Packet Functions

    /**
     * Handles receiving a connection request packet.
     * @param _packet Packet.
     */
    private void onClientConnected(Packet _packet) {

        // Check if there's already a client from the packets endpoint.
        // If there is we send another connection response in case the others got lost.
        int clientID = findExistingClientID(_packet.getEndpoint());
        if (clientID != -1) {
            netManager.send(Packet.ConnectionAccepted(
                    clientID,
                    maxClients,
                    numClients,
                    0,
                    getClientInfo(),
                    _packet.getEndpoint()
            ));
            return;
        }

        // If we are not in an active lobby reject any connection requests.
        if (!game.getState().equals("lobby-active")) {
            netManager.send(Packet.ConnectionDenied(_packet.getEndpoint()));
            return;
        }

        // Reject connections if we are already full.
        if (numClients == maxClients) {
            netManager.send(Packet.ConnectionDenied(_packet.getEndpoint()));
            return;
        }

        // If we have passed all of those checks the connection is accepted.
        clientID = findFreeClientID();
        clients[clientID] = new NetChan(_packet.getEndpoint());
        numClients++;

        // get the clients name and tell the simulation to create a player.
        String name = _packet.getData().readString();
        game.getEvents().post(new EvClientConnected(clientID, name));

        // Start the heartbeat timer.
        resetTimeout(_packet);

        // Send a connection accepted packet to the client.
        // Client receives info on all connected clients in the response.
        netManager.send(Packet.ConnectionAccepted(
                clientID,
                maxClients,
                numClients,
                0,
                getClientInfo(),
                _packet.getEndpoint()
        ));

        // All all other clients of the new client connection.
        sendReliableCommandAllExcept(new CmdClientConnected(getClientInfo(clientID)), clientID);
    }

    /**
     * Handles a disconnection packet.
     * @param _packet Packet.
     */
    private void onClientDisconnected(Packet _packet) {

        // Check if the client sending the disconnection packet is still connected and from the same computer.
        int clientID = findExistingClientID(_packet.getEndpoint());
        if (clientID == -1) return;
        if (!_packet.getEndpoint().equals(clients[clientID].getDestination())) return;

        // Remove the client from the structure.
        clients[clientID] = null;
        numClients--;

        // Tell the simulation a client disconnected.
        game.getEvents().post(new EvClientDisconnected(clientID));

        // Cancel the heartbeat timeout.
        timeouts[clientID].cancel();
        timeouts[clientID] = null;

        // Tell all clients about the disconnection.
        sendReliableCommandAll(new CmdClientDisconnected(clientID));
    }

    /**
     * Handles a heartbeat packet. Resets the timeout timer for the client who sent the heartbeat.
     * @param _packet Packet.
     */
    private void onClientHeartbeat(Packet _packet) {
        int clientID = findExistingClientID(_packet.getEndpoint());
        if (clientID != -1) {
            resetTimeout(_packet);
            netManager.send(Packet.Heartbeat(_packet.getEndpoint()));
        }
    }

    /**
     * Handles receiving a discovery packet. Respond to the discovery request with info on this server.
     * @param _packet Packet.
     */
    private void onDiscoveryRequest(Packet _packet) {
        netDiscover.send(Packet.Discovery(name, port, numClients, maxClients, _packet.getEndpoint()));
    }

    // Client Helper Functions.

    /**
     * Returns an array of all client information. Used to tell connecting clients about all other clients.
     * @return array of client info.
     */
    private ClientInfo[] getClientInfo() {
        ClientInfo[] info = new ClientInfo[numClients];

        int index = 0;
        for (int i = 0; i < maxClients; i++) {
            if (clients[i] == null) continue;

            Player player = game.getPlayer(i);
            info[index++] = new ClientInfo(i, player);
        }

        return info;
    }

    /**
     * Returns a client info instance for a specific client.
     * @param _clientID client to get info on.
     * @return client info instance.
     */
    private ClientInfo getClientInfo(int _clientID) {
        return new ClientInfo(_clientID, game.getPlayer(_clientID));
    }

    /**
     * Adds a reliable command to all clients netchans
     * @param _cmd Command to add.
     */
    private void sendReliableCommandAll(Command _cmd) {
        sendReliableCommandAllExcept(_cmd, -1);
    }

    /**
     * Add a reliable command to all clients netchans except for the provided clients.
     * @param _cmd      Command to add.
     * @param _clientID ID of the client to skip.
     */
    private void sendReliableCommandAllExcept(Command _cmd, int _clientID) {
        for (int i = 0; i < maxClients; i++) {
            if (clients[i] == null || i == _clientID) continue;
            clients[i].addReliableCommand(_cmd);
        }
    }

    /**
     * Resets a timer for the client who send the packet.
     * @param _packet Packet.
     */
    private void resetTimeout(Packet _packet) {
        int clientID = findExistingClientID(_packet.getEndpoint());

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                onClientDisconnected(_packet);
            }
        };

        if (timeouts[clientID] != null) {
            timeouts[clientID].cancel();
        }

        timeouts[clientID] = new Timer();
        timeouts[clientID].schedule(task, 5000);
    }

    /**
     * Find the clientID for the endpoint.
     * @param _from EndPoint to check.
     * @return int clientID.
     */
    private int findExistingClientID(EndPoint _from) {
        for (int i = 0; i < maxClients; i++) {
            if (clients[i] == null) continue;
            if (clients[i].getDestination().equals(_from)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Gets the first free clientID or -1 is none are available.
     * @return int clientID.
     */
    private int findFreeClientID() {
        for (int i = 0; i < maxClients; i++) {
            if (clients[i] == null) return i;
        }

        return -1;
    }
}
