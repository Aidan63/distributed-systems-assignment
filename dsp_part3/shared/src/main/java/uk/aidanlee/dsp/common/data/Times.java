package uk.aidanlee.dsp.common.data;

import uk.aidanlee.dsp.common.structural.ec.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Times {
    /**
     * Map linking the clients names to their list of times.
     */
    private Map<String, List<Float>> times;

    /**
     * The number of laps in this race.
     */
    private int maxLaps;

    /**
     *
     * @param _entities //
     * @param _maxLaps  //
     */
    public Times(Entity[] _entities, int _maxLaps) {
        times   = new HashMap<>();
        maxLaps = _maxLaps;

        for (Entity e : _entities) {
            if (e == null) continue;
            times.put(e.getName(), new ArrayList<>());
        }
    }

    public void playerDisconnected(String _name) {
        if (!times.containsKey(_name)) return;

        times.remove(_name);
    }

    public void addTime(String _name, float _time) {
        if (times.get(_name).size() == maxLaps) return;

        times.get(_name).add(_time);
    }

    public boolean playerFinished(String _name) {
        return (times.get(_name).size() == maxLaps);
    }

    public boolean allPlayersFinished() {
        for (List<Float> time : times.values()) {
            if (time.size() != maxLaps) return false;
        }

        return true;
    }
}
