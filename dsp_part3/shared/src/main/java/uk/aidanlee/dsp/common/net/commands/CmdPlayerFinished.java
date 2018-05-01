package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.Packet;

/**
 * Reads and writes a clientID for a netchan for a player which has finished the race.
 */
public class CmdPlayerFinished extends Command {

    /**
     * ClientID of the player who has finished the race.
     */
    public final int clientID;

    public CmdPlayerFinished(int _clientID) {
        super(Command.PLAYER_FINISHED);
        clientID = _clientID;
    }

    public CmdPlayerFinished(Packet _packet, int _sentTime) {
        super(Command.PLAYER_FINISHED, _sentTime);
        clientID = _packet.getData().readByte();
    }

    @Override
    public void add(Packet _packet) {
        _packet.getData().writeByte(Command.PLAYER_FINISHED);
        _packet.getData().writeByte((byte) clientID);
    }
}
