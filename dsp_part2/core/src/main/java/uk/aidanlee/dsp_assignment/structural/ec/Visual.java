package uk.aidanlee.dsp_assignment.structural.ec;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * Convenience wrapper over the Entity class for an entity with a visual image attached.
 * Contains basic rendering information and a draw function.
 */
public class Visual extends Entity {
    /**
     * Texture region for this entity.
     */
    public TextureRegion region;

    /**
     * Tint colour for this entity.
     */
    public Color color;

    /**
     * Scale of this entity.
     */
    public Vector2 scale;

    /**
     * Creates a new visual entity with the provided name.
     * @param _name name of the entity.
     */
    public Visual(String _name) {
        super(_name);

        scale = new Vector2(1, 1);
        color = Color.WHITE;
    }

    // Public API

    /**
     * Draws the visual with the provided sprite batch.
     * @param _batch The sprite batch to draw with.
     */
    public void draw(SpriteBatch _batch) {
        _batch.setColor(color);
        _batch.draw(region, pos.x, pos.y, origin.x, origin.y, region.getRegionWidth(), region.getRegionHeight(), scale.x, scale.y, rotation);
        _batch.setColor(1, 1, 1, 1);
    }
}
