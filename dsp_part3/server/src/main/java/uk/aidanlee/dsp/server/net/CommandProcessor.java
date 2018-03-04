package uk.aidanlee.dsp.server.net;

import uk.aidanlee.dsp.common.net.BitPacker;
import uk.aidanlee.dsp.common.net.commands.CmdChatMessage;
import uk.aidanlee.dsp.common.net.commands.Command;
import uk.aidanlee.dsp.server.Server;

public class CommandProcessor {
    public static void parse(BitPacker _data) {

        for (int i = 0; i < _data.readByte(); i++) {
            switch (_data.readByte()) {
                case Command.CHAT_MESSAGE:
                    cmdChatMessage(_data);
                    break;
            }
        }
    }

    private static void cmdChatMessage(BitPacker _data) {
        // Read the text message and simply relay it to all clients reliably.

        int    id  = _data.readByte();
        String txt = _data.readString();

        Command cmd = new CmdChatMessage(id, txt);
        Server.connections.addCommandAll(cmd);
    }
}
