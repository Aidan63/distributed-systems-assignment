package uk.aidanlee.dsp.common.components.craft;

import uk.aidanlee.dsp.common.components.PolygonComponent;
import uk.aidanlee.dsp.common.data.events.EvLapTime;
import uk.aidanlee.dsp.common.structural.ec.Component;
import uk.aidanlee.jDiffer.Collision;
import uk.aidanlee.jDiffer.shapes.Ray;

public class LapTracker extends Component {
    /**
     * Boolean status for if each checkpoint for the current lap has been passed.
     */
    private boolean[] checkpointPassed;

    /**
     * All of the checkpoint rays.
     */
    private Ray[] checkpoints;

    public LapTracker(String _name, Ray[] _checkpoints) {
        super(_name);

        checkpoints      = _checkpoints;
        checkpointPassed = new boolean[checkpoints.length];
    }

    @Override
    public void update(float _dt) {

        // Get this entities polygon and check for checkpoint collisions
        if (!has("polygon")) return;
        PolygonComponent poly = (PolygonComponent) get("polygon");

        for (int i = 0; i < checkpointPassed.length; i++) {
            // Specific case for start / finish line.
            if (i == 0) {
                if (Collision.rayWithShape(checkpoints[i], poly.getShape(), null) == null) {
                    continue;
                }

                // If we've completed a lap then fire an event with the entity name and time float.
                // then remove the timer and reset the checkpoint status.
                if (allCheckpointsPassed()) {
                    entity.getEvents().post(new EvLapTime(entity.getName(), ((LapTimer) get("lap_timer")).time));
                    remove("lap_timer");

                    for (int j = 0; j < checkpointPassed.length; j++) {
                        checkpointPassed[j] = false;
                    }
                }

                // If we are crossing the finish line and don't have a timer, add a new one.
                if (!checkpointPassed[0]) {
                    add(new LapTimer("lap_timer"));
                }
            }

            // Set the checkpoint to passed after passing it.
            if (Collision.rayWithShape(checkpoints[i], poly.getShape(), null) != null) {
                checkpointPassed[i] = true;
            }
        }
    }

    private boolean allCheckpointsPassed() {
        for (boolean b : checkpointPassed) {
            if (!b) return false;
        }

        return true;
    }
}
