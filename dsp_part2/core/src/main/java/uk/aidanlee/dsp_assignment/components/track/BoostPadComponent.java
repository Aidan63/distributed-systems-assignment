package uk.aidanlee.dsp_assignment.components.track;

import com.badlogic.gdx.graphics.Color;
import uk.aidanlee.dsp_assignment.race.Race;
import uk.aidanlee.dsp_assignment.structural.ec.Component;

public class BoostPadComponent extends Component {
    public String tileID;
    public boolean active;

    private int timer;
    private int timerLimit;

    public BoostPadComponent(String _name, String _tileID) {
        super(_name);

        tileID = _tileID;
        active = true;

        timer = 0;
        timerLimit = 60;

        activate();
    }

    @Override
    public void update(float _dt) {
        if (!active) {
            timer++;
            if (timer == timerLimit) {
                active = true;
                timer  = 0;
                activate();
            }
        }
    }

    public void activate() {
        Color newColor = new Color(0.20f, 0.60f, 0.86f, 1.00f);
        Race.circuit.getMesh().colorQuad(Race.circuit.getQuadIDs().get(tileID), newColor);
        Race.circuit.getMesh().rebuild();
        active = true;
    }

    public void deactivate() {
        Color newColor = new Color(1f, 1f, 1f, 1f);
        Race.circuit.getMesh().colorQuad(Race.circuit.getQuadIDs().get(tileID), newColor);
        Race.circuit.getMesh().rebuild();
        active = false;
    }
}
