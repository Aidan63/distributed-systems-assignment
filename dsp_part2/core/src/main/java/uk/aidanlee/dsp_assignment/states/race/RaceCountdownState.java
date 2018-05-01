package uk.aidanlee.dsp_assignment.states.race;

import com.badlogic.gdx.utils.Timer;
import uk.aidanlee.dsp_assignment.structural.State;

public class RaceCountdownState extends State {

    public RaceCountdownState(String _name) {
        super(_name);
    }

    @Override
    public void onEnter(Object _enterWith) {
        Timer.Task task = new Timer.Task() {
            @Override
            public void run() {
                machine.set("race", null, null);
            }
        };

        Timer.schedule(task, 3);
    }
}
