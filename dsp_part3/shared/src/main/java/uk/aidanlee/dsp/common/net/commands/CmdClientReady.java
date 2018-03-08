package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.Packet;

public class CmdClientReady extends Command {
    public final int clientID;

    public CmdClientReady(int _id) {
        super(Command.CLIENT_READY);
        clientID = _id;
    }

    public CmdClientReady(Packet _packet) {
        super(Command.CLIENT_READY);
        clientID = _packet.getData().readByte();
    }

    @Override
    public void add(Packet _packet) {
        _packet.getData().writeByte(Command.CLIENT_READY);
        _packet.getData().writeByte((byte) clientID);
    }
}
