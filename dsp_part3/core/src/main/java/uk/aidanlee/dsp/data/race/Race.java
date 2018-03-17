package uk.aidanlee.dsp.data.race;

import uk.aidanlee.dsp.common.data.circuit.Circuit;

public class Race {
    public final Circuit circuit;
    public final Craft craft;
    public final View view;

    public Race() {
        circuit = new Circuit("/media/aidan/BAD1-1589/dsp/dsp_part2/assets/tracks/track.p2");
        craft   = new Craft();
        view    = new View();
    }
}
