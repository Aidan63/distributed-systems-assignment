package uk.aidanlee.dsp.common.structural;

import com.badlogic.gdx.math.Rectangle;

/**
 * Interface a class must implement in order to be placed in a quad tree.
 */
public interface IQuadtreeElement {
    Rectangle box();
}
