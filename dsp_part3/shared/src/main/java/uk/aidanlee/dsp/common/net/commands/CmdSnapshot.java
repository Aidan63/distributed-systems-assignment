package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.net.Snapshot;

public class CmdSnapshot extends Command {
    public final Snapshot snapshot;

    public CmdSnapshot(Snapshot _snapshot) {
        super(Command.SNAPSHOT);
        snapshot = _snapshot;
    }

    public CmdSnapshot(Packet _packet) {
        super(Command.SNAPSHOT);

        snapshot = new Snapshot();
        int numEnt = _packet.getData().readByte();
        for (int i = 0; i < numEnt; i++) {
            snapshot.addPlayer(
                    _packet.getData().readByte(),
                    _packet.getData().readFloat(),
                    _packet.getData().readFloat(),
                    _packet.getData().readFloat()
            );
        }
    }

    @Override
    public void add(Packet _packet) {
        _packet.getData().writeByte(Command.SNAPSHOT);
        _packet.getData().writeByte((byte) snapshot.getPlayers().size());

        for (int i = 0; i < snapshot.getPlayers().size(); i++) {
            _packet.getData().writeByte((byte) snapshot.getPlayers().get(i).id);
            _packet.getData().writeFloat(snapshot.getPlayers().get(i).x);
            _packet.getData().writeFloat(snapshot.getPlayers().get(i).y);
            _packet.getData().writeFloat(snapshot.getPlayers().get(i).angle);
        }
    }
}
