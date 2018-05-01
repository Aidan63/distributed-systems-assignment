package uk.aidanlee.dsp.common.data.events;

/**
 * Instance of this class is created by the lap timer component and fired off into the entities event bus.
 * Other objects can listen for this event from the bus to be notified when players complete laps.
 */
public class EvLapTime {

    /**
     * Name of the entity which has completed a lap.
     */
    public final String name;

    /**
     * Time of that lap.
     */
    public final float time;

    public EvLapTime(String _name, float _time) {
        name = _name;
        time = _time;
    }
}
