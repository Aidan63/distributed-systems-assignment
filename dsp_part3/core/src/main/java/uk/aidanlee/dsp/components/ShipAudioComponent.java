package uk.aidanlee.dsp.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import uk.aidanlee.dsp.common.components.InputComponent;
import uk.aidanlee.dsp.common.components.StatsComponent;
import uk.aidanlee.dsp.common.structural.ec.Component;

/**
 * Component which manages audio for the local player ship.
 * Plays an engine sound where the pitch varies based on how close to our max speed we are.
 * Also plays a quieter sound when steering.
 */
public class ShipAudioComponent extends Component {

    /**
     * The sound instance for the engine sound.
     */
    private Sound engine;

    /**
     * The sound instance for the airbrakes sound.
     */
    private Sound airbrakes;

    /**
     * The ID of the engine sound being played.
     */
    private long engineID;

    /**
     * The ID of the airbrakes sound being played.
     */
    private long airbrakesID;

    public ShipAudioComponent(String _name) {
        super(_name);
    }

    @Override
    public void onadded() {
        engine    = Gdx.audio.newSound(Gdx.files.internal("audio/ENGINE.WAV"));
        airbrakes = Gdx.audio.newSound(Gdx.files.internal("audio/AIRBRAKE.WAV"));

        engineID    = engine.loop(0.5f);
        airbrakesID = airbrakes.loop(0.0f);
    }

    @Override
    public void update(float _dt) {
        if (has("stats")) {
            // Set the engine pitch according to how fast we are going.
            StatsComponent stats = (StatsComponent) get("stats");
            engine.setPitch(engineID, 1 + ((stats.engineSpeed / stats.maxSpeed) / 4 ));

            // Play another sound when steering
            if (stats.engineSpeed > 2 && has("input")) {
                InputComponent input = (InputComponent) get("input");
                if (input.steerLeft || input.steerRight || input.airBrakeLeft || input.airBrakeRight) {
                    engine.setVolume(airbrakesID, 0.25f);
                } else {
                    engine.setVolume(airbrakesID, 0.0f);
                }
            }
        }
    }

    @Override
    public void onremoved() {
        engine.stop();
        airbrakes.stop();
    }
}
