package uk.aidanlee.dsp_assignment.data.circuit;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import uk.aidanlee.jDiffer.math.Vector;
import uk.aidanlee.jDiffer.shapes.InfiniteState;
import uk.aidanlee.jDiffer.shapes.Polygon;
import uk.aidanlee.jDiffer.shapes.Ray;
import uk.aidanlee.dsp_assignment.structural.IQuadtreeElement;

public class TreeTileWall implements IQuadtreeElement {
    private Rectangle aabb;

    public String tileID;
    public Polygon wall;
    public Ray ray;

    public Vector2 tangent;
    public Vector2 normal;

    @Override
    public Rectangle box() {
        return aabb;
    }

    public static TreeTileWall createNegativeWall(CircuitPoint _point) {
        Vector2 startPoint = new Vector2(_point.negativePoint.x + (_point.tangent.x * 64), _point.negativePoint.y + (_point.tangent.y * 64));
        Vector2 endPoint   = new Vector2(_point.next.negativePoint.x - (_point.next.tangent.x * 64), _point.next.negativePoint.y - (_point.next.tangent.y * 64));

        TreeTileWall wall = new TreeTileWall();
        wall.tileID = _point.negativeTile.id;
        wall.aabb   = new Rectangle(
                Math.min(_point.negativePoint.x, _point.next.negativePoint.x),
                Math.min(_point.negativePoint.y, _point.next.negativePoint.y),
                Math.abs(_point.negativePoint.x - _point.next.negativePoint.x),
                Math.abs(_point.negativePoint.y - _point.next.negativePoint.y));

        wall.wall = new Polygon(0, 0, new Vector[] {
                new Vector(_point.negativePoint.x, _point.negativePoint.y),
                new Vector(startPoint.x - (_point.normal.x * 64), startPoint.y - (_point.normal.y * 64)),
                new Vector(endPoint.x - (_point.next.normal.x * 64), endPoint.y - (_point.next.normal.y * 64)),
                new Vector(_point.next.negativePoint.x, _point.next.negativePoint.y)
        });
        wall.wall.set_position(new Vector(0, 0));
        wall.wall.set_rotation(0);

        wall.ray  = new Ray(new Vector(_point.negativePoint.x, _point.negativePoint.y), new Vector(_point.next.negativePoint.x, _point.next.negativePoint.y), InfiniteState.not_infinite);
        wall.normal  = _point.normal;
        wall.tangent = _point.tangent;

        return wall;
    }

    public static TreeTileWall createPositiveWall(CircuitPoint _point) {
        Vector2 startPoint = new Vector2(_point.positivePoint.x + (_point.tangent.x * 64), _point.positivePoint.y + (_point.tangent.y * 64));
        Vector2 endPoint   = new Vector2(_point.next.positivePoint.x - (_point.next.tangent.x * 64), _point.next.positivePoint.y - (_point.next.tangent.y * 64));

        TreeTileWall wall = new TreeTileWall();
        wall.tileID = _point.negativeTile.id;

        wall.aabb   = new Rectangle(
                Math.min(_point.positivePoint.x, _point.next.positivePoint.x),
                Math.min(_point.positivePoint.y, _point.next.positivePoint.y),
                Math.abs(_point.positivePoint.x - _point.next.positivePoint.x),
                Math.abs(_point.positivePoint.y - _point.next.positivePoint.y));
        wall.wall = new Polygon(0, 0, new Vector[] {
                new Vector(_point.positivePoint.x, _point.positivePoint.y),
                new Vector(startPoint.x + (_point.normal.x * 64), startPoint.y + (_point.normal.y * 64)),
                new Vector(endPoint.x + (_point.next.normal.x * 64), endPoint.y + (_point.next.normal.y * 64)),
                new Vector(_point.next.positivePoint.x, _point.next.positivePoint.y)
        });
        wall.wall.set_position(new Vector(0, 0));
        wall.wall.set_rotation(0);

        wall.ray = new Ray(new Vector(_point.positivePoint.x, _point.positivePoint.y), new Vector(_point.next.positivePoint.x, _point.next.positivePoint.y), InfiniteState.not_infinite);
        wall.normal  = _point.normal;
        wall.tangent = _point.tangent;

        return wall;
    }
}
