package uk.aidanlee.dsp.common.data.circuit;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import uk.aidanlee.dsp.common.structural.Quadtree;
import uk.aidanlee.dsp.common.structural.ec.Entity;
import uk.aidanlee.jDiffer.math.Vector;
import uk.aidanlee.jDiffer.shapes.InfiniteState;
import uk.aidanlee.jDiffer.shapes.Ray;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Circuit {
    private CircuitInfo info;

    private CircuitPoint firstPoint;

    private CircuitPoint[] points;

    private CircuitTile[] tiles;

    private CircuitSpawn spawn;

    private Quadtree<TreeTileWall> wallTree;

    private Ray[] checkpoints;

    private List<Entity> boostpads;

    private Map<String, UUID> quadIDs;

    // Constructors

    public Circuit(String _filePath) {
        URL url = getClass().getResource(_filePath);

        try {
            Gson gson = new Gson();
            CircuitJson json = gson.fromJson(Resources.toString(url, Charsets.UTF_8), CircuitJson.class);

            info   = json.info;
            points = json.points;
            tiles  = json.tiles;
            spawn  = json.spawn;

            createLinkedList();
            createQuadTree();
            createCheckPoints();
            applySettings();
        } catch (IOException _ex) {
            System.out.println("Failed to load track resource : " + _ex.getMessage());
        }
    }

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
        applySettings();
    }

    // Getters and Setters

    public CircuitInfo getInfo() {
        return info;
    }

    public CircuitPoint getFirstPoint() {
        return firstPoint;
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

    private void load(String _filePath) {

    }

    private void createLinkedList() {
        firstPoint = points[0];

        for (int i = 0; i < points.length; i++) {
            CircuitPoint thisPoint = points[i];
            CircuitPoint nextPoint = i == 0 ? points[points.length - 1] : points[i - 1];
            CircuitPoint prevPoint = i == points.length - 1 ? points[0] : points[i + 1];

            thisPoint.next = nextPoint;
            thisPoint.prev = prevPoint;
        }
    }

    private void createQuadTree() {
        wallTree = new Quadtree<>(new Rectangle(0, 0, 20000, 20000), 5, 100);

        for (CircuitPoint point : points) {
            wallTree.add(TreeTileWall.createNegativeWall(point));
            wallTree.add(TreeTileWall.createPositiveWall(point));
        }
    }

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

    private void applySettings() {
        //
    }

    private class CircuitJson {
        private CircuitPoint[] points;
        private CircuitTile[] tiles;
        private CircuitSpawn spawn;
        private CircuitInfo info;
    }
}
