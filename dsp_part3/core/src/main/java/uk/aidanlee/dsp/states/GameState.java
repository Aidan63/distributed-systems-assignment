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

public class GameState extends State {

    private EventBus events;

    private EndPoint server;

    //

    private NetChan netChan;

    private Player[] players;

    private ChatLog chat;

    private Resources resources;

    //

    private StateMachine gameState;

    private Timer heartbeatSender;

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
        heartbeatTimeout.cancel();
        heartbeatSender.cancel();
        resources.dispose();
    }

    @Override
    public void onUpdate() {
        // Read and process and commands from the server.
        gameState.update();

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

    @Subscribe
    public void onNetChanMessage(EvNetChanData _event) {
        for (Command cmd : netChan.receive(_event.packet)) {
            events.post(cmd);
        }
    }

    @Subscribe
    public void onOOBMessage(EvOOBData _event) {
        switch (_event.packet.getData().readByte()) {
            case Packet.HEARTBEAT:
                resetHeartbeatTimeout();
                break;

            case Packet.DISCONNECTION:
                changeState("menu", null, null);
                break;
        }
    }

    @Subscribe
    public void onReliableCommand(EvAddReliableCommand _event) {
        netChan.addCommand(_event.cmd);
    }

    @Subscribe
    public void onUnreliableCommand(EvAddUnreliableCommand _event) {
        netChan.addReliableCommand(_event.cmd);
    }

    @Subscribe
    public void onClientConnected(CmdClientConnected _cmd) {
        Player player = new Player(_cmd.client.getName());
        player.setShipIndex(_cmd.client.getShipIndex());
        player.setShipColor(_cmd.client.getShipColor());
        player.setTrailColor(_cmd.client.getTrailColor());

        players[_cmd.client.getId()] = player;
        chat.addServerMessage(_cmd.client.getName() + " has joined");
    }

    @Subscribe
    public void onClientDisconnected(CmdClientDisconnected _cmd) {
        if (players[_cmd.clientID] == null) return;

        chat.addServerMessage(players[_cmd.clientID].getName() + " has left");
        players[_cmd.clientID] = null;

        //gameState.getEvents().post(_cmd);
    }

    @Subscribe
    public void onChatMessage(CmdChatMessage _cmd) {
        chat.addPlayerMessage(players[_cmd.clientID].getName(), _cmd.message);
    }

    // Internal Functions

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
                changeState("menu", null, null);
            }
        };
        heartbeatTimeout = new Timer();
        heartbeatTimeout.schedule(task, 5000);
    }
}
