package uk.aidanlee.dsp.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import uk.aidanlee.dsp.common.components.InputComponent;
import uk.aidanlee.dsp.common.structural.ec.Component;

public class LocalInputComponent extends Component {
    public LocalInputComponent(String _name) {
        super(_name);
    }

    @Override
    public void update(float _dt) {
        if (!has("input")) return;

        InputComponent ip = (InputComponent) get("input");
        ip.accelerate = Gdx.input.isKeyPressed(Input.Keys.W);
        ip.decelerate = Gdx.input.isKeyPressed(Input.Keys.S);
        ip.steerLeft  = Gdx.input.isKeyPressed(Input.Keys.A);
        ip.steerRight = Gdx.input.isKeyPressed(Input.Keys.D);
        ip.airBrakeLeft  = Gdx.input.isKeyPressed(Input.Keys.Q);
        ip.airBrakeRight = Gdx.input.isKeyPressed(Input.Keys.E);
    }

    @Override
    public void onremoved() {
        if (!has("input")) return;

        InputComponent ip = (InputComponent) get("input");
        ip.accelerate = false;
        ip.decelerate = false;
        ip.steerLeft  = false;
        ip.steerRight = false;
        ip.airBrakeLeft  = false;
        ip.airBrakeRight = false;
    }
}
