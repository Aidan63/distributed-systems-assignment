package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.BitPacker;

import java.util.UUID;

public class Command implements Comparable<Command> {
    public static final byte CLIENT_CONNECTED    = 0;
    public static final byte CLIENT_DISCONNECTED = 1;
    public static final byte CLIENT_UPDATED      = 2;
    public static final byte CLIENT_READY        = 3;
    public static final byte CLIENT_UNREADY      = 4;

    public static final byte CHAT_MESSAGE = 5;

    private UUID uuid;

    Command() {
        uuid = UUID.randomUUID();
    }

    public void add(BitPacker _packet) {
        //
    }

    @Override
    public int compareTo(Command _command) {
        return uuid.compareTo(_command.uuid);
    }
}
