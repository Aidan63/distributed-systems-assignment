package uk.aidanlee.dsp_assignment.data.circuit;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.google.gson.Gson;

import uk.aidanlee.dsp_assignment.structural.Quadtree;
import uk.aidanlee.jDiffer.math.Vector;
import uk.aidanlee.jDiffer.shapes.InfiniteState;
import uk.aidanlee.jDiffer.shapes.Ray;

/**
 * Circuit class holds all data on a track circuit. This information is used by the server and client to perform collision detection
 * and by the client to draw a visual representation of the track.
 *
 * Track information is stored in a JSON file and is parsed out using GSON.
 */
public class Circuit {
    /**
     * General information about the track, e.g. Author, track name. Currently unused.
     */
    private CircuitInfo info;

    /**
     * All of the points in this circuit.
     */
    private CircuitPoint[] points;

    /**
     * All of the tiles in this circuit.
     */
    private CircuitTile[] tiles;

    /**
     * Information on all of the circuit spawns.
     */
    private CircuitSpawn spawn;

    /**
     * Quad tree containing all wall collision data.
     */
    private Quadtree<TreeTileWall> wallTree;

    /**
     * Array of four checkpoints to track player lap progress.
     */
    private Ray[] checkpoints;

    // Constructors

    /**
     * Loads a track from a LibGDX file handle.
     * @param _handle File handle.
     */
    public Circuit(FileHandle _handle) {
        Gson gson = new Gson();
        CircuitJson json = gson.fromJson(_handle.readString(), CircuitJson.class);

        info   = json.info;
        points = json.points;
        tiles  = json.tiles;
        spawn  = json.spawn;

        createLinkedList();
        createQuadTree();
        createCheckPoints();
    }

    // Getters and Setters

    public CircuitInfo getInfo() {
        return info;
    }

    public CircuitPoint[] getPoints() {
        return points;
    }

    public CircuitTile[] getTiles() {
        return tiles;
    }

    public CircuitSpawn getSpawn() {
        return spawn;
    }

    public Quadtree<TreeTileWall> getWallTree() {
        return wallTree;
    }

    public Ray[] getCheckpoints() {
        return checkpoints;
    }

    // Public API

    /**
     * re-constructs the circular, doubly linked list from all of the circuit points.
     */
    private void createLinkedList() {

        for (int i = 0; i < points.length; i++) {
            CircuitPoint thisPoint = points[i];
            CircuitPoint nextPoint = i == 0 ? points[points.length - 1] : points[i - 1];
            CircuitPoint prevPoint = i == points.length - 1 ? points[0] : points[i + 1];

            thisPoint.next = nextPoint;
            thisPoint.prev = prevPoint;
        }
    }

    /**
     * Creates the collision quad tree from all circuit points.
     */
    private void createQuadTree() {
        wallTree = new Quadtree<>(new Rectangle(0, 0, 20000, 20000), 5, 100);

        for (CircuitPoint point : points) {
            wallTree.add(TreeTileWall.createNegativeWall(point));
            wallTree.add(TreeTileWall.createPositiveWall(point));
        }
    }

    /**
     * Creates checkpoints at each quarter way around the track. First checkpoint is also the start / finish line.
     */
    private void createCheckPoints() {
        checkpoints = new Ray[4];

        int it = points.length / 4;
        for (int i = 0; i < 4; i++) {
            int index = (int) Math.ceil((it * i) + spawn.startIndex) % points.length;
            checkpoints[i] = new Ray(
                    new Vector(points[index].negativePoint.x, points[index].negativePoint.y),
                    new Vector(points[index].positivePoint.x, points[index].positivePoint.y),
                    InfiniteState.not_infinite);
        }
    }

    /**
     * Class which represents the overall structure of the track JSON files.
     * Used by GSON to parse and map data onto the java classes.
     */
    private class CircuitJson {
        private CircuitPoint[] points;
        private CircuitTile[] tiles;
        private CircuitSpawn spawn;
        private CircuitInfo info;
    }
}
