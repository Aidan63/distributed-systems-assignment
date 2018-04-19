package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.Packet;

public class CmdPlayerFinished extends Command {
    public final int clientID;

    public CmdPlayerFinished(int _clientID) {
        super(Command.PLAYER_FINISHED);
        clientID = _clientID;
    }

    public CmdPlayerFinished(Packet _packet, int _sentTime) {
        super(Command.PLAYER_FINISHED, _sentTime);
        clientID = _packet.getData().readByte();
    }

    @Override
    public void add(Packet _packet) {
        _packet.getData().writeByte(Command.PLAYER_FINISHED);
        _packet.getData().writeByte((byte) clientID);
    }
}
