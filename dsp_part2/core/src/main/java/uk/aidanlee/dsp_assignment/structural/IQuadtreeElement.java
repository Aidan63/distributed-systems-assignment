package uk.aidanlee.dsp_assignment.structural;

import com.badlogic.gdx.math.Rectangle;

/**
 * Interface a class must implement in order to be placed in a quad tree.
 */
public interface IQuadtreeElement {
    Rectangle box();
}
