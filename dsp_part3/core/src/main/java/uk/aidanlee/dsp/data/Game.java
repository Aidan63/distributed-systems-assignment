package uk.aidanlee.dsp.data;

import uk.aidanlee.dsp.common.net.NetChan;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.structural.StateMachine;
import uk.aidanlee.dsp.data.race.Race;
import uk.aidanlee.dsp.net.ConnectionState;
import uk.aidanlee.dsp.net.Connections;
import uk.aidanlee.dsp.common.net.NetManager;
import uk.aidanlee.dsp.states.ConnectingState;
import uk.aidanlee.dsp.states.LobbyState;
import uk.aidanlee.dsp.states.MenuState;

import java.util.Timer;
import java.util.TimerTask;

public class Game {
    /**
     * Thread which packets can be posted to for sending and which received packets from a UDP socket.
     */
    public static NetManager netManager;

    /**
     * Holds information about all clients connected to the server and processes OOB Packets.
     */
    public static Connections connections;

    /**
     * Processes net channel packets coming in and going out of this client.
     */
    public static NetChan netChan;

    /**
     * Access to the servers chat log.
     */
    public static ChatLog chatlog;

    /**
     * The state machine for the game.
     */
    public static StateMachine state;

    /**
     * Access to the race structure for when in game.
     */
    public static Race race;

    /**
     * Heartbeat time to stop the server timing us out.
     */
    private static Timer heartbeat;

    /**
     *
     */
    public static void start() {
        state = new StateMachine();
        state.add(new MenuState("menu"));
        state.add(new ConnectingState("connecting"));
        state.add(new LobbyState("lobby"));
        state.set("menu", null, null);
    }

    public static void update() {
        // Read packets
        if (connections != null) {
            connections.readPackets();
        }

        state.update();
    }

    public static void render() {
        state.render();
    }

    public static void stop() {
        //
    }

    /**
     *
     */
    public static void netStart() {
        netManager = new NetManager();
        netManager.start();

        connections = new Connections();
        netChan     = new NetChan(null);
        chatlog     = new ChatLog();

        startHeartbeat();
    }

    /**
     *
     */
    public static void netStop() {
        if (netManager != null) {
            netManager.interrupt();
            netManager = null;

            connections = null;
            netChan     = null;
            chatlog     = null;

            stopHeartbeat();
        }
    }

    /**
     *
     */
    private static void startHeartbeat() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // If we are connected, send a heart beat packet to stop the server timing us out.
                if (Game.connections.getState() == ConnectionState.Connected) {
                    Game.netManager.send(Packet.Heartbeat(Game.connections.getServer()));
                }
            }
        };

        // Schedule a heart beat packet to be sent every second.
        heartbeat = new Timer();
        heartbeat.scheduleAtFixedRate(task, 1000, 1000);
    }

    /**
     *
     */
    private static void stopHeartbeat() {
        heartbeat.cancel();
        heartbeat.purge();
        heartbeat = null;
    }
}
