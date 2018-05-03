package uk.aidanlee.dsp.states;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import uk.aidanlee.dsp.common.net.EndPoint;
import uk.aidanlee.dsp.common.net.NetChan;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.net.commands.*;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.common.structural.StateMachine;
import uk.aidanlee.dsp.data.ChatLog;
import uk.aidanlee.dsp.data.Resources;
import uk.aidanlee.dsp.data.events.*;
import uk.aidanlee.dsp.data.states.LobbyData;
import uk.aidanlee.dsp.net.ConnectionResponse;
import uk.aidanlee.dsp.states.game.LobbyState;
import uk.aidanlee.dsp.states.game.RaceState;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Main game state. When the client is connected to a server it is in this state.
 */
public class GameState extends State {

    /**
     * Access to the clients event bus.
     */
    private EventBus events;

    /**
     * IP address and port of the server.
     */
    private EndPoint server;

    /**
     * Clients netchannel for send commands to the server.
     */
    private NetChan netChan;

    /**
     * Data on all clients connected to the server.
     */
    private Player[] players;

    /**
     * chat log for text chat between players in the game lobby.
     */
    private ChatLog chat;

    /**
     * All resources (images, track data, etc) used by the client.
     */
    private Resources resources;

    /**
     * Game sub state. Contains a lobby and race state.
     */
    private StateMachine gameState;

    /**
     * Timer for sending out heartbeats packets. Timer is repeatedly called.
     */
    private Timer heartbeatSender;

    /**
     * If this timer triggers we disconnect as the server has not sent a heartbeat in 5 seconds.
     * This resets on every heartbeat packet received.
     */
    private Timer heartbeatTimeout;

    public GameState(String _name, EventBus _events) {
        super(_name);
        events = _events;
    }

    @Override
    public void onEnter(Object _enterWith) {
        events.register(this);

        // Cast the entered object to the connection response class
        ConnectionResponse response = (ConnectionResponse) _enterWith;

        // Read some basic data from the response
        server           = response.getEp();
        int id           = response.getPacket().getData().readByte();
        int maxClients   = response.getPacket().getData().readByte();
        int mapIndex     = response.getPacket().getData().readByte();
        int numConnected = response.getPacket().getData().readByte();

        // Setup all of the game needed classes
        netChan   = new NetChan(server);
        players   = new Player[maxClients];
        chat      = new ChatLog();
        resources = new Resources();

        // Read all of the other connected player information
        readPlayers(response.getPacket(), numConnected);

        // Create the game state machine.
        gameState = new StateMachine();
        gameState.add(new LobbyState("lobby", resources, events, server));
        gameState.add(new RaceState("race", resources, events));
        gameState.set("lobby", new LobbyData(chat, players, id), null);

        // Setup the heartbeat timer.
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                events.post(new EvSendPacket(Packet.Heartbeat(server)));
            }
        };

        heartbeatSender = new Timer();
        heartbeatSender.scheduleAtFixedRate(task, 1000, 1000);

        // Setup the server timeout checker.
        resetHeartbeatTimeout();
    }

    @Override
    public void onLeave(Object _leaveWith) {
        events.unregister(this);
        gameState.unset(null);

        heartbeatTimeout.cancel();
        heartbeatSender.cancel();

        resources.dispose();
    }

    @Override
    public void onUpdate() {
        // Read and process and commands from the server.
        gameState.update();

        // Sends out a netchan packet every step.
        Packet netchanPacket = netChan.send();
        if (netchanPacket != null) {
            events.post(new EvSendPacket(netchanPacket));
        }

    }

    @Override
    public void onRender() {
        gameState.render();
    }

    // Event Functions

    /**
     * State change events are called by the sub state to change the main event.
     * @param _stateData State to switch to along with the data to enter and leave with.
     */
    @Subscribe
    public void onStateChange(EvStateChange _stateData) {
        machine.set(_stateData.state, _stateData.enterWith, _stateData.leaveWith);
    }

    /**
     * When we receive a netchan packet, process it and post all the containing commands into the event bus.
     * @param _event Event containing the netchan packet.
     */
    @Subscribe
    public void onNetChanMessage(EvNetChanData _event) {
        for (Command cmd : netChan.receive(_event.packet)) {
            events.post(cmd);
        }
    }

    /**
     * Clients only care about receiving two OOB packet types once they are in game.
     * @param _event Event containing the OOB Packet.
     */
    @Subscribe
    public void onOOBMessage(EvOOBData _event) {
        switch (_event.packet.getData().readByte()) {
            case Packet.HEARTBEAT:
                resetHeartbeatTimeout();
                break;

            case Packet.DISCONNECTION:
                machine.set("menu", null, null);
                break;
        }
    }

    /**
     * Event posts the included command into the netchans reliable command buffer.
     * @param _event Event containing the command to reliably send.
     */
    @Subscribe
    public void onReliableCommand(EvAddReliableCommand _event) {
        netChan.addCommand(_event.cmd);
    }

    /**
     * Event posts the included command into the netchan.
     * @param _event Event containing the command to unreliably send.
     */
    @Subscribe
    public void onUnreliableCommand(EvAddUnreliableCommand _event) {
        netChan.addReliableCommand(_event.cmd);
    }

    /**
     * Event is received when a connection command is received in a netchan packet.
     * Contains data about a new client connection. It reads the data and adds them to the player array.
     * @param _cmd Command containing newly joined clients info.
     */
    @Subscribe
    public void onClientConnected(CmdClientConnected _cmd) {
        Player player = new Player(_cmd.client.getName());
        player.setShipIndex(_cmd.client.getShipIndex());
        player.setShipColor(_cmd.client.getShipColor());
        player.setTrailColor(_cmd.client.getTrailColor());

        players[_cmd.client.getId()] = player;
        chat.addServerMessage(_cmd.client.getName() + " has joined");
    }

    /**
     * Event is received when a disconnection command is received in a netchan packet.
     * Command contains the ID of the disconnected client so it can be removed from our player array.
     * @param _cmd Command containing the ID of the disconnected client.
     */
    @Subscribe
    public void onClientDisconnected(CmdClientDisconnected _cmd) {
        if (players[_cmd.clientID] == null) return;

        chat.addServerMessage(players[_cmd.clientID].getName() + " has left");
        players[_cmd.clientID] = null;
    }

    /**
     * Event is received when a new chat message is received in a netchan packet.
     * @param _cmd Command containing message string and the ID of the client who sent it.
     */
    @Subscribe
    public void onChatMessage(CmdChatMessage _cmd) {
        chat.addPlayerMessage(players[_cmd.clientID].getName(), _cmd.message);
    }

    // Internal Functions

    /**
     * Reads a specific number of player data from a packet.
     * @param _packet     Packet to raad player data from.
     * @param _numPlayers Number of players to read.
     */
    private void readPlayers(Packet _packet, int _numPlayers) {
        for (int i = 0; i < _numPlayers; i++) {
            // Read Basic Info
            String  name  = _packet.getData().readString();
            int     id    = _packet.getData().readByte();
            int     idx   = _packet.getData().readByte();
            boolean ready = _packet.getData().readBoolean();

            // Read ship color
            float sR = (_packet.getData().readByte() & 0xFF) / 255f;
            float sG = (_packet.getData().readByte() & 0xFF) / 255f;
            float sB = (_packet.getData().readByte() & 0xFF) / 255f;

            // Read trail color
            float tR = (_packet.getData().readByte() & 0xFF) / 255f;
            float tG = (_packet.getData().readByte() & 0xFF) / 255f;
            float tB = (_packet.getData().readByte() & 0xFF) / 255f;

            // Create a new client with the read data.
            Player player = new Player(name);
            player.setShipIndex(idx);
            player.setShipColor(new float[] { sR, sG, sB, 1 });
            player.setTrailColor(new float[] { tR, tG, tB, 1 });
            player.setReady(ready);

            // Add the client into the array.
            players[id] = player;
        }
    }

    /**
     * Resets the heartbeat timeout, creates a timer if none exists.
     * When a timeout occurs return to the menu.
     */
    private void resetHeartbeatTimeout() {
        if (heartbeatTimeout != null) {
            heartbeatTimeout.cancel();
            heartbeatTimeout = null;
        }

        // Start the timout timer.
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Server timeout");
                machine.set("menu", null, null);
            }
        };
        heartbeatTimeout = new Timer();
        heartbeatTimeout.schedule(task, 5000);
    }
}
