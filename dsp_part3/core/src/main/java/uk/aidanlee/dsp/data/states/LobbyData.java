package uk.aidanlee.dsp.data.states;

import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.data.ChatLog;

public class LobbyData {
    public final ChatLog chat;
    public final Player[] players;
    public final int ourID;

    public LobbyData(ChatLog _chat, Player[] _players, int _ourID) {
        chat    = _chat;
        players = _players;
        ourID   = _ourID;
    }
}
