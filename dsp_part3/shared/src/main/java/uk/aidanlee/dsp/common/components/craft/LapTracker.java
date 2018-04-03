package uk.aidanlee.dsp.common.components.craft;

import uk.aidanlee.dsp.common.components.PolygonComponent;
import uk.aidanlee.dsp.common.data.Times;
import uk.aidanlee.dsp.common.structural.ec.Component;
import uk.aidanlee.jDiffer.Collision;
import uk.aidanlee.jDiffer.shapes.Ray;

public class LapTracker extends Component {
    /**
     * Boolean status for if each checkpoint for the current lap has been passed.
     */
    private boolean[] checkpointPassed;

    public LapTracker(String _name) {
        super(_name);
    }

    public void check(Ray[] _checkpoints, Times _times) {
        // Create the checkpoint array if we don't have one.
        if (checkpointPassed == null) {
            checkpointPassed = new boolean[_checkpoints.length];
        }

        // Get this entities polygon and check for checkpoint collisions
        if (!has("polygon")) return;
        PolygonComponent poly = (PolygonComponent) get("polygon");

        for (int i = 0; i < checkpointPassed.length; i++) {
            // Specific case for start / finish line.
            if (i == 0) {
                if (Collision.rayWithShape(_checkpoints[i], poly.getShape(), null) == null) {
                    continue;
                }

                // If we've completed a lap then add the current lap time into the times class.
                // then remove the timer and reset the checkpoint status.
                if (allCheckpointsPassed()) {
                    _times.addTime(entity.getName(), ((LapTimer) get("lap_timer")).time);
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
            if (Collision.rayWithShape(_checkpoints[i], poly.getShape(), null) != null) {
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
