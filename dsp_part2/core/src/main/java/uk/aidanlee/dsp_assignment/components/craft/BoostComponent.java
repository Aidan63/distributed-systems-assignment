package uk.aidanlee.dsp_assignment.components.craft;

import uk.aidanlee.dsp_assignment.structural.ec.Component;

public class BoostComponent extends Component {
    private int duration;
    private int increase;
    private int timer;

    public BoostComponent(String _name, int _duration, int _increase) {
        super(_name);
        duration = _duration;
        increase = _increase;
        timer    = 0;
    }

    @Override
    public void onadded() {
        StatsComponent stats = (StatsComponent) get("stats");
        stats.maxSpeed += increase;
        stats.engineSpeed += increase / 2;
    }

    @Override
    public void onremoved() {
        StatsComponent stats = (StatsComponent) get("stats");
        stats.maxSpeed -= increase;
    }

    @Override
    public void update(float _dt) {
        timer++;

        if (timer == duration) {
            remove(name);
        }
    }
}
