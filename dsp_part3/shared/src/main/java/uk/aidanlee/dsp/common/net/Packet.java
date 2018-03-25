package uk.aidanlee.dsp.common.net;

import uk.aidanlee.dsp.common.data.ClientInfo;
import uk.aidanlee.dsp.common.utils.ColorUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Packet {

    /**
     * Raw bytes packet data.
     */
    private BitPacker data;

    /**
     * The address and port this packet has arrived to or wants to be sent to.
     */
    private EndPoint endpoint;

    /**
     * Creates a packet with the specified payload and location.
     * @param _data Bytes data.
     * @param _ep   destination.
     */
    public Packet(BitPacker _data, EndPoint _ep) {
        data     = _data;
        endpoint = _ep;
    }

    /**
     * Creates an empty packet with just a destination.
     * @param _ep destination.
     */
    public Packet(EndPoint _ep) {
        data     = new BitPacker();
        endpoint = _ep;
    }

    public BitPacker getData() {
        return data;
    }

    public EndPoint getEndpoint() {
        return endpoint;
    }

    // OOB Packets Static Constructors

    // OOB Packet IDs
    public static final byte CONNECTION = 0;
    public static final byte CONNECTION_RESPONSE = 1;
    public static final byte DISCONNECTION = 2;
    public static final byte HEARTBEAT = 3;
    public static final byte DISCOVERY = 4;

    /**
     *
     * @return
     */
    public static Packet Connection(String _name, EndPoint _to) {
        BitPacker packer = new BitPacker();
        packer.writeBoolean(true);
        packer.writeByte(CONNECTION);
        packer.writeString(_name);

        return new Packet(packer, _to);
    }

    /**
     *
     * @return Byte array of the data.
     */
    public static Packet Disconnection(EndPoint _to) {
        BitPacker packer = new BitPacker();
        packer.writeBoolean(true);
        packer.writeByte(DISCONNECTION);

        return new Packet(packer, _to);
    }

    /**
     * Creates and returns the bytes of a connection accepted packet.
     * @param _newID      The ID for the newly connected client.
     * @param _maxClients The max number of clients this server allows.
     * @param _numClients The current number of clients on the server.
     * @param _map        The map index the server is using.
     * @param _current    Array of connected clients info.
     * @return
     */
    public static Packet ConnectionAccepted(int _newID, int _maxClients, int _numClients, int _map, ClientInfo[] _current, EndPoint _to) {
        BitPacker packer = new BitPacker();
        packer.writeBoolean(true);
        packer.writeByte(CONNECTION_RESPONSE);
        packer.writeBoolean(true);

        // Write initial server details and client info.
        packer.writeByte((byte) _newID);
        packer.writeByte((byte) _maxClients);
        packer.writeByte((byte) _map);
        packer.writeByte((byte) _numClients);

        for (ClientInfo info : _current) {
            packer.writeString(info.getName());
            packer.writeByte((byte) info.getId());
            packer.writeByte((byte) info.getShipIndex());
            packer.writeBoolean(info.isReady());

            // Write ship colour
            packer.writeByte((byte) (info.getShipColor()[0] * 255));
            packer.writeByte((byte) (info.getShipColor()[1] * 255));
            packer.writeByte((byte) (info.getShipColor()[2] * 255));

            // write trail colour
            packer.writeByte((byte) (info.getTrailColor()[0] * 255));
            packer.writeByte((byte) (info.getTrailColor()[1] * 255));
            packer.writeByte((byte) (info.getTrailColor()[2] * 255));
        }

        return new Packet(packer, _to);
    }

    /**
     * Creates a Connection Denied packet.
     * @return Byte array of the data.
     */
    public static Packet ConnectionDenied(EndPoint _to) {
        BitPacker packer = new BitPacker();
        packer.writeBoolean(true);
        packer.writeByte(CONNECTION_RESPONSE);
        packer.writeBoolean(false);

        return new Packet(packer, _to);
    }

    /**
     * Creates a heartbeat packet.
     * @return Byte array of the data.
     */
    public static Packet Heartbeat(EndPoint _to) {
        BitPacker packer = new BitPacker();
        packer.writeBoolean(true);
        packer.writeByte(HEARTBEAT);

        return new Packet(packer, _to);
    }

    /**
     * Creates a server discovery packet which will be broadcast across the LAN.
     * @param _serverName The name of the server.
     * @param _connected  The number of clients currently connected.
     * @param _max        The maximum number of clients
     * @return Packet with bytes data and endpoint location.
     */
    public static Packet Discovery(String _serverName, int _connected, int _max) {
        BitPacker packer = new BitPacker();
        packer.writeBoolean(true);
        packer.writeByte(DISCOVERY);

        packer.writeString(_serverName);
        packer.writeByte((byte) _connected);
        packer.writeByte((byte) _max);

        try {
            return new Packet(packer, new EndPoint(InetAddress.getByName("192.168.1.255"), 7778));
        } catch (UnknownHostException _ex) {
            System.out.println("Unknown host exception");
        }

        return null;
    }
}
