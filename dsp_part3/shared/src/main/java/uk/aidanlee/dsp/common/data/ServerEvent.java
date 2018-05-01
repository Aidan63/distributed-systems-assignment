package uk.aidanlee.dsp.common.data;

/**
 * List of named events which occurs on the server and clients are informed about through reliable commands.
 */
public class ServerEvent {
    /**
     * Indicates to the client that they should return to the lobby.
     * Sent at the end of a race.
     */
    public static final byte EVENT_LOBBY_ENTER = 0;

    /**
     * Indicates that the lobby countdown has begun.
     */
    public static final byte EVENT_LOBBY_COUNTDOWN = 1;

    /**
     * Sent to the clients once the lobby countdown has finished and the server has successfully entered the race state.
     */
    public static final byte EVENT_RACE_ENTER = 2;

    /**
     * Sent to the clients when the race countdown has ended.
     * This event is sent 3 seconds after EVENT_RACE_ENTER.
     */
    public static final byte EVENT_RACE_START = 3;
}
