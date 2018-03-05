package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.BitPacker;

public class CmdChatMessage extends Command {
    private int id;
    private String message;

    public CmdChatMessage(int _id, String _msg) {
        super();
        id      = _id;
        message = _msg;
    }

    @Override
    public void add(BitPacker _packet) {
        _packet.writeByte(Command.CHAT_MESSAGE);
        _packet.writeByte((byte) id);
        _packet.writeString(message);
    }
}
