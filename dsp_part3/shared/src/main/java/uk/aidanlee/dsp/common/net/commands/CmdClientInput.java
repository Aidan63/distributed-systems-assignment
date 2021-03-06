package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.components.InputComponent;
import uk.aidanlee.dsp.common.net.Packet;

/**
 * This command is only ever sent from the client to the server. It contains the state a clients game buttons from a specific time.
 * Server uses these keys inputs to properly simulate a client in the game state.
 */
public class CmdClientInput extends Command {
    public final int clientID;
    public final boolean accel;
    public final boolean decel;
    public final boolean steerLeft;
    public final boolean steerRight;
    public final boolean abLeft;
    public final boolean abRight;

    public CmdClientInput(int _clientID, InputComponent _input) {
        super(Command.CLIENT_INPUT);
        clientID = _clientID;
        accel      = _input.accelerate;
        decel      = _input.decelerate;
        steerLeft  = _input.steerLeft;
        steerRight = _input.steerRight;
        abLeft     = _input.airBrakeLeft;
        abRight    = _input.airBrakeRight;
    }

    public CmdClientInput(Packet _packet, int _sentTime) {
        super(Command.CLIENT_INPUT, _sentTime);
        clientID = _packet.getData().readByte();
        accel      = _packet.getData().readBoolean();
        decel      = _packet.getData().readBoolean();
        steerLeft  = _packet.getData().readBoolean();
        steerRight = _packet.getData().readBoolean();
        abLeft     = _packet.getData().readBoolean();
        abRight    = _packet.getData().readBoolean();
    }

    @Override
    public void add(Packet _packet) {
        _packet.getData().writeByte(Command.CLIENT_INPUT);
        _packet.getData().writeByte((byte) clientID);
        _packet.getData().writeBoolean(accel);
        _packet.getData().writeBoolean(decel);
        _packet.getData().writeBoolean(steerLeft);
        _packet.getData().writeBoolean(steerRight);
        _packet.getData().writeBoolean(abLeft);
        _packet.getData().writeBoolean(abRight);
    }
}
