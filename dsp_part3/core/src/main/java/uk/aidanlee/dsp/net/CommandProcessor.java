package uk.aidanlee.dsp.net;

import uk.aidanlee.dsp.common.net.BitPacker;
import uk.aidanlee.dsp.common.net.commands.Command;
import uk.aidanlee.dsp.data.Game;

import java.net.StandardSocketOptions;

public class CommandProcessor {

    /**
     * Parse an unknown amount of commands
     * @param _data
     */
    public static void parse(BitPacker _data) {

        for (int i = 0; i < _data.readByte(); i++) {
            switch (_data.readByte()) {
                case Command.CLIENT_CONNECTED:
                    cmdClientConnected(_data);
                    break;

                case Command.CLIENT_DISCONNECTED:
                    cmdClientDisconnected(_data);
                    break;

                case Command.CHAT_MESSAGE:
                    cmdChatMessage(_data);
                    break;
            }
        }
    }

    /**
     *
     * @param _data
     */
    private static void cmdClientConnected(BitPacker _data) {

        System.out.println("NEW CLIENT CONNECTED?!");

        // Read Basic Info
        String name = _data.readString();
        int    id   = _data.readByte();
        int    idx  = _data.readByte();

        // Read ship color
        int sR = _data.readByte();
        int sG = _data.readByte();
        int sB = _data.readByte();

        // Read trail color
        int tR = _data.readByte();
        int tG = _data.readByte();
        int tB = _data.readByte();

        Client c = new Client(id, name);
        System.out.println(name);
        c.setShipIndex (idx);
        c.setShipColor (new float[] { sR, sG, sB });
        c.setTrailColor(new float[] { tR, tG, tB });

        Game.connections.getClients()[id] = c;
        Game.chatlog.addServerMessage(name + " has joined");
    }

    /**
     *
     * @param _data
     */
    private static void cmdClientDisconnected(BitPacker _data) {

        int id = _data.readByte();
        Client[] clients = Game.connections.getClients();

        Game.chatlog.addServerMessage(clients[id].getName() + " has left");
        clients[id] = null;
    }

    /**
     *
     * @param _data
     */
    private static void cmdChatMessage(BitPacker _data) {

        int    id  = _data.readByte();
        String txt = _data.readString();

        Client[] clients = Game.connections.getClients();
        Game.chatlog.addPlayerMessage(clients[id].getName(), txt);
    }
}
