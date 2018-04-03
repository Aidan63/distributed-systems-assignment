package uk.aidanlee.dsp.common.components.craft;

import uk.aidanlee.dsp.common.components.PolygonComponent;
import uk.aidanlee.dsp.common.structural.ec.Component;
import uk.aidanlee.jDiffer.Collision;
import uk.aidanlee.jDiffer.shapes.Ray;

public class LapTracker extends Component {
    /**
     *
     */
    private boolean[] checkpointPassed;

    public LapTracker(String _name) {
        super(_name);
    }

    public void check(Ray[] _checkpoints) {
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

                if (allCheckpointsPassed()) {
                    System.out.println("Lap passed");
                    for (int j = 0; j < checkpointPassed.length; j++) {
                        checkpointPassed[j] = false;
                    }
                }

                if (!checkpointPassed[0]) {
                    System.out.println("Adding new timer...");
                }
            }

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
