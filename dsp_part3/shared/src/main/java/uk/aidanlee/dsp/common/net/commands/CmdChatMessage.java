package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.Packet;

public class CmdChatMessage extends Command {

    /**
     * The ID of the client who sent this message.
     */
    public final int clientID;

    /**
     * The chat message string.
     */
    public final String message;

    /**
     *
     * @param _id
     * @param _msg
     */
    public CmdChatMessage(int _id, String _msg) {
        super(Command.CHAT_MESSAGE);
        clientID = _id;
        message  = _msg;
    }

    /**
     *
     * @param _packet
     */
    public CmdChatMessage(Packet _packet, int _sentTime) {
        super(Command.CHAT_MESSAGE, _sentTime);
        clientID = _packet.getData().readByte();
        message  = _packet.getData().readString();
    }

    @Override
    public void add(Packet _packet) {
        _packet.getData().writeByte(Command.CHAT_MESSAGE);
        _packet.getData().writeByte((byte) clientID);
        _packet.getData().writeString(message);
    }
}
