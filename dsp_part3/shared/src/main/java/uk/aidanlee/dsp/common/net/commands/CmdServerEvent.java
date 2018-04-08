package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.Packet;

public class CmdServerEvent extends Command {
    public final byte state;

    public CmdServerEvent(byte _state) {
        super(Command.SERVER_STATE);
        state    = _state;
    }

    public CmdServerEvent(Packet _packet, int _sentTime) {
        super(Command.SERVER_STATE, _sentTime);
        state    = _packet.getData().readByte();
    }

    @Override
    public void add(Packet _packet) {
        _packet.getData().writeByte(Command.SERVER_STATE);
        _packet.getData().writeByte(state);
    }
}