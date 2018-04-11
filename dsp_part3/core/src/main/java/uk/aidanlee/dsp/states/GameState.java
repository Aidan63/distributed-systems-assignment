package uk.aidanlee.dsp.states;

import com.google.common.eventbus.Subscribe;
import uk.aidanlee.dsp.Client;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.net.commands.*;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.common.structural.StateMachine;
import uk.aidanlee.dsp.data.ChatLog;
import uk.aidanlee.dsp.data.states.LobbyData;
import uk.aidanlee.dsp.net.ConnectionResponse;
import uk.aidanlee.dsp.net.Connections;
import uk.aidanlee.dsp.states.game.LobbyState;
import uk.aidanlee.dsp.states.game.RaceState;

import java.util.Timer;
import java.util.TimerTask;

public class GameState extends State {
    /**
     * Manages the connection to the server.
     * Will send and read OOB and netchan commands.
     */
    private Connections connections;

    /**
     * Array of all players connected to this server.
     * Length of the array is the max clients connected.
     * Index values are the clients ID.
     */
    private Player[] players;

    /**
     * The local players ID.
     */
    private int ourID;

    /**
     * Chat log to store all messages.
     */
    private ChatLog chat;

    /**
     * Game state machine. Has a lobby and race state.
     */
    private StateMachine gameState;

    /**
     * Repeatedly triggering timer to send an OOB heartbeat to the server.
     */
    private Timer heartbeat;

    public GameState(String _name) {
        super(_name);
    }

    @Override
    public void onEnter(Object _enterWith) {
        // Cast the entered object to the connection response class
        ConnectionResponse response = (ConnectionResponse) _enterWith;

        // Read some basic data from the response
        int id           = response.getPacket().getData().readByte();
        int maxClients   = response.getPacket().getData().readByte();
        int mapIndex     = response.getPacket().getData().readByte();
        int numConnected = response.getPacket().getData().readByte();

        // Setup all of the game needed classes
        connections = new Connections(response.getEp(), machine.getEvents());
        players     = new Player[maxClients];
        chat        = new ChatLog();

        // Read all of the other connected player information
        readPlayers(response.getPacket(), numConnected);
        ourID = id;

        // Create the game state machine.
        gameState = new StateMachine();
        gameState.add(new LobbyState("lobby"));
        gameState.add(new RaceState("race"));
        gameState.set("lobby", new LobbyData(connections.getNetChan(), chat, players, ourID), null);

        // Setup the heartbeat timer.
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Client.netManager.send(Packet.Heartbeat(connections.getServer()));
            }
        };

        heartbeat = new Timer();
        heartbeat.scheduleAtFixedRate(task, 1000, 1000);
    }

    @Override
    public void onLeave(Object _leaveWith) {
        super.onLeave(_leaveWith);
        heartbeat.cancel();
        connections.dispose();
    }

    @Override
    public void onUpdate() {
        // Read and process and commands from the server.
        connections.update();
        gameState.update();

    }

    @Override
    public void onRender() {
        gameState.render();
    }

    // Internal Functions

    /**
     * Reads about about all of the existing players in the game.
     * @param _packet     Packet to read data from.
     * @param _numPlayers The number of clients currently connected.
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

    @Subscribe
    public void eventClientConnected(CmdClientConnected _cmd) {
        Player player = new Player(_cmd.client.getName());
        player.setShipIndex(_cmd.client.getShipIndex());
        player.setShipColor(_cmd.client.getShipColor());
        player.setTrailColor(_cmd.client.getTrailColor());

        players[_cmd.client.getId()] = player;
        chat.addServerMessage(_cmd.client.getName() + " has joined");
    }

    @Subscribe
    public void eventClientDisconnected(CmdClientDisconnected _cmd) {
        if (players[_cmd.clientID] == null) return;

        chat.addServerMessage(players[_cmd.clientID].getName() + " has left");
        players[_cmd.clientID] = null;

        gameState.getEvents().post(_cmd);
    }

    @Subscribe
    public void eventChatMessage(CmdChatMessage _cmd) {
        chat.addPlayerMessage(players[_cmd.clientID].getName(), _cmd.message);
    }

    @Subscribe
    public void eventSnapshot(CmdSnapshot _cmd) {
        System.out.println("event snapshot");
        gameState.getEvents().post(_cmd);
    }

    @Subscribe
    public void eventServerState(CmdServerEvent _cmd) {
        gameState.getEvents().post(_cmd);
    }
}
