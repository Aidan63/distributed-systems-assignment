package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.net.Snapshot;

public class CmdSnapshot extends Command {
    public final Snapshot master;

    public CmdSnapshot(Snapshot _master) {
        super(Command.SNAPSHOT);
        master = _master;
    }

    public CmdSnapshot(Packet _packet, int _sentTime) {
        super(Command.SNAPSHOT, _sentTime);

        master = new Snapshot();

        int numClients = _packet.getData().readByte();
        for (int i = 0; i < numClients; i++) {
            int clientID  = _packet.getData().readByte();
            Player player = new Player("dummy");
            player.setShipIndex(_packet.getData().readByte());

            player.setShipColor(new float[] {
                    (_packet.getData().readByte() & 0xFF) / 255f,
                    (_packet.getData().readByte() & 0xFF) / 255f,
                    (_packet.getData().readByte() & 0xFF) / 255f, 1
            });
            player.setTrailColor(new float[] {
                    (_packet.getData().readByte() & 0xFF) / 255f,
                    (_packet.getData().readByte() & 0xFF) / 255f,
                    (_packet.getData().readByte() & 0xFF) / 255f, 1
            });
            player.setReady(_packet.getData().readBoolean());
            player.setX(_packet.getData().readFloat());
            player.setY(_packet.getData().readFloat());
            player.setRotation(_packet.getData().readFloat());

            master.addPlayer(clientID, player);
        }
    }

    @Override
    public void add(Packet _packet) {
        _packet.getData().writeByte(Command.SNAPSHOT);
        _packet.getData().writeByte((byte) master.getPlayerCount());

        for (int i = 0; i < master.getPlayerCount(); i++) {
            Player player = master.getPlayer(i);
            int    id     = master.getID(i);

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
