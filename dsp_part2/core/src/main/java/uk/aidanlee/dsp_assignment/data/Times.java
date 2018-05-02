package uk.aidanlee.dsp_assignment.data;

import uk.aidanlee.dsp_assignment.structural.ec.Entity;

import java.util.*;

/**
 * Responsible for storing all players lap times.
 */
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
     * @param _entities All of the entities in this race.
     * @param _maxLaps  Number of laps for this race.
     */
    public Times(Entity[] _entities, int _maxLaps) {
        times   = new HashMap<>();
        maxLaps = _maxLaps;

        for (Entity e : _entities) {
            times.put(e.getName(), new ArrayList<>());
        }
    }

    /**
     * Returns all of the times for this race.
     * @return Map of entity names to a list of float times.
     */
    public Map<String, List<Float>> getTimes() {
        return times;
    }

    /**
     * Appends a new lap time for a specific player.
     * @param _name The name of the entity to add the time to.
     * @param _time The time float to add.
     */
    public void addTime(String _name, float _time) {
        if (times.get(_name).size() == maxLaps) return;

        times.get(_name).add(_time);
    }

    /**
     * Checks if the provided player has finished all of their laps.
     * @param _name entity name.
     * @return boolean.
     */
    public boolean playerFinished(String _name) {
        return (times.get(_name).size() == maxLaps);
    }

    /**
     * Returns if all players have finished all laps.
     * @return boolean.
     */
    public boolean allPlayersFinished() {
        for (List<Float> time : times.values()) {
            if (time.size() != maxLaps) return false;
        }

        return true;
    }
}
