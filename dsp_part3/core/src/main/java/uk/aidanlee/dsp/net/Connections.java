package uk.aidanlee.dsp.net;

import com.google.common.eventbus.EventBus;
import uk.aidanlee.dsp.Client;
import uk.aidanlee.dsp.common.net.EndPoint;
import uk.aidanlee.dsp.common.net.NetChan;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.net.commands.Command;

import java.util.*;

public class Connections {
    /**
     * Servers endpoint location.
     */
    private EndPoint server;

    /**
     *
     */
    private NetChan netChan;

    /**
     * Timer which will reset each time we receive a heartbeat OOB packet.
     * If it triggers we will disconnect as the server is no longer active.
     */
    private Timer heartbeat;

    /**
     * List of net chan commands which have been received.
     */
    private EventBus events;

    /**
     * Creates a new connection class to
     * @param _server
     */
    public Connections(EndPoint _server, EventBus _events) {
        server  = _server;
        netChan = new NetChan(server);
        events  = _events;

        resetTimeout();
    }

    // Public API

    /**
     *
     * @return
     */
    public EndPoint getServer() {
        return server;
    }

    /**
     *
     * @return
     */
    public NetChan getNetChan() {
        return netChan;
    }

    /**
     *
     * @return
     */
    public void update() {
        // Read packets from the net manager
        Packet pck = Client.netManager.getPackets().poll();
        while (pck != null) {
            processPacket(pck);
            pck = Client.netManager.getPackets().poll();
        }

        // Generate a net chan packet and send it to the server.
        Packet netChanPacket = netChan.send();
        if (netChanPacket != null) {
            Client.netManager.send(netChanPacket);
        }
    }

    /**
     * Reads the initial data from a packet received from the NetManager.
     * @param _packet Packet class containing the byte data and sender.
     */
    private void processPacket(Packet _packet) {

        // First bit indicates if the packet is OOB (1) or netchan (0).
        if (_packet.getData().readBoolean()) {
            processOOBPacket(_packet);
        } else {
            // Send netchan packet to the net channel and parse any commands
            //commands.addAll(Arrays.asList(netChan.receive(_packet)));
            for (Command cmd : netChan.receive(_packet)) {
                events.post(cmd);
            }
        }
    }

    /**
     * Reads the first byte of the OOB Packet to figure out which type it is.
     * @param _packet OOB packet.
     */
    private void processOOBPacket(Packet _packet) {
        switch (_packet.getData().readByte()) {
            case Packet.DISCONNECTION:
                readDisconnection();
                break;

            case Packet.HEARTBEAT:
                resetTimeout();

            default:
                //
                break;
        }
    }


    /**
     * If we receive an OOB disconnection packet when we're connected that means we've been disconnected by the server.
     */
    private void readDisconnection() {
        System.out.println("We have been disconnected / kicked by the server");
        Client.clientState.set("menu", null, null);
    }

    /**
     * Resets the heartbeat timer once we're received data from the server.
     */
    private void resetTimeout() {
        if (heartbeat != null) {
            heartbeat.cancel();
            heartbeat = null;
        }

        // Start the timout timer.
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Server timeout.");
                Client.clientState.set("menu", null, null);
            }
        };
        heartbeat = new Timer();
        heartbeat.schedule(task, 5000);
    }

    public void dispose() {
        heartbeat.cancel();
    }
}
