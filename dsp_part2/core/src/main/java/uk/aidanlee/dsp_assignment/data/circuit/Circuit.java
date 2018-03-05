package uk.aidanlee.dsp_assignment.data.circuit;

import java.util.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.google.gson.Gson;

import uk.aidanlee.dsp_assignment.components.PolygonComponent;
import uk.aidanlee.dsp_assignment.components.QuadtreeComponent;
import uk.aidanlee.dsp_assignment.components.track.BoostPadComponent;
import uk.aidanlee.dsp_assignment.race.Race;
import uk.aidanlee.dsp_assignment.structural.Quadtree;
import uk.aidanlee.dsp_assignment.structural.ec.Entity;
import uk.aidanlee.dsp_assignment.geometry.QuadMesh;
import uk.aidanlee.jDiffer.math.Vector;
import uk.aidanlee.jDiffer.shapes.InfiniteState;
import uk.aidanlee.jDiffer.shapes.Ray;

public class Circuit {
    /**
     * Holds various information about the track.
     */
    private CircuitInfo info;

    /**
     * The first point in the circuit.
     * This is the point where the finish line occurs.
     */
    private CircuitPoint firstPoint;

    /**
     * All of the sub points in the circuit.
     */
    private CircuitPoint[] points;

    /**
     * All of the tiles in the circuit.
     */
    private CircuitTile[] tiles;

    /**
     * Holds spawn point information.
     */
    private CircuitSpawn spawn;

    /**
     * Quadtree for the wall elements.
     */
    private Quadtree<TreeTileWall> wallTree;

    /**
     * Quadtree for the boostpad elements.
     */
    private Quadtree<QuadtreeComponent> padTree;

    /**
     * All the checkpoints in this circuit. One appears every 1/4 of the track.
     * checkpoints[0] is the ray at the start / finish line.
     */
    private List<Ray> checkpoints;

    /**
     * Mesh geometry of the track.
     */
    private QuadMesh mesh;

    /**
     * All of the boost pad entities in this circuit.
     */
    private List<Entity> boostPads;

    /**
     * Maps the mesh's packed quad UUIDs to the CircuitTile IDs.
     */
    private Map<String, UUID> quadIDs;

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

    public QuadMesh getMesh() {
        return mesh;
    }

    public Quadtree<TreeTileWall> getWallTree() {
        return wallTree;
    }

    public Quadtree<QuadtreeComponent> getPadTree() {
        return padTree;
    }

    public Map<String, UUID> getQuadIDs() {
        return quadIDs;
    }

    public List<Entity> getBoostPads() {
        return boostPads;
    }

    public List<Ray> getCheckpoints() {
        return checkpoints;
    }

    /**
     *
     */
    public void load() {
        Gson gson = new Gson();
        CircuitJson json = gson.fromJson(Gdx.files.internal("assets/tracks/track.p2").readString(), CircuitJson.class);

        info   = json.info;
        points = json.points;
        tiles  = json.tiles;
        spawn  = json.spawn;

        createdLinkedList();
        createGeometry();
        createQuadTree();
        createCheckPoints();
        applySettings();
    }

    /**
     *
     */
    public void dispose() {
        mesh.dispose();
    }

    /**
     *
     */
    private void createdLinkedList() {
        firstPoint = points[0];

        for (int i = 0; i < points.length; i++) {
            CircuitPoint thisPoint = points[i];
            CircuitPoint nextPoint = i == 0 ? points[points.length - 1] : points[i - 1];
            CircuitPoint prevPoint = i == points.length - 1 ? points[0] : points[i + 1];

            thisPoint.next = nextPoint;
            thisPoint.prev = prevPoint;
        }
    }

    /**
     *
     */
    private void createGeometry() {
        quadIDs = new HashMap<>();
        mesh    = new QuadMesh(false, tiles.length, Race.resources.trackAtlas);

        for (int i = 0; i < tiles.length; i++) {
            UUID id = mesh.addQuad(tiles[i].frame, tiles[i].verts[0], tiles[i].verts[1], tiles[i].verts[2], tiles[i].verts[3], Color.WHITE, true, (i % 2 != 0));
            quadIDs.put(tiles[i].id, id);
        }

        mesh.rebuild();
    }

    /**
     *
     */
    private void createQuadTree() {
        wallTree = new Quadtree<>(new Rectangle(0, 0, 20000, 20000), 5, 100);
        padTree  = new Quadtree<>(new Rectangle(0, 0, 20000, 20000), 5, 100);

        // Add walls into the tree
        for (CircuitPoint point : points) {
            wallTree.add(TreeTileWall.createNegativeWall(point));
            wallTree.add(TreeTileWall.createPositiveWall(point));
        }
    }

    /**
     *
     */
    private void createCheckPoints() {
        checkpoints = new LinkedList<>();

        int it = points.length / 4;
        for (int i = 0; i < 4; i++) {
            int index = (int) Math.ceil((it * i) + spawn.startIndex) % points.length;
            checkpoints.add(new Ray(
                    new Vector(points[index].negativePoint.x, points[index].negativePoint.y),
                    new Vector(points[index].positivePoint.x, points[index].positivePoint.y),
                    InfiniteState.not_infinite
            ));
        }
    }

    /**
     *
     */
    private void applySettings() {
        boostPads = new LinkedList<>();

        if (Race.settings.isBoostPads()) {
            for (CircuitTile tile : tiles) {
                if (!tile.frame.equals("boost")) continue;

                Entity boostPad = new Entity("boost_pad");

                QuadtreeComponent element = new QuadtreeComponent("quadtree_pad", tile.verts);
                padTree.add(element);

                boostPad.add(element);
                boostPad.add(new PolygonComponent("polygon", tile.verts));
                boostPad.add(new BoostPadComponent("boost_pad", tile.id));

                boostPads.add(boostPad);
            }
        }
    }

    private class CircuitJson {
        private CircuitPoint[] points;
        private CircuitTile[] tiles;
        private CircuitSpawn spawn;
        private CircuitInfo info;
    }
}
