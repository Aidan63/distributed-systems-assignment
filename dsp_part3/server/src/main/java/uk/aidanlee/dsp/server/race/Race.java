package uk.aidanlee.dsp.server.race;

import uk.aidanlee.dsp.common.data.circuit.Circuit;
import uk.aidanlee.dsp.server.data.Craft;

public class Race {
    public final Circuit circuit;
    public final Craft craft;

    public Race() {
        circuit = new Circuit();
        craft   = new Craft();
    }
}
