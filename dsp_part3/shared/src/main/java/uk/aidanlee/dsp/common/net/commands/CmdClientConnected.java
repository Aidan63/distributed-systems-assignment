package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.data.ClientInfo;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.net.Player;

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
                                (_packet.getData().readByte() & 0xFF) / 255f,
                                (_packet.getData().readByte() & 0xFF) / 255f,
                                (_packet.getData().readByte() & 0xFF) / 255f, 1
                        },
                        new float[] {
                                (_packet.getData().readByte() & 0xFF) / 255f,
                                (_packet.getData().readByte() & 0xFF) / 255f,
                                (_packet.getData().readByte() & 0xFF) / 255f, 1
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

        // Write ship colour
        _packet.getData().writeByte((byte) (client.getShipColor()[0] * 255));
        _packet.getData().writeByte((byte) (client.getShipColor()[1] * 255));
        _packet.getData().writeByte((byte) (client.getShipColor()[2] * 255));

        // write trail colour
        _packet.getData().writeByte((byte) (client.getTrailColor()[0] * 255));
        _packet.getData().writeByte((byte) (client.getTrailColor()[1] * 255));
        _packet.getData().writeByte((byte) (client.getTrailColor()[2] * 255));
    }
}
