package uk.aidanlee.dsp.server.net;

import uk.aidanlee.dsp.common.net.BitPacker;
import uk.aidanlee.dsp.common.net.EndPoint;
import uk.aidanlee.dsp.server.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class NetManager extends Thread {
    /**
     * Socket for sending and receiving bytes
     */
    private DatagramSocket socket;

    /**
     * Buffer incoming bytes are stored in.
     */
    private byte[] buffer;

    /**
     *
     * @param _port
     */
    public NetManager(int _port) {
        setName("thread network manager");

        try {
            socket  = new DatagramSocket(_port);
            buffer  = new byte[1400];
        } catch (SocketException _ex) {
            System.out.println("NetManager exception trying to open socket on port " + _port + " : " + _ex.getMessage());
            System.exit(-1);
        }
    }

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
                Server.connections.processPacket(bit, from);
            } catch (IOException _ex) {
                System.out.println("NetManager exception while reading UDP socket data : " + _ex.getMessage());
                System.exit(-1);
            }
        }
    }

    /**
     * Sends the input bytes to the provided end point.
     * @param _data        Byte array of data to send.
     * @param _destination Address and port to send to.
     */
    public synchronized void send(byte[] _data, EndPoint _destination) {
        try {
            DatagramPacket packet = new DatagramPacket(_data, _data.length, _destination.getAddress(), _destination.getPort());
            socket.send(packet);
        } catch (IOException _ex) {
            System.out.println("NetManager : IO Exception while attempting to send bytes.");
        }
    }

    /**
     * Class to store the received packet data and its sender.
     */
    public class Packet {
        private BitPacker data;
        private EndPoint sender;

        public Packet(BitPacker _data, EndPoint _sender) {
            data   = _data;
            sender = _sender;
        }

        public BitPacker getData() {
            return data;
        }

        public EndPoint getSender() {
            return sender;
        }
    }
}
