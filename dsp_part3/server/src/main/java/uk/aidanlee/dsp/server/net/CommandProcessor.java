package uk.aidanlee.dsp.server.net;

import uk.aidanlee.dsp.common.net.commands.*;
import uk.aidanlee.dsp.server.Server;

public class CommandProcessor {
    public static void parse(Command[] _cmds) {
        for (Command cmd : _cmds) {
            switch (cmd.id) {
                case Command.CHAT_MESSAGE:
                    cmdChatMessage((CmdChatMessage) cmd);
                    break;

                case Command.CLIENT_SETTINGS:
                case Command.CLIENT_INPUT:
                    Server.game.addCommand(cmd);
                    break;

                default:
                    System.out.println("Skipping unknown command");
            }
        }
    }

    private static void cmdChatMessage(CmdChatMessage _cmd) {
        Server.connections.addCommandAllExcept(_cmd, _cmd.clientID);
    }
}
