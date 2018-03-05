package uk.aidanlee.dsp_assignment.components.craft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import uk.aidanlee.dsp_assignment.structural.ec.Component;

public class InputComponent extends Component {
    public boolean accelerate = false;
    public boolean decelerate = false;
    public boolean steerLeft  = false;
    public boolean steerRight = false;
    public boolean airBrakeLeft  = false;
    public boolean airBrakeRight = false;

    public InputComponent(String _name) {
        super(_name);
    }

    @Override
    public void update(float _dt) {
        switch (entity.getName()) {
            case "local player 0":
                accelerate = Gdx.input.isKeyPressed(Input.Keys.W);
                decelerate = Gdx.input.isKeyPressed(Input.Keys.S);
                steerLeft  = Gdx.input.isKeyPressed(Input.Keys.A);
                steerRight = Gdx.input.isKeyPressed(Input.Keys.D);
                airBrakeLeft  = Gdx.input.isKeyPressed(Input.Keys.Q);
                airBrakeRight = Gdx.input.isKeyPressed(Input.Keys.E);
                break;

            case "local player 1":
                accelerate = Gdx.input.isKeyPressed(Input.Keys.T);
                decelerate = Gdx.input.isKeyPressed(Input.Keys.G);
                steerLeft  = Gdx.input.isKeyPressed(Input.Keys.F);
                steerRight = Gdx.input.isKeyPressed(Input.Keys.H);
                airBrakeLeft  = Gdx.input.isKeyPressed(Input.Keys.Q);
                airBrakeRight = Gdx.input.isKeyPressed(Input.Keys.E);
                break;

            case "local player 2":
                accelerate = Gdx.input.isKeyPressed(Input.Keys.I);
                decelerate = Gdx.input.isKeyPressed(Input.Keys.K);
                steerLeft  = Gdx.input.isKeyPressed(Input.Keys.J);
                steerRight = Gdx.input.isKeyPressed(Input.Keys.L);
                airBrakeLeft  = Gdx.input.isKeyPressed(Input.Keys.Q);
                airBrakeRight = Gdx.input.isKeyPressed(Input.Keys.E);
                break;

            case "local player 3":
                accelerate = Gdx.input.isKeyPressed(Input.Keys.UP   );
                decelerate = Gdx.input.isKeyPressed(Input.Keys.DOWN );
                steerLeft  = Gdx.input.isKeyPressed(Input.Keys.LEFT );
                steerRight = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
                airBrakeLeft  = Gdx.input.isKeyPressed(Input.Keys.Q);
                airBrakeRight = Gdx.input.isKeyPressed(Input.Keys.E);
                break;
        }
    }
}
