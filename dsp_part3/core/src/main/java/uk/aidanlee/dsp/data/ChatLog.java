package uk.aidanlee.dsp.data;

import java.util.LinkedList;
import java.util.List;

public class ChatLog {
    /**
     * Backlog of all messages.
     */
    private List<String> log;

    /**
     * Creates a new empty chat.
     */
    public ChatLog() {
        log = new LinkedList<>();
    }

    public List<String> getLog() {
        return log;
    }

    public void addPlayerMessage(String _player, String _message) {
        log.add(_player + " : " + _message);
    }

    public void addServerMessage(String _message) {
        log.add(_message);
    }

    public void clear() {
        log.clear();
    }
}
