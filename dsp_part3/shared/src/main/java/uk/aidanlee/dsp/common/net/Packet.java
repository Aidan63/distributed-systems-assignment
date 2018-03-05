package uk.aidanlee.dsp.common.net;

import uk.aidanlee.dsp.common.data.ClientInfo;

public class Packet {

    // OOB Packet IDs
    public static final byte CONNECTION = 0;
    public static final byte CONNECTION_RESPONSE = 1;
    public static final byte DISCONNECTION = 2;
    public static final byte HEARTBEAT = 3;

    // Packet

    /**
     * Raw bytes packet data.
     */
    private byte[] data;

    /**
     * The address and port this packet has arrived to or wants to be sent to.
     */
    private EndPoint endpoint;

    public Packet(byte[] _data, EndPoint _ep) {
        data     = _data;
        endpoint = _ep;
    }

    public byte[] getData() {
        return data;
    }

    public EndPoint getEndpoint() {
        return endpoint;
    }

    // OOB Packets Constructors

    /**
     *
     * @return
     */
    public static Packet Connection(String _name, EndPoint _to) {
        BitPacker packer = new BitPacker();
        packer.writeBoolean(true);
        packer.writeByte(CONNECTION);
        packer.writeString(_name);

        return new Packet(packer.toBytes(), _to);
    }

    /**
     *
     * @return Byte array of the data.
     */
    public static Packet Disconnection(EndPoint _to) {
        BitPacker packer = new BitPacker();
        packer.writeBoolean(true);
        packer.writeByte(DISCONNECTION);

        return new Packet(packer.toBytes(), _to);
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

            float[] color;

            // Write ship color
            color = info.getShipColor();
            packer.writeByte((byte) (color[0] * 255));
            packer.writeByte((byte) (color[1] * 255));
            packer.writeByte((byte) (color[2] * 255));

            // Write trail color
            color = info.getTrailColor();
            packer.writeByte((byte) (color[0] * 255));
            packer.writeByte((byte) (color[1] * 255));
            packer.writeByte((byte) (color[2] * 255));
        }

        return new Packet(packer.toBytes(), _to);
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

        return new Packet(packer.toBytes(), _to);
    }

    /**
     * Creates a heartbeat packet.
     * @return Byte array of the data.
     */
    public static Packet Heartbeat(EndPoint _to) {
        BitPacker packer = new BitPacker();
        packer.writeBoolean(true);
        packer.writeByte(HEARTBEAT);

        return new Packet(packer.toBytes(), _to);
    }

    // NetChan Packets
}
