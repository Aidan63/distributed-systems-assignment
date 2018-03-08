package uk.aidanlee.dsp_assignment.components.craft;

import uk.aidanlee.dsp_assignment.components.PolygonComponent;
import uk.aidanlee.dsp_assignment.race.Race;
import uk.aidanlee.dsp_assignment.structural.ec.Component;
import uk.aidanlee.jDiffer.Collision;

public class LapTracker extends Component {
    private boolean[] checkpointsPassed;
    private PolygonComponent poly;
    private int checkpointCount;

    public LapTracker(String _name) {
        super(_name);
    }

    @Override
    public void onadded() {
        checkpointCount   = Race.circuit.getCheckpoints().size();
        checkpointsPassed = new boolean[checkpointCount];
        for (Boolean b : checkpointsPassed) b = false;

        poly = (PolygonComponent) get("polygon");
    }

    @Override
    public void onremoved() {
        if (has("lap_timer")) remove("lap_timer");
    }

    @Override
    public void update(float _dt) {
        for (int i = 0; i < checkpointCount; i++)  {
            // Do start / finish line stuff.
            if (i == 0) {
                if (Collision.rayWithShape(Race.circuit.getCheckpoints().get(0), poly.getShape(), null) == null) {
                    continue;
                }

                // If we've completed a lap queue an event with the entities ID and the lap time.
                // Then remove the timer and reset the checkpoint status.
                if (allCheckpointsPassed())
                {
                    System.out.println("lap passed");
                    remove("lap_timer");

                    for (int j = 0; j < checkpointCount; j++) {
                        checkpointsPassed[j] = false;
                    }
                }

                // If we are passing over the start point and haven't already add a new lap timer.
                if (!checkpointsPassed[0]) {
                    System.out.println("Adding new timer...");
                }
            }

            // All checkpoint type collision checking
            if (Collision.rayWithShape(Race.circuit.getCheckpoints().get(i), poly.getShape(), null) != null) {
                checkpointsPassed[i] = true;
            }
        }
    }

    private boolean allCheckpointsPassed() {
        for (boolean b : checkpointsPassed) {
            if (!b) return false;
        }

        return true;
    }
}
