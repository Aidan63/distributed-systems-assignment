package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.Packet;

public class CmdClientUpdated extends Command {
    /**
     * The ID of the client who sent this command.
     */
    public final int clientID;

    /**
     * The clients ship index.
     */
    public final int index;

    /**
     * 4 element (RGBA) normalized (0 - 1) array for ship color.
     */
    public final float[] shipColor;

    /**
     * 4 element (RGBA) normalized (0 - 1) array for trail color.
     */
    public final float[] trailColor;

    /**
     *
     * @param _id
     * @param _idx
     * @param _sCol
     * @param _tCol
     */
    public CmdClientUpdated(int _id, int _idx, float[] _sCol, float[] _tCol) {
        super(Command.CLIENT_UPDATED);
        clientID   = _id;
        index      = _idx;
        shipColor  = _sCol;
        trailColor = _tCol;
    }

    /**
     *
     * @param _packet
     */
    public CmdClientUpdated(Packet _packet) {
        super(Command.CLIENT_UPDATED);
        clientID  = _packet.getData().readByte();
        index     = _packet.getData().readByte();
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
    }

    @Override
    public void add(Packet _packet) {
        _packet.getData().writeByte(Command.CLIENT_UPDATED);
        _packet.getData().writeByte((byte) clientID);
        _packet.getData().writeByte((byte) index);

        _packet.getData().writeFloat(shipColor[0]);
        _packet.getData().writeFloat(shipColor[1]);
        _packet.getData().writeFloat(shipColor[2]);

        _packet.getData().writeFloat(trailColor[0]);
        _packet.getData().writeFloat(trailColor[1]);
        _packet.getData().writeFloat(trailColor[2]);

    }
}
