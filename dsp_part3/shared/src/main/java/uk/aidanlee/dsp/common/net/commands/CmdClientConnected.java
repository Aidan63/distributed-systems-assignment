package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.data.ClientInfo;
import uk.aidanlee.dsp.common.net.BitPacker;

public class CmdClientConnected extends Command {
    /**
     * Newly connected clients info.
     */
    private ClientInfo client;

    /**
     * Creates a new client connected command.
     * @param _client Clients data.
     */
    public CmdClientConnected(ClientInfo _client) {
        super();
        client = _client;
    }

    public ClientInfo getClient() {
        return client;
    }

    @Override
    public void add(BitPacker _packet) {
        _packet.writeByte(Command.CLIENT_CONNECTED);

        _packet.writeString(client.getName());
        _packet.writeByte((byte) client.getId());
        _packet.writeByte((byte) client.getShipIndex());

        float[] color;

        // Write ship color
        color = client.getShipColor();
        _packet.writeByte((byte) (color[0] * 255));
        _packet.writeByte((byte) (color[1] * 255));
        _packet.writeByte((byte) (color[2] * 255));

        // Write trail color
        color = client.getTrailColor();
        _packet.writeByte((byte) (color[0] * 255));
        _packet.writeByte((byte) (color[1] * 255));
        _packet.writeByte((byte) (color[2] * 255));
    }
}
