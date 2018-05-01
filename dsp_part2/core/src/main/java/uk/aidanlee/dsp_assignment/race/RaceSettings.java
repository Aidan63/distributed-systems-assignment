package uk.aidanlee.dsp_assignment.race;

import uk.aidanlee.dsp_assignment.data.SplitType;

public class RaceSettings {
    private final int localPlayers;
    private final SplitType split;
    private final PlayerSetting[] players;

    public RaceSettings(int _humans, SplitType _split, PlayerSetting[] _players) {
        localPlayers = _humans;
        split        = _split;
        players      = _players;
    }

    public int getLocalPlayers() {
        return localPlayers;
    }

    public SplitType getSplit() {
        return split;
    }

    public PlayerSetting[] getPlayers() {
        return players;
    }
}
