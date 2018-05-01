package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.Packet;

public class CmdClientSettings extends Command {
    public final int clientID;
    public final int index;
    public final float[] shipColor;
    public final float[] trailColor;
    public final boolean ready;

    public CmdClientSettings(int _clientID, int _index, float[] _sCol, float[] _tCol, boolean _ready) {
        super(Command.CLIENT_SETTINGS);

        clientID = _clientID;
        index    = _index;
        shipColor  = _sCol;
        trailColor = _tCol;
        ready = _ready;
    }

    public CmdClientSettings(Packet _packet, int _sentTime) {
        super(Command.CLIENT_SETTINGS, _sentTime);

        clientID  = _packet.getData().readByte();
        index     = _packet.getData().readByte();
        shipColor = new float[] {
                (_packet.getData().readByte() & 0xFF) / 255f,
                (_packet.getData().readByte() & 0xFF) / 255f,
                (_packet.getData().readByte() & 0xFF) / 255f, 1
        };
        trailColor = new float[] {
                (_packet.getData().readByte() & 0xFF) / 255f,
                (_packet.getData().readByte() & 0xFF) / 255f,
                (_packet.getData().readByte() & 0xFF) / 255f, 1
        };
        ready = _packet.getData().readBoolean();
    }

    @Override
    public void add(Packet _packet) {
        _packet.getData().writeByte(Command.CLIENT_SETTINGS);
        _packet.getData().writeByte((byte) clientID);
        _packet.getData().writeByte((byte) index);

        _packet.getData().writeByte((byte) (shipColor[0] * 255));
        _packet.getData().writeByte((byte) (shipColor[1] * 255));
        _packet.getData().writeByte((byte) (shipColor[2] * 255));

        _packet.getData().writeByte((byte) (trailColor[0] * 255));
        _packet.getData().writeByte((byte) (trailColor[1] * 255));
        _packet.getData().writeByte((byte) (trailColor[2] * 255));

        _packet.getData().writeBoolean(ready);
    }
}
