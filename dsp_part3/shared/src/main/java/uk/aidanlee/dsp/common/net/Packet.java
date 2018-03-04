package uk.aidanlee.dsp.common.net;

import uk.aidanlee.dsp.common.data.ClientInfo;

public class Packet {
    // OOB Packet IDs
    public static final byte CONNECTION = 0;
    public static final byte CONNECTION_RESPONSE = 1;
    public static final byte DISCONNECTION = 2;
    public static final byte HEARTBEAT = 3;

    // NetChan Packet IDs

    // Packet Constructors

    // OOB Packets

    /**
     *
     * @return
     */
    public static byte[] Connection(String _name) {
        BitPacker packer = new BitPacker();
        packer.writeBoolean(true);
        packer.writeByte(CONNECTION);
        packer.writeString(_name);

        return packer.toBytes();
    }

    /**
     *
     * @return Byte array of the data.
     */
    public static byte[] Disconnection() {
        BitPacker packer = new BitPacker();
        packer.writeBoolean(true);
        packer.writeByte(DISCONNECTION);

        return packer.toBytes();
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
    public static byte[] ConnectionAccepted(int _newID, int _maxClients, int _numClients, int _map, ClientInfo[] _current) {
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

        return packer.toBytes();
    }

    /**
     * Creates a Connection Denied packet.
     * @return Byte array of the data.
     */
    public static byte[] ConnectionDenied() {
        BitPacker packer = new BitPacker();
        packer.writeBoolean(true);
        packer.writeByte(CONNECTION_RESPONSE);
        packer.writeBoolean(false);

        return packer.toBytes();
    }

    /**
     * Creates a heartbeat packet.
     * @return Byte array of the data.
     */
    public static byte[] Heartbeat() {
        BitPacker packer = new BitPacker();
        packer.writeBoolean(true);
        packer.writeByte(HEARTBEAT);

        return packer.toBytes();
    }

    // NetChan Packets
}
