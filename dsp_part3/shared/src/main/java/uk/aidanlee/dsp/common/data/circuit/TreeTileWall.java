package uk.aidanlee.dsp.common.data.circuit;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import uk.aidanlee.dsp.common.structural.IQuadtreeElement;
import uk.aidanlee.jDiffer.math.Vector;
import uk.aidanlee.jDiffer.shapes.InfiniteState;
import uk.aidanlee.jDiffer.shapes.Polygon;
import uk.aidanlee.jDiffer.shapes.Ray;

/**
 * The track is split up into a series of walls for each tile in the track. All of these tiles are inserted into the quad tree
 * for fast AABB collision checking before performing slow precise collisions.
 *
 * This tile class implements the quad tree interface and holds the AABB bounding box and precise polygon for a wall.
 */
public class TreeTileWall implements IQuadtreeElement {

    /**
     * Bounding box for this tree item.
     */
    private Rectangle aabb;

    /**
     * Polygon shape for collision checking with this wall.
     */
    private Polygon polygon;

    /**
     * Tangent for the wall.
     */
    private Vector2 tangent;

    /**
     * Normal of the tangent for the wall.
     */
    private Vector2 normal;

    @Override
    public Rectangle box() {
        return aabb;
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public Vector2 getTangent() {
        return tangent;
    }

    public Vector2 getNormal() {
        return normal;
    }

    /**
     * Creates a trapezoid collision polygon, calculates normal and tangents, and a bounding box for a negative track tile.
     * @param _point Circuit point to create the negative tile for.
     * @return TreeTileWall instance.
     */
    public static TreeTileWall createNegativeWall(CircuitPoint _point) {
        Vector2 startPoint = new Vector2(_point.negativePoint.x + (_point.tangent.x * 64), _point.negativePoint.y + (_point.tangent.y * 64));
        Vector2 endPoint   = new Vector2(_point.next.negativePoint.x - (_point.next.tangent.x * 64), _point.next.negativePoint.y - (_point.next.tangent.y * 64));

        TreeTileWall wall = new TreeTileWall();

        // Calculate the bounding box for the precise collision polygon.
        wall.aabb = new Rectangle(
                Math.min(_point.negativePoint.x, _point.next.negativePoint.x),
                Math.min(_point.negativePoint.y, _point.next.negativePoint.y),
                Math.abs(_point.negativePoint.x - _point.next.negativePoint.x),
                Math.abs(_point.negativePoint.y - _point.next.negativePoint.y)
        );

        // Create a trapezoid collision polygon for precise collision checking.
        wall.polygon = new Polygon(0, 0, new Vector[] {
                new Vector(_point.negativePoint.x, _point.negativePoint.y),
                new Vector(startPoint.x - (_point.normal.x * 64), startPoint.y - (_point.normal.y * 64)),
                new Vector(endPoint.x - (_point.next.normal.x * 64), endPoint.y - (_point.next.normal.y * 64)),
                new Vector(_point.next.negativePoint.x, _point.next.negativePoint.y)
        });

        wall.polygon.set_position(new Vector(0, 0));
        wall.polygon.set_rotation(0);

        wall.normal  = _point.normal;
        wall.tangent = _point.tangent;

        return wall;
    }

    /**
     * Creates a trapezoid collision polygon, calculates normal and tangents, and a bounding box for a positive track tile.
     * @param _point Circuit point to create the file for.
     * @return TreeTileWall instance.
     */
    public static TreeTileWall createPositiveWall(CircuitPoint _point) {
        Vector2 startPoint = new Vector2(_point.positivePoint.x + (_point.tangent.x * 64), _point.positivePoint.y + (_point.tangent.y * 64));
        Vector2 endPoint   = new Vector2(_point.next.positivePoint.x - (_point.next.tangent.x * 64), _point.next.positivePoint.y - (_point.next.tangent.y * 64));

        TreeTileWall wall = new TreeTileWall();

        // Calculate the bounding box for the precise collision polygon.
        wall.aabb = new Rectangle(
                Math.min(_point.positivePoint.x, _point.next.positivePoint.x),
                Math.min(_point.positivePoint.y, _point.next.positivePoint.y),
                Math.abs(_point.positivePoint.x - _point.next.positivePoint.x),
                Math.abs(_point.positivePoint.y - _point.next.positivePoint.y)
        );

        // Create a trapezoid collision polygon for precise collision checking.
        wall.polygon = new Polygon(0, 0, new Vector[] {
                new Vector(_point.positivePoint.x, _point.positivePoint.y),
                new Vector(startPoint.x + (_point.normal.x * 64), startPoint.y + (_point.normal.y * 64)),
                new Vector(endPoint.x + (_point.next.normal.x * 64), endPoint.y + (_point.next.normal.y * 64)),
                new Vector(_point.next.positivePoint.x, _point.next.positivePoint.y)
        });
        wall.polygon.set_position(new Vector(0, 0));
        wall.polygon.set_rotation(0);

        wall.normal  = _point.normal;
        wall.tangent = _point.tangent;

        return wall;
    }
}
