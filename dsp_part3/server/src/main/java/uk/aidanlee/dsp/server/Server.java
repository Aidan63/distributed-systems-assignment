package uk.aidanlee.dsp.server;

import com.google.common.eventbus.Subscribe;
import uk.aidanlee.dsp.common.data.ClientInfo;
import uk.aidanlee.dsp.common.net.*;
import uk.aidanlee.dsp.common.net.commands.*;
import uk.aidanlee.dsp.server.data.Game;
import uk.aidanlee.dsp.server.data.events.*;

import java.util.Timer;
import java.util.TimerTask;

public class Server {

    private final NetManager netManager;

    private final Game game;

    private final NetChan[] clients;

    private final Timer[] timeouts;

    private final int maxClients;

    private int numClients;

    public Server(int _port, int _clients) {

        netManager = new NetManager(_port);
        netManager.start();

        game = new Game();
        game.getEvents().register(this);

        maxClients = _clients;
        numClients = 0;

        clients  = new NetChan[maxClients];
        timeouts = new Timer[maxClients];
    }

    // core server loop functions.

    /**
     * Called every loop iteration of the server launcher.
     * Checks the network listener thread for any messages.
     */
    public void onLoop() {
        Packet packet = netManager.getPackets().poll();
        while (packet != null) {
            onPacket(packet);
            packet = netManager.getPackets().poll();
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

        // TODO : Broadcast info about the server across the LAN
    }

    // Event Functions

    @Subscribe
    public void onGameEvent(EvGameEvent _event) {
        sendReliableCommandAll(new CmdServerEvent(_event.event));
    }

    @Subscribe
    public void onPlayerFinished(EvPlayerFinished _event) {
        System.out.println("Sending player finished CMD");
        sendReliableCommandAll(new CmdPlayerFinished(_event.clientID));
    }

    @Subscribe
    public void onRaceResults(EvRaceResults _event) {
        System.out.println("Sending race results CMD");
        sendReliableCommandAll(new CmdRaceResults(_event.times, 3));
    }

    // Packet Functions

    private void onPacket(Packet _packet) {
        if (_packet.getData().readBoolean()) {
            onOOBPacket(_packet);
        } else {
            onNetChanPacket(_packet);
        }
    }

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
        }
    }

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
                    game.getEvents().post(new EvClientInput((CmdClientInput) cmd));
                    break;

                case Command.CLIENT_SETTINGS:
                    game.getEvents().post(new EvClientSettings((CmdClientSettings) cmd));
                    break;
            }
        }
    }

    // OOB Packet Functions

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

    private void onClientHeartbeat(Packet _packet) {
        int clientID = findExistingClientID(_packet.getEndpoint());
        if (clientID != -1) {
            resetTimeout(_packet);
            netManager.send(Packet.Heartbeat(_packet.getEndpoint()));
        }
    }

    // Client Helper Functions.

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

    private ClientInfo getClientInfo(int _clientID) {
        return new ClientInfo(_clientID, game.getPlayer(_clientID));
    }

    private void sendReliableCommandAll(Command _cmd) {
        sendReliableCommandAllExcept(_cmd, -1);
    }

    private void sendReliableCommandAllExcept(Command _cmd, int _clientID) {
        for (int i = 0; i < maxClients; i++) {
            if (clients[i] == null || i == _clientID) continue;
            clients[i].addReliableCommand(_cmd);
        }
    }

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

    private int findExistingClientID(EndPoint _from) {
        for (int i = 0; i < maxClients; i++) {
            if (clients[i] == null) continue;
            if (clients[i].getDestination().equals(_from)) {
                return i;
            }
        }

        return -1;
    }

    private int findFreeClientID() {
        for (int i = 0; i < maxClients; i++) {
            if (clients[i] == null) return i;
        }

        return -1;
    }
}
