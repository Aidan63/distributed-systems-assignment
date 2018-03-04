package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.BitPacker;

public class Command {
    public static final byte CLIENT_CONNECTED    = 0;
    public static final byte CLIENT_DISCONNECTED = 1;
    public static final byte CLIENT_UPDATED      = 2;
    public static final byte CLIENT_READY        = 3;
    public static final byte CLIENT_UNREADY      = 4;

    public static final byte CHAT_MESSAGE = 5;

    public void add(BitPacker _packet) {
        //
    }
}
