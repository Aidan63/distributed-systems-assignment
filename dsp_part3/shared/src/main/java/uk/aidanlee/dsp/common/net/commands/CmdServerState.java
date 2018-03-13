package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.Packet;

public class CmdServerState extends Command {
    public final byte state;

    public CmdServerState(byte _state) {
        super(Command.SERVER_STATE);
        state    = _state;
    }

    public CmdServerState(Packet _packet) {
        super(Command.SERVER_STATE);
        state    = _packet.getData().readByte();
    }

    @Override
    public void add(Packet _packet) {
        _packet.getData().writeByte(Command.SERVER_STATE);
        _packet.getData().writeByte(state);
    }
}
