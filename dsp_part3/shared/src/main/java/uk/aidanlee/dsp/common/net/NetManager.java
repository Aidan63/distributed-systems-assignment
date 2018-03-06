package uk.aidanlee.dsp.common.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
     * Creates a network manager which will listen for packets on an automatically assigned port.
     */
    public NetManager() {
        try {
            socket  = new DatagramSocket();
            buffer  = new byte[1400];
            packets = new ConcurrentLinkedQueue<>();
        } catch (SocketException _ex) {
            System.out.println("Network Manager : Socket Exception - " + _ex.getMessage());
        }
    }

    /**
     * Creates a network manager and attempts to listen for packets on a specified port.
     * @param _port The port to listen on.
     */
    public NetManager(int _port) {
        try {
            socket  = new DatagramSocket(_port);
            buffer  = new byte[1400];
            packets = new ConcurrentLinkedQueue<>();
        } catch (SocketException _ex) {
            System.out.println("Network Manager : Socket Exception - " + _ex.getMessage());
        }
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
        while (!Thread.currentThread().isInterrupted()) {
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
                packets.offer(new Packet(bit.toBytes(), from));
            } catch (IOException _ex) {
                System.out.println("Network Manager : IO Exception reading data from UDP socket - " + _ex.getMessage());
            }
        }
    }

    @Override
    public void interrupt() {
        System.out.println("Network Manager : Thread interrupted, shutting down.");
        socket.close();

        super.interrupt();
    }

    /**
     * Sends data to the game server.
     * @param _packet The byte array to send.
     */
    public void send(Packet _packet) {
        try {
            DatagramPacket packet = new DatagramPacket(_packet.getData(), _packet.getData().length, _packet.getEndpoint().getAddress(), _packet.getEndpoint().getPort());
            socket.send(packet);
        } catch (IOException _ex) {
            System.out.println("Network Manager : IO Exception sending datagram packet to server - " + _ex.getMessage());
        }
    }
}
