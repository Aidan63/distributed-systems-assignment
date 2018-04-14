package uk.aidanlee.dsp.server.data.events;

import java.util.List;
import java.util.Map;

public class EvRaceResults {
    public final Map<Integer, List<Float>> times;

    public EvRaceResults(Map<Integer, List<Float>> _times) {
        times = _times;
    }
}
