package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.Packet;

public abstract class Command {
    public static final byte CLIENT_CONNECTED    = 0;
    public static final byte CLIENT_DISCONNECTED = 1;
    public static final byte CLIENT_SETTINGS     = 2;
    public static final byte CLIENT_INPUT        = 3;
    public static final byte SNAPSHOT            = 4;
    public static final byte CHAT_MESSAGE = 5;
    public static final byte SERVER_STATE = 6;

    // Command Variables.

    public final byte id;
    public final int sentTime;

    public abstract void add(Packet _packet);

    Command(byte _id, int _sentTime) {
        id       = _id;
        sentTime = _sentTime;
    }

    Command(byte _id) {
        id       = _id;
        sentTime = 0;
    }
}
