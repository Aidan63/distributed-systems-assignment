package uk.aidanlee.dsp.server.net;

import uk.aidanlee.dsp.common.components.InputComponent;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.net.commands.*;
import uk.aidanlee.dsp.common.structural.ec.Entity;
import uk.aidanlee.dsp.server.Server;

import java.awt.color.ICC_ColorSpace;

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

                case Command.CLIENT_INPUT:
                    cmdClientInput((CmdClientInput) cmd);
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

    private static void cmdClientInput(CmdClientInput _cmd) {
        // Update the players input based on the remote client.
        if (!Server.state.getActiveStateName().equals("game")) return;

        Entity e = Server.race.craft.getRemotePlayers()[_cmd.clientID];
        InputComponent ip = (InputComponent) e.get("input");

        ip.accelerate = _cmd.accel;
        ip.decelerate = _cmd.decel;
        ip.steerLeft  = _cmd.steerLeft;
        ip.steerRight = _cmd.steerRight;
        ip.airBrakeLeft  = _cmd.abLeft;
        ip.airBrakeRight = _cmd.abRight;
    }
}
