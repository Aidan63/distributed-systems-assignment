package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.Packet;

/**
 * All connected clients get sent this reliable command when another client disconnects.
 */
public class CmdClientDisconnected extends Command {
    /**
     * The ID of the disconnected client.
     */
    public final int clientID;

    public CmdClientDisconnected(int _id) {
        super(Command.CLIENT_DISCONNECTED);
        clientID = _id;
    }

    public CmdClientDisconnected(Packet _packet, int _sentTime) {
        super(Command.CLIENT_DISCONNECTED, _sentTime);
        clientID = _packet.getData().readByte();
    }

    @Override
    public void add(Packet _packet) {
        _packet.getData().writeByte(Command.CLIENT_DISCONNECTED);
        _packet.getData().writeByte((byte) clientID);
    }
}
