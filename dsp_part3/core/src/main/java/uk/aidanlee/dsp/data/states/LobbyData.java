package uk.aidanlee.dsp.data.states;

import uk.aidanlee.dsp.common.net.NetChan;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.data.ChatLog;
import uk.aidanlee.dsp.net.Connections;

public class LobbyData {
    public final NetChan chan;
    public final ChatLog chat;
    public final Player[] players;
    public final int ourID;

    public LobbyData(NetChan _chan, ChatLog _chat, Player[] _players, int _ourID) {
        chan    = _chan;
        chat    = _chat;
        players = _players;
        ourID   = _ourID;
    }
}
