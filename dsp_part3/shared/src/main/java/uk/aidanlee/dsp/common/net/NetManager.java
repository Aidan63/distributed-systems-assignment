package uk.aidanlee.dsp.common.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Threaded network listener. NetManager listens on a provided or automatically assigned port.
 * When it receives data creates a packet instance and places it on the queue.
 * This queue provides thread safe access for the main thread to access incoming packets.
 */
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
     * All of the packets received by this thread.
     */
    private Queue<Packet> packets;

    /**
     * If the network manager is active.
     * This is false if a socket exception occured.
     */
    private boolean ready;

    /**
     * Creates a network manager which will listen for packets on an automatically assigned port.
     */
    public NetManager() {
        try {
            socket  = new DatagramSocket();
            buffer  = new byte[1400];
            ready   = true;

            setDaemon(true);
            setName("network manager");
        } catch (SocketException _ex) {
            System.out.println("Network Manager : Socket Exception - " + _ex.getMessage());
            ready = false;
        }

        packets = new ConcurrentLinkedQueue<>();
    }

    /**
     * Creates a network manager and attempts to listen for packets on a specified port.
     * @param _port The port to listen on.
     */
    public NetManager(int _port) {
        try {
            socket  = new DatagramSocket(_port);
            buffer  = new byte[1400];
            ready   = true;

            setDaemon(true);
            setName("network manager");
        } catch (SocketException _ex) {
            System.out.println("Network Manager : Socket Exception - " + _ex.getMessage());
            ready = false;
        }

        packets = new ConcurrentLinkedQueue<>();
    }

    /**
     * Thread safe access to the packets received by a concurrent linked queue.
     * @return Queue of packets.
     */
    public Queue<Packet> getPackets() {
        return packets;
    }

    /**
     * While the thread is not interrupted (while (true) loop) listen for packets on the socket and place them on the queue.
     */
    @Override
    public void run() {
        while (true) {
            if (!ready) continue;

            try {
                // Read data from the UDP socket
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Construct an endpoint instance from the sender
                // and a bit packer instance from the data.
                EndPoint from = new EndPoint(packet.getAddress(), packet.getPort());

                BitPacker bit = new BitPacker();
                bit.writeBytes(packet.getData(), packet.getLength());

                // Add the packet to the queue to be read by the main thread.
                packets.offer(new Packet(bit, from));
            } catch (IOException _ex) {
                System.out.println("Network Manager : IO Exception reading data from UDP socket - " + _ex.getMessage());
                break;
            }
        }
    }

    @Override
    public void interrupt() {
        System.out.println("Network Manager : Thread interrupted, shutting down.");
        if (ready) {
            socket.close();
        }

        super.interrupt();
    }

    /**
     * Sends data to the game server.
     * @param _packet The byte array to send.
     */
    public void send(Packet _packet) {
        try {
            byte[] raw = _packet.getData().toBytes();
            DatagramPacket packet = new DatagramPacket(raw, raw.length, _packet.getEndpoint().getAddress(), _packet.getEndpoint().getPort());
            socket.send(packet);
        } catch (IOException _ex) {
            System.out.println("Network Manager : IO Exception sending datagram packet to server - " + _ex.getMessage());
        }
    }
}
