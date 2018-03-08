package uk.aidanlee.dsp.net;

import uk.aidanlee.dsp.common.net.commands.*;
import uk.aidanlee.dsp.data.Game;

public class CommandProcessor {

    /**
     * Parse an unknown amount of commands
     * @param _cmds
     */
    public static void parse(Command[] _cmds) {

        for (Command cmd : _cmds) {
            switch (cmd.id) {
                case Command.CLIENT_CONNECTED:
                    cmdClientConnected((CmdClientConnected) cmd);
                    break;

                case Command.CLIENT_DISCONNECTED:
                    cmdClientDisconnected((CmdClientDisconnected) cmd);
                    break;

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
            }
        }
    }

    /**
     *
     * @param _cmd
     */
    private static void cmdClientConnected(CmdClientConnected _cmd) {
        Client c = new Client(_cmd.client.getId(), _cmd.client.getName());
        c.setShipIndex (_cmd.client.getShipIndex());
        c.setShipColor (_cmd.client.getShipColor());
        c.setTrailColor(_cmd.client.getTrailColor());

        Game.connections.getClients()[_cmd.client.getId()] = c;
        Game.chatlog.addServerMessage(_cmd.client.getName() + " has joined");
    }

    /**
     *
     * @param _cmd
     */
    private static void cmdClientDisconnected(CmdClientDisconnected _cmd) {

        Client[] clients = Game.connections.getClients();
        if (clients[_cmd.clientID] == null) return;

        Game.chatlog.addServerMessage(clients[_cmd.clientID].getName() + " has left");
        clients[_cmd.clientID] = null;
    }

    /**
     *
     * @param _cmd
     */
    private static void cmdChatMessage(CmdChatMessage _cmd) {
        Client[] clients = Game.connections.getClients();
        Game.chatlog.addPlayerMessage(clients[_cmd.clientID].getName(), _cmd.message);
    }

    /**
     *
     * @param _cmd
     */
    private static void cmdClientUpdate(CmdClientUpdated _cmd) {
        Client c = Game.connections.getClients()[_cmd.clientID];
        if (c == null) return;

        c.setShipIndex (_cmd.index);
        c.setShipColor (_cmd.shipColor);
        c.setTrailColor(_cmd.trailColor);
    }

    private static void cmdClientReady(CmdClientReady _cmd) {
        Client c = Game.connections.getClients()[_cmd.clientID];
        c.setReady(true);
    }

    private static void cmdClientUnready(CmdClientUnready _cmd) {
        Client c = Game.connections.getClients()[_cmd.clientID];
        c.setReady(false);
    }
}
