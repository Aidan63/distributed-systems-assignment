package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.BitPacker;

public class CmdClientDisconnected extends Command {
    /**
     * The ID of the disconnected client.
     */
    private int clientID;

    public CmdClientDisconnected(int _id) {
        clientID = _id;
    }

    @Override
    public void add(BitPacker _packet) {
        _packet.writeByte(Command.CLIENT_DISCONNECTED);
        _packet.writeByte((byte) clientID);
    }
}
