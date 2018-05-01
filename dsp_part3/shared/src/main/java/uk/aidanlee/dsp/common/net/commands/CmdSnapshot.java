package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.net.Snapshot;

/**
 * Snapshot command. Reads and write a snapshot to and from a packet.
 */
public class CmdSnapshot extends Command {
    /**
     * The snapshot data.
     */
    public final Snapshot snapshot;

    /**
     * Creates a packet with a snapshot to write to the packet.
     * @param _snapshot Snapshot to write.
     */
    public CmdSnapshot(Snapshot _snapshot) {
        super(Command.SNAPSHOT);
        snapshot = _snapshot;
    }

    /**
     * Reads a snapshot from the provided packet.
     * @param _packet   Packet to read the snapshot from.
     * @param _sentTime Time the command was sent.
     */
    public CmdSnapshot(Packet _packet, int _sentTime) {
        super(Command.SNAPSHOT, _sentTime);

        // Reconstruct the snapshot by reading and adding all player data.
        // Normalized color floats (0.0 - 1.0) are converted to bytes so they can be sent as 8 bits per RGB component instead of 32bits.
        snapshot = new Snapshot();

        // Read all player data from the packet and re-create the snapshot.
        int numPlayers = _packet.getData().readByte();
        for (int i = 0; i < numPlayers; i++) {
            int clientID = _packet.getData().readByte();
            int index    = _packet.getData().readByte();
            float[] shipColor = new float[] {
                    (_packet.getData().readByte() & 0xFF) / 255f,
                    (_packet.getData().readByte() & 0xFF) / 255f,
                    (_packet.getData().readByte() & 0xFF) / 255f
            };
            float[] trailColor = new float[] {
                    (_packet.getData().readByte() & 0xFF) / 255f,
                    (_packet.getData().readByte() & 0xFF) / 255f,
                    (_packet.getData().readByte() & 0xFF) / 255f
            };
            boolean ready = _packet.getData().readBoolean();
            float x = _packet.getData().readFloat();
            float y = _packet.getData().readFloat();
            float r = _packet.getData().readFloat();

            Player player = new Player("");
            player.setShipIndex(index);
            player.setShipColor(shipColor);
            player.setTrailColor(trailColor);
            player.setReady(ready);
            player.setX(x);
            player.setY(y);
            player.setRotation(r);

            snapshot.addPlayer(clientID, player);
        }
    }

    @Override
    public void add(Packet _packet) {
        _packet.getData().writeByte(Command.SNAPSHOT);
        _packet.getData().writeByte((byte) snapshot.getPlayerCount());

        for (int i = 0; i < snapshot.getPlayerCount(); i++) {
            Player player = snapshot.getPlayer(i);
            int    id     = snapshot.getID(i);

            _packet.getData().writeByte((byte) id);
            _packet.getData().writeByte((byte) player.getShipIndex());

            _packet.getData().writeByte((byte) (player.getShipColor()[0] * 255));
            _packet.getData().writeByte((byte) (player.getShipColor()[1] * 255));
            _packet.getData().writeByte((byte) (player.getShipColor()[2] * 255));

            _packet.getData().writeByte((byte) (player.getTrailColor()[0] * 255));
            _packet.getData().writeByte((byte) (player.getTrailColor()[1] * 255));
            _packet.getData().writeByte((byte) (player.getTrailColor()[2] * 255));

            _packet.getData().writeBoolean(player.isReady());

            _packet.getData().writeFloat(player.getX());
            _packet.getData().writeFloat(player.getY());
            _packet.getData().writeFloat(player.getRotation());

        }
    }
}
