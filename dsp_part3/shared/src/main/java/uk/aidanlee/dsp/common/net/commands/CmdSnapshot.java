package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.net.PlayerDiff;
import uk.aidanlee.dsp.common.net.Snapshot;

import java.util.LinkedList;
import java.util.List;

public class CmdSnapshot extends Command {
    private Snapshot master;
    private Snapshot lastAck;

    private List<PlayerDiff> diffedPlayers;

    public List<PlayerDiff> getDiffedPlayers() {
        return diffedPlayers;
    }

    public CmdSnapshot(Snapshot _master, Snapshot _lastAck) {
        super(Command.SNAPSHOT);
        master  = _master;
        lastAck = _lastAck;
    }

    public CmdSnapshot(Packet _packet, int _sentTime) {
        super(Command.SNAPSHOT, _sentTime);

        diffedPlayers = new LinkedList<>();

        int numPlayers = _packet.getData().readByte();
        for (int i = 0; i < numPlayers; i++) {
            PlayerDiff diff = new PlayerDiff();
            diff.id = _packet.getData().readByte();

            diff.diffShipIndex = _packet.getData().readBoolean();
            if (diff.diffShipIndex) {
                diff.shipIndex = _packet.getData().readByte();
            }

            diff.diffShipColR = _packet.getData().readBoolean();
            if (diff.diffShipColR) {
                diff.shipColR = (_packet.getData().readByte() & 0xFF) / 255f;
            }
            diff.diffShipColG = _packet.getData().readBoolean();
            if (diff.diffShipColG) {
                diff.shipColG = (_packet.getData().readByte() & 0xFF) / 255f;
            }
            diff.diffShipColB = _packet.getData().readBoolean();
            if (diff.diffShipColB) {
                diff.shipColB = (_packet.getData().readByte() & 0xFF) / 255f;
            }

            diff.diffTrailColR = _packet.getData().readBoolean();
            if (diff.diffTrailColR) {
                diff.trailColR = (_packet.getData().readByte() & 0xFF) / 255f;
            }
            diff.diffTrailColG = _packet.getData().readBoolean();
            if (diff.diffTrailColG) {
                diff.trailColG = (_packet.getData().readByte() & 0xFF) / 255f;
            }
            diff.diffTrailColB = _packet.getData().readBoolean();
            if (diff.diffTrailColB) {
                diff.trailColB = (_packet.getData().readByte() & 0xFF) / 255f;
            }

            diff.ready = _packet.getData().readBoolean();

            diff.diffX = _packet.getData().readBoolean();
            if (diff.diffX) {
                diff.x = _packet.getData().readFloat();
            }
            diff.diffY = _packet.getData().readBoolean();
            if (diff.diffY) {
                diff.y = _packet.getData().readFloat();
            }
            diff.diffRotation = _packet.getData().readBoolean();
            if (diff.diffRotation) {
                diff.rotation = _packet.getData().readFloat();
            }

            diffedPlayers.add(diff);
        }
    }

    @Override
    public void add(Packet _packet) {
        _packet.getData().writeByte(Command.SNAPSHOT);
        _packet.getData().writeByte((byte) master.getPlayerCount());

        if (lastAck == null) {
            // Full state update.
            for (int i = 0; i < master.getPlayerCount(); i++) {
                Player player = master.getPlayer(i);
                int    id     = master.getID(i);

                _packet.getData().writeByte((byte) id);
                writeFullPlayer(_packet, player);
            }
        }
        else {
            // Delta state update
            for (int i = 0; i < master.getPlayerCount(); i++) {
                int id = master.getID(i);
                Player mPlayer = master.getPlayerByID(i);
                Player lPlayer = lastAck.getPlayerByID(i);

                _packet.getData().writeByte((byte) id);

                if (lPlayer == null) {
                    // Player was not in the last snapshot.
                    // So full state update needs to be sent.
                    writeFullPlayer(_packet, mPlayer);
                } else {
                    // Partial state update.
                    if (_packet.getData().writeBoolean(mPlayer.getShipIndex() != lPlayer.getShipIndex())) {
                        _packet.getData().writeByte((byte) mPlayer.getShipIndex());
                    }

                    if (_packet.getData().writeBoolean(mPlayer.getShipColor()[0] != lPlayer.getShipColor()[0])) {
                        _packet.getData().writeByte((byte) (mPlayer.getShipColor()[0] * 255));
                    }
                    if (_packet.getData().writeBoolean(mPlayer.getShipColor()[1] != lPlayer.getShipColor()[1])) {
                        _packet.getData().writeByte((byte) (mPlayer.getShipColor()[1] * 255));
                    }
                    if (_packet.getData().writeBoolean(mPlayer.getShipColor()[2] != lPlayer.getShipColor()[2])) {
                        _packet.getData().writeByte((byte) (mPlayer.getShipColor()[2] * 255));
                    }

                    if (_packet.getData().writeBoolean(mPlayer.getTrailColor()[0] != lPlayer.getTrailColor()[0])) {
                        _packet.getData().writeByte((byte) (mPlayer.getTrailColor()[0] * 255));
                    }
                    if (_packet.getData().writeBoolean(mPlayer.getTrailColor()[1] != lPlayer.getTrailColor()[1])) {
                        _packet.getData().writeByte((byte) (mPlayer.getTrailColor()[1] * 255));
                    }
                    if (_packet.getData().writeBoolean(mPlayer.getTrailColor()[2] != lPlayer.getTrailColor()[2])) {
                        _packet.getData().writeByte((byte) (mPlayer.getTrailColor()[2] * 255));
                    }

                    _packet.getData().writeBoolean(mPlayer.isReady());

                    if (_packet.getData().writeBoolean(mPlayer.getX() != lPlayer.getX())) {
                        _packet.getData().writeFloat(mPlayer.getX());
                    }
                    if (_packet.getData().writeBoolean(mPlayer.getY() != lPlayer.getY())) {
                        _packet.getData().writeFloat(mPlayer.getY());
                    }
                    if (_packet.getData().writeBoolean(mPlayer.getRotation() != lPlayer.getRotation())) {
                        _packet.getData().writeFloat(mPlayer.getRotation());
                    }
                }
            }
        }
    }

    /**
     * Writes a full player state update into the packet.
     * @param _packet The packet to write into.
     * @param _player The player state to write.
     */
    private void writeFullPlayer(Packet _packet, Player _player) {
        _packet.getData().writeBoolean(true);
        _packet.getData().writeByte((byte) _player.getShipIndex());

        _packet.getData().writeBoolean(true);
        _packet.getData().writeByte((byte) (_player.getShipColor()[0] * 255));
        _packet.getData().writeBoolean(true);
        _packet.getData().writeByte((byte) (_player.getShipColor()[1] * 255));
        _packet.getData().writeBoolean(true);
        _packet.getData().writeByte((byte) (_player.getShipColor()[2] * 255));

        _packet.getData().writeBoolean(true);
        _packet.getData().writeByte((byte) (_player.getTrailColor()[0] * 255));
        _packet.getData().writeBoolean(true);
        _packet.getData().writeByte((byte) (_player.getTrailColor()[1] * 255));
        _packet.getData().writeBoolean(true);
        _packet.getData().writeByte((byte) (_player.getTrailColor()[2] * 255));

        _packet.getData().writeBoolean(_player.isReady());

        _packet.getData().writeBoolean(true);
        _packet.getData().writeFloat(_player.getX());
        _packet.getData().writeBoolean(true);
        _packet.getData().writeFloat(_player.getY());
        _packet.getData().writeBoolean(true);
        _packet.getData().writeFloat(_player.getRotation());
    }
}
