package uk.aidanlee.dsp.server.net;

import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.net.commands.*;
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

                case Command.CLIENT_READY:
                    cmdClientReady((CmdClientReady) cmd);
                    break;

                case Command.CLIENT_UNREADY:
                    cmdClientUnready((CmdClientUnready) cmd);
                    break;

                default:
                    System.out.println("Skipping unknown ncommand");
            }
        }
    }

    private static void cmdChatMessage(CmdChatMessage _cmd) {
        Server.connections.addCommandAllExcept(_cmd, _cmd.clientID);
    }

    private static void cmdClientUpdate(CmdClientUpdated _cmd) {
        Player p = Server.game.getPlayer(_cmd.clientID);
        p.setShipIndex (_cmd.index);
        p.setShipColor (_cmd.shipColor);
        p.setTrailColor(_cmd.trailColor);

        Server.connections.addCommandAllExcept(_cmd, _cmd.clientID);
    }

    private static void cmdClientReady(CmdClientReady _cmd) {
        Player p = Server.game.getPlayer(_cmd.clientID);
        p.setReady(true);

        Server.connections.addCommandAllExcept(_cmd, _cmd.clientID);
    }

    private static void cmdClientUnready(CmdClientUnready _cmd) {
        Player p = Server.game.getPlayer(_cmd.clientID);
        p.setReady(false);

        Server.connections.addCommandAllExcept(_cmd, _cmd.clientID);
    }
}
