package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.Packet;

/**
 * Reads and write a server event in a netchan packet.
 */
public class CmdServerEvent extends Command {

    /**
     * ID of the server event.
     */
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
