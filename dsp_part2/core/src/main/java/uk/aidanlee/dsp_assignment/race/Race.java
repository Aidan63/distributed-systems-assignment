package uk.aidanlee.dsp_assignment.race;

import uk.aidanlee.dsp_assignment.data.Craft;
import uk.aidanlee.dsp_assignment.data.Times;
import uk.aidanlee.dsp_assignment.data.Views;
import uk.aidanlee.dsp_assignment.data.circuit.Circuit;

public class Race {
    public static RaceSettings settings;
    public static Circuit circuit;
    public static Views views;
    public static Craft craft;
    public static Times times;
    public static Resources resources;

    public static void init(RaceSettings _settings) {
        settings = _settings;

        resources = new Resources();
        circuit   = new Circuit();
        views     = new Views();
        craft     = new Craft();
        times     = new Times();
    }

    public static void dispose() {
        circuit.dispose();
        resources.dispose();
    }
}
