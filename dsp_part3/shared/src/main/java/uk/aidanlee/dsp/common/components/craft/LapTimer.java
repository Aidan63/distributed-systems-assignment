package uk.aidanlee.dsp.common.components.craft;

import uk.aidanlee.dsp.common.structural.ec.Component;

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
        // TODO : Remove this hard coded assumption of a DT of 0.16666 (60fps)
        time += (1 * 0.167);
    }
}
