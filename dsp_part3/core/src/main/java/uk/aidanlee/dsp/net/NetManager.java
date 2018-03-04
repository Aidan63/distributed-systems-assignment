package uk.aidanlee.dsp.net;

import uk.aidanlee.dsp.common.net.BitPacker;
import uk.aidanlee.dsp.common.net.EndPoint;
import uk.aidanlee.dsp.data.Game;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class NetManager extends Thread {
    /**
     * The socket used for sending and receiving data.
     */
    private DatagramSocket socket;

    /**
     * Buffer used to store incoming packet data.
     */
    private byte[] buffer;

    /**
     * The address and port of the server.
     */
    private EndPoint destination;

    /**
     * Creates a network manager and attempts to connect to the provided address and port.
     */
    public NetManager() {
        try {
            socket = new DatagramSocket();
            buffer = new byte[1400];
        } catch (SocketException _ex) {
            System.out.println("Network Manager : Socket Exception - " + _ex.getMessage());
            System.exit(-1);
        }
    }

    /**
     *
     */
    @Override
    public void run() {
        while (true) {
            try {
                // Read data from the UDP socket
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Construct an endpoint instance from the sender
                // and a bit packer instance from the data.
                EndPoint from = new EndPoint(packet.getAddress(), packet.getPort());

                BitPacker bit = new BitPacker();
                bit.writeBytes(packet.getData(), packet.getLength());

                // Pass the data and sender info to the connections class for processing.
                Game.connections.processPacket(bit);
            } catch (IOException _ex) {
                System.out.println("Network Manager : IO Exception reading data from UDP socket - " + _ex.getMessage());
                System.exit(-1);
            }
        }
    }

    /**
     * Sends data to the game server.
     * @param _data The byte array to send.
     */
    public synchronized void send(byte[] _data) {
        try {
            DatagramPacket packet = new DatagramPacket(_data, _data.length, destination.getAddress(), destination.getPort());
            socket.send(packet);
        } catch (IOException _ex) {
            System.out.println("Network Manager : IO Exception sending datagram packet to server - " + _ex.getMessage());
        }
    }

    /**
     * Sets the servers location.
     * @param destination The address and port of the server.
     */
    public synchronized void setDestination(EndPoint destination) {
        this.destination = destination;
    }
}
