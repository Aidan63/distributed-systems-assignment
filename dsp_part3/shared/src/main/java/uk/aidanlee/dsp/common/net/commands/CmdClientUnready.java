package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.Packet;

public class CmdClientUnready extends Command {
    public final int clientID;

    public CmdClientUnready(int _id) {
        super(Command.CLIENT_UNREADY);
        clientID = _id;
    }

    public CmdClientUnready(Packet _packet) {
        super(Command.CLIENT_UNREADY);
        clientID = _packet.getData().readByte();
    }

    @Override
    public void add(Packet _packet) {
        _packet.getData().writeByte(Command.CLIENT_UNREADY);
        _packet.getData().writeByte((byte) clientID);
    }
}
