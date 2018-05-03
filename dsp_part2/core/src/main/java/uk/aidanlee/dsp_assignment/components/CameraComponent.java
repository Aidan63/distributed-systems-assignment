package uk.aidanlee.dsp_assignment.components;

import com.badlogic.gdx.graphics.Camera;
import uk.aidanlee.dsp_assignment.structural.ec.Component;
import uk.aidanlee.dsp_assignment.utils.MathsUtil;

/**
 * Holds the camera for this specific entity.
 */
public class CameraComponent extends Component {
    public Camera camera;

    public CameraComponent(String _name, Camera _camera) {
        super(_name);
        camera = _camera;
    }

    @Override
    public void update(float _dt) {
        camera.position.x = (float)(entity.pos.x + MathsUtil.lengthdirX(200, entity.rotation));
        camera.position.y = (float)(entity.pos.y + MathsUtil.lengthdirY(200, entity.rotation));
    }
}
