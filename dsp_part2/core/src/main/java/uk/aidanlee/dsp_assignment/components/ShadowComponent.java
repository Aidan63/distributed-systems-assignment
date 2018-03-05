package uk.aidanlee.dsp_assignment.components;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import uk.aidanlee.dsp_assignment.structural.ec.Component;
import uk.aidanlee.dsp_assignment.structural.ec.Visual;

public class ShadowComponent extends Component {
    private Visual sprite;

    public ShadowComponent(String _name) {
        super(_name);
    }

    @Override
    public void onadded() {
        sprite = (Visual) entity;
    }

    public void draw(SpriteBatch _batch) {
        _batch.setColor(0.0f, 0.0f, 0.0f, 0.5f);
        _batch.draw(sprite.region, entity.pos.x + 10, entity.pos.y + 10, entity.origin.x, entity.origin.y, sprite.region.getRegionWidth(), sprite.region.getRegionHeight(), sprite.scale.x, sprite.scale.y, entity.rotation);
        _batch.setColor(1, 1, 1, 1);
    }
}
