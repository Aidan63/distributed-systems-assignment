package uk.aidanlee.dsp_assignment.structural;

import com.badlogic.gdx.math.Rectangle;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Quadtree<T extends IQuadtreeElement> {
    /**
     * All of the elements in this quad tree.
     */
    private List<T> entities;

    /**
     * The boundary of this quadtree.
     */
    private Rectangle boundary;

    /**
     * How far down the tree this quadtree is.
     */
    private int depth;

    /**
     * The max depth this quadtree can go.
     */
    private int maxDepth;

    /**
     * The number of elements needed before the tree is split.
     */
    private int minElementsBeforeSplit;


    // Four children quads.
    private Quadtree<T> topLeft;
    private Quadtree<T> topRight;
    private Quadtree<T> bottomRight;
    private Quadtree<T> bottomLeft;

    /**
     * Creates a new quad tree structure.
     * @param _bounds                 The total rectangle this quad tree covers.
     * @param _minElementsBeforeSplit Minimum number of elements needed at each level before it splits.
     * @param _maxDepth               The maximum depth this tree can split to.
     */
    public Quadtree(Rectangle _bounds, int _minElementsBeforeSplit, int _maxDepth) {
        entities = new LinkedList<>();

        boundary = _bounds;
        maxDepth = _maxDepth;
        minElementsBeforeSplit = _minElementsBeforeSplit;
    }

    /**
     * Add the provided element to the tree.
     *
     * @param _object The elements to add.
     * @return If the element was successfully added.
     */
    public boolean add(T _object) {
        if (!boundary.contains(_object.box())) return false;

        // Try to add it in a children.
        if (depth < maxDepth && (topLeft != null || entities.size() >=  minElementsBeforeSplit))
        {
            if (topLeft == null) split();

            if (topLeft.add(_object)) { return true; }
            if (topRight.add(_object)) { return true; }
            if (bottomRight.add(_object)) { return true; }
            if (bottomLeft.add(_object)) { return true; }
        }

        // Else add here.
        entities.add(_object);
        return true;
    }

    /**
     * Remove an element of the tree.
     * Doesn't change its data structure.
     *
     * @param _object The element to remove.
     * @return If the element was removed.
     */
    public boolean remove(T _object) {
        if (topLeft == null) return entities.remove(_object);

        return entities.remove(_object) || topLeft.remove(_object) || topRight.remove(_object) || bottomLeft.remove(_object) || bottomRight.remove(_object);
    }

    /**
     * Return all elements which collide with the given box.
     *
     * @param _rectangle The rectangle to check for collisions with.
     * @param _storage The storage instance to store collided elements in.
     * @return The initially provided storage instance with any elements added.
     */
    public List<T> getCollisions(Rectangle _rectangle, List<T> _storage) {
        if (!boundary.overlaps(_rectangle)) return  _storage;

        // Add all from this level who intersect
        for (T e : entities) {
            if (e.box().overlaps(_rectangle)) {
                _storage.add(e);
            }
        }

        if (topLeft == null) return _storage;

        topLeft.getCollisions(_rectangle, _storage);
        topRight.getCollisions(_rectangle, _storage);
        bottomRight.getCollisions(_rectangle, _storage);
        bottomLeft.getCollisions(_rectangle, _storage);

        return _storage;
    }

    /**
     * Create a new tree node.
     * Only used to create children.
     *
     * @param _boundary The boundary of the new node.
     * @return The quadtree instance.
     */
    private Quadtree<T> getChildTree(Rectangle _boundary) {
        Quadtree<T> child = new Quadtree<>(_boundary, minElementsBeforeSplit, maxDepth);
        child.depth = depth + 1;

        return child;
    }

    /**
     * Split the current node to add children.
     * Re-balance the current entities if they can fit in a lower node.
     */
    private void split() {
        float leftWidth = boundary.width  / 2;
        float topHeight = boundary.height / 2;

        float rightStartX = boundary.x + leftWidth + 1;
        float rightWidth  = boundary.width - (leftWidth + 1);

        float botStartY = boundary.y + topHeight + 1;
        float botHeight = boundary.height - (topHeight + 1);

        topLeft     = getChildTree(new Rectangle(boundary.x , boundary.y, leftWidth , topHeight));
        topRight    = getChildTree(new Rectangle(rightStartX, boundary.y, rightWidth, topHeight));
        bottomRight = getChildTree(new Rectangle(rightStartX, botStartY , rightWidth, botHeight));
        bottomLeft  = getChildTree(new Rectangle(boundary.x , botStartY , leftWidth , botHeight));

        balance();
    }

    /**
     * Move entities who can fit in a lower node.
     */
    private void balance()
    {
        entities = entities.stream().filter(e -> !(topLeft.add(e) || topRight.add(e) || bottomRight.add(e) || bottomLeft.add(e))).collect(Collectors.toList());
    }
}
