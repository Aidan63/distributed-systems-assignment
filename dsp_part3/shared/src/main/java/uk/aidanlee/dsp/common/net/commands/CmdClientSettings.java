package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.utils.ColorUtil;

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

        clientID = _packet.getData().readByte();
        index    = _packet.getData().readByte();
        shipColor = new float[] {
                _packet.getData().readFloat(),
                _packet.getData().readFloat(),
                _packet.getData().readFloat(), 1
        };
        trailColor = new float[] {
                _packet.getData().readFloat(),
                _packet.getData().readFloat(),
                _packet.getData().readFloat(), 1
        };
        ready = _packet.getData().readBoolean();
    }

    @Override
    public void add(Packet _packet) {
        _packet.getData().writeByte(Command.CLIENT_SETTINGS);
        _packet.getData().writeByte((byte) clientID);
        _packet.getData().writeByte((byte) index);

        _packet.getData().writeFloat(shipColor[0]);
        _packet.getData().writeFloat(shipColor[1]);
        _packet.getData().writeFloat(shipColor[2]);

        _packet.getData().writeFloat(trailColor[0]);
        _packet.getData().writeFloat(trailColor[1]);
        _packet.getData().writeFloat(trailColor[2]);

        _packet.getData().writeBoolean(ready);
    }
}
