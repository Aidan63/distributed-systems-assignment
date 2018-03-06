package uk.aidanlee.dsp.server.net;

import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.net.commands.CmdChatMessage;
import uk.aidanlee.dsp.common.net.commands.CmdClientUpdated;
import uk.aidanlee.dsp.common.net.commands.Command;
import uk.aidanlee.dsp.server.Server;

public class CommandProcessor {
    public static void parse(Command[] _cmds) {
        for (Command cmd : _cmds) {
            switch (cmd.id) {
                case Command.CHAT_MESSAGE:
                    cmdChatMessage((CmdChatMessage) cmd);
                    break;

                case Command.CLIENT_UPDATED:
                    cmdClientUpdate((CmdClientUpdated) cmd);
                    break;

                default:
                    System.out.println("Skipping unknown ncommand");
            }
        }
    }

    private static void cmdChatMessage(CmdChatMessage _cmd) {
        Server.connections.addReliableCommandAllExcept(_cmd, _cmd.clientID);
    }

    private static void cmdClientUpdate(CmdClientUpdated _cmd) {
        Player p = Server.game.getPlayer(_cmd.clientID);
        p.setShipIndex (_cmd.index);
        p.setShipColor (_cmd.shipColor);
        p.setTrailColor(_cmd.trailColor);

        Server.connections.addCommandAllExcept(_cmd, _cmd.clientID);
    }
}
