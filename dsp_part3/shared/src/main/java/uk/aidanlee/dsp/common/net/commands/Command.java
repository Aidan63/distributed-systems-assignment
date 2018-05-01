package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.Packet;

/**
 * Commands contain data to send across the network and are serialize into a netchan packet.
 * All commands are prefixed with a byte containing their ID telling the receiver what data is to follow.
 *
 * Data fields in commands should be final so they cannot be reassigned once sent to the netchan for serialization or deserialization.
 * The abstract "add" function needs to be implemented for each child command to serialize its data into the packet.
 */
public abstract class Command {

    // All of the commands.
    public static final byte CLIENT_CONNECTED    = 0;
    public static final byte CLIENT_DISCONNECTED = 1;
    public static final byte CLIENT_SETTINGS     = 2;
    public static final byte CLIENT_INPUT        = 3;
    public static final byte SNAPSHOT            = 4;
    public static final byte CHAT_MESSAGE        = 5;
    public static final byte SERVER_STATE        = 6;
    public static final byte PLAYER_FINISHED     = 7;
    public static final byte RACE_RESULTS        = 8;

    // Command Variables.

    /**
     * ID representing which command this is.
     */
    public final byte id;

    /**
     * The time this command was sent.
     */
    public final int sentTime;

    /**
     * Serialization command.
     * @param _packet The packet to add this commands data to.
     */
    public abstract void add(Packet _packet);

    /**
     * Constructor used for de-serializing data from a packet. The time field is the time it was sent, not received.
     * @param _id       Command ID.
     * @param _sentTime Time this command was sent from the server or client.
     */
    Command(byte _id, int _sentTime) {
        id       = _id;
        sentTime = _sentTime;
    }

    /**
     * Constructor used for creating a command to be serialized.
     * @param _id Command ID.
     */
    Command(byte _id) {
        id       = _id;
        sentTime = 0;
    }
}
