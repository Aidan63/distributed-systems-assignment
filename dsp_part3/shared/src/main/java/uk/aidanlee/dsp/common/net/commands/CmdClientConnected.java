package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.data.ClientInfo;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.utils.ColorUtil;

public class CmdClientConnected extends Command {
    /**
     * Newly connected clients info.
     */
    public final ClientInfo client;

    /**
     * Creates a new client connected command.
     * @param _client Clients data.
     */
    public CmdClientConnected(ClientInfo _client) {
        super(Command.CLIENT_CONNECTED);
        client = _client;
    }

    /**
     *
     * @param _packet
     */
    public CmdClientConnected(Packet _packet, int _sentTime) {
        super(Command.CLIENT_CONNECTED, _sentTime);
        client = new ClientInfo(
                _packet.getData().readByte(),
                new Player(
                        _packet.getData().readString(),
                        _packet.getData().readByte(),
                        new float[] {
                                _packet.getData().readFloat(),
                                _packet.getData().readFloat(),
                                _packet.getData().readFloat(), 1
                        },
                        new float[] {
                                _packet.getData().readFloat(),
                                _packet.getData().readFloat(),
                                _packet.getData().readFloat(), 1
                        }
                )
        );
    }

    @Override
    public void add(Packet _packet) {
        _packet.getData().writeByte(Command.CLIENT_CONNECTED);

        _packet.getData().writeByte((byte) client.getId());
        _packet.getData().writeString(client.getName());
        _packet.getData().writeByte((byte) client.getShipIndex());

        float[] color;

        // Write ship color
        color = client.getShipColor();
        _packet.getData().writeFloat(color[0]);
        _packet.getData().writeFloat(color[1]);
        _packet.getData().writeFloat(color[2]);

        // Write trail color
        color = client.getTrailColor();
        _packet.getData().writeFloat(color[0]);
        _packet.getData().writeFloat(color[1]);
        _packet.getData().writeFloat(color[2]);
    }
}
