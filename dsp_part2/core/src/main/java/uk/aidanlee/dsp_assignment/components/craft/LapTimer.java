package uk.aidanlee.dsp_assignment.components.craft;

import uk.aidanlee.dsp_assignment.structural.ec.Component;

public class LapTimer extends Component {
    public float time;

    public LapTimer(String _name) {
        super(_name);
    }

    @Override
    public void onadded() {
        time = 0;
    }

    @Override
    public void update(float _dt) {
        time += (1f / 60f);
    }
}
