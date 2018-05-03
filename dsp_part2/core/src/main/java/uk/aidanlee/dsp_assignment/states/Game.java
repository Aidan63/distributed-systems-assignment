package uk.aidanlee.dsp_assignment.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Timer;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import uk.aidanlee.dsp_assignment.components.AABBComponent;
import uk.aidanlee.dsp_assignment.components.PolygonComponent;
import uk.aidanlee.dsp_assignment.components.ShadowComponent;
import uk.aidanlee.dsp_assignment.components.TrailComponent;
import uk.aidanlee.dsp_assignment.data.*;
import uk.aidanlee.dsp_assignment.data.circuit.Circuit;
import uk.aidanlee.dsp_assignment.data.circuit.TreeTileWall;
import uk.aidanlee.dsp_assignment.data.events.EvLapTime;
import uk.aidanlee.dsp_assignment.geometry.MeshBatch;
import uk.aidanlee.dsp_assignment.geometry.QuadMesh;
import uk.aidanlee.dsp_assignment.states.race.RaceCountdownState;
import uk.aidanlee.dsp_assignment.states.race.RaceResultsState;
import uk.aidanlee.dsp_assignment.states.race.RaceState;
import uk.aidanlee.dsp_assignment.structural.StateMachine;
import uk.aidanlee.dsp_assignment.structural.ec.Entity;
import uk.aidanlee.dsp_assignment.structural.ec.Visual;
import uk.aidanlee.dsp_assignment.race.RaceSettings;
import uk.aidanlee.dsp_assignment.structural.State;
import uk.aidanlee.jDiffer.Collision;
import uk.aidanlee.jDiffer.data.ShapeCollision;
import uk.aidanlee.jDiffer.shapes.Polygon;

import java.util.LinkedList;
import java.util.List;

public class Game extends State {

    /**
     * All resources (images, track data, etc) used by the client.
     */
    private Resources resources;

    /**
     * Access to the event bus.
     */
    private EventBus events;

    /**
     * Stores all the local player entities.
     */
    private Craft craft;

    /**
     * Loads the track data needed for calculating collisions and drawing the track.
     */
    private Circuit circuit;

    /**
     * Manages the viewports for the local players.
     */
    private Views views;

    /**
     * Holds lap times for all clients.
     */
    private Times times;

    /**
     * Race sub state machine.
     */
    private StateMachine raceState;

    /**
     * Batcher to draw ship images with.
     */
    private SpriteBatch spriteBatcher;

    /**
     * Batcher to draw the track and trail meshes with.
     */
    private MeshBatch meshBatcher;

    /**
     * The mesh of the track data used to draw a visual representation of the track.
     */
    private QuadMesh trackMesh;

    /**
     * Draws the HUDs for the local players.
     */
    private HUD[] huds;

    /**
     * Time which starts when the results are shown.
     * Once triggered the game returns to the menu.
     */
    private Timer.Task resultsTimer;

    public Game(String _name, Resources _resources) {
        super(_name);

        resources = _resources;
    }

    @Override
    public void onEnter(Object _enterWith) {
        RaceSettings settings = (RaceSettings) _enterWith;

        events = new EventBus();
        events.register(this);

        circuit = new Circuit(Gdx.files.internal("tracks/track.p2"));
        views   = new Views(settings.getLocalPlayers(), settings.getSplit());
        craft   = new Craft(settings.getPlayers(), circuit, views, resources);
        times   = new Times(craft.getLocalPlayers(), 3);

        spriteBatcher = new SpriteBatch();
        meshBatcher   = new MeshBatch(resources.trackAtlas.getTextures().first());
        meshBatcher.addShader("track", new ShaderProgram(Gdx.files.internal("shaders/track.vert"), Gdx.files.internal("shaders/track.frag")));
        meshBatcher.addShader("trail", new ShaderProgram(Gdx.files.internal("shaders/trail.vert"), Gdx.files.internal("shaders/trail.frag")));

        // Build the track mesh.
        trackMesh = new QuadMesh(false, circuit.getTiles().length, resources.trackAtlas);
        for (int i = 0; i < circuit.getTiles().length; i++) {
            trackMesh.addQuad(
                    circuit.getTiles()[i].frame,
                    circuit.getTiles()[i].verts[0],
                    circuit.getTiles()[i].verts[1],
                    circuit.getTiles()[i].verts[2],
                    circuit.getTiles()[i].verts[3],
                    Color.WHITE, true, (i % 2 != 0));
        }
        trackMesh.rebuild();

        huds = new HUD[settings.getLocalPlayers()];
        for (int i = 0; i < settings.getLocalPlayers(); i++) {
            huds[i] = new HUD(resources);
            huds[i].showCountdown();
        }

        // Race state machine
        raceState = new StateMachine();
        raceState.add(new RaceCountdownState("countdown"));
        raceState.add(new RaceState("race", craft, times, huds));
        raceState.add(new RaceResultsState("results"));
        raceState.set("countdown", null, null);

        for (Entity e : craft.getLocalPlayers()) {
            e.getEvents().register(this);
        }

    }

    @Override
    public void onLeave(Object _leaveWith) {
        super.onLeave(_leaveWith);

        spriteBatcher.dispose();
        meshBatcher.dispose();

        resultsTimer = null;
    }

    @Override
    public void onUpdate() {
        simulatePlayers();

        resolveWallCollisions();

        resolveCraftCollisions();

        raceState.update();

        if (resultsTimer == null && times.allPlayersFinished()) {
            Timer.Task task = new Timer.Task() {
                @Override
                public void run() {
                    machine.set("menu", null, null);
                }
            };

            resultsTimer = Timer.schedule(task, 10);
        }
    }

    @Override
    public void onRender() {

        // For each local player draw what they would see.
        for (int i = 0; i < views.getCameras().length; i++) {

            // Setup the viewport and get the camera projection matrix
            views.getViewports()[i].apply();
            Matrix4 cameraProjection = views.getCameras()[i].combined;

            // Draw the track.
            meshBatcher.begin();
            meshBatcher.setShader("track", cameraProjection);
            meshBatcher.draw(trackMesh.getMesh(), GL20.GL_TRIANGLES);
            meshBatcher.end();

            // Draw player shadows
            spriteBatcher.setProjectionMatrix(cameraProjection);
            spriteBatcher.begin();
            for (Visual craft : craft.getLocalPlayers()) {
                ((ShadowComponent)craft.get("shadow")).draw(spriteBatcher);
            }
            spriteBatcher.end();

            // Draw the trails.
            meshBatcher.begin();
            meshBatcher.setShader("trail", cameraProjection);
            for (Entity craft : craft.getLocalPlayers()) {
                TrailComponent trail = (TrailComponent) craft.get("trail");
                meshBatcher.draw(trail.mesh, GL20.GL_TRIANGLE_STRIP);
            }
            meshBatcher.end();

            // Draw all of the player craft.
            spriteBatcher.setProjectionMatrix(cameraProjection);
            spriteBatcher.begin();
            for (Visual craft : craft.getLocalPlayers()) {
                craft.draw(spriteBatcher);
            }
            spriteBatcher.end();

            huds[i].resize(views.getViewports()[i].getScreenX(), views.getViewports()[i].getScreenY(), views.getViewports()[i].getScreenWidth(), views.getViewports()[i].getScreenHeight());
            huds[i].render();
        }
    }

    /**
     * Called when a craft entity has completed a lap. Adds the lap time into the times storage.
     * @param _event Contains the time of the lap.
     */
    @Subscribe
    public void onLapTime(EvLapTime _event) {
        times.addTime(_event.name, _event.time);
    }

    /**
     *
     */
    private void simulatePlayers() {
        // Resize the views in case the window size has changed and update all player entities.
        views.resize();

        // Update entities.
        for (Entity e : craft.getLocalPlayers()) {
            e.update(0);
        }
    }

    /**
     * Resolve any wall collisions between the player craft.
     */
    private void resolveWallCollisions() {
        for (Entity e : craft.getLocalPlayers()) {

            // Get the entity and ensure it has the AABB and poly components
            if (!e.has("aabb") || !e.has("polygon")) continue;

            // Get the components and query the circuit wall tree for collisions.
            AABBComponent aabb = (AABBComponent) e.get("aabb");
            PolygonComponent poly = (PolygonComponent) e.get("polygon");

            List<TreeTileWall> collisions = new LinkedList<>();
            circuit.getWallTree().getCollisions(aabb.getBox(), collisions);

            // If there are no collisions skip this loop.
            if (collisions.size() == 0) continue;

            // Check each AABB collision for a precise collision.
            for (TreeTileWall col : collisions) {
                Polygon transformedPoly = poly.getShape();

                ShapeCollision wallCol = Collision.shapeWithShape(transformedPoly, col.getPolygon(), null);
                if (wallCol == null) continue;

                e.pos.x += wallCol.separationX;
                e.pos.y += wallCol.separationY;
            }
        }
    }

    /**
     * Moves any colliding ship entities apart from each other.
     */
    private void resolveCraftCollisions() {
        for (Entity e : craft.getLocalPlayers()) {

            // Get the entity and ensure it has the AABB and poly components
            if (!e.has("aabb") || !e.has("polygon")) continue;

            // Get the components and query the circuit wall tree for collisions.
            AABBComponent    aabb = (AABBComponent) e.get("aabb");
            PolygonComponent poly = (PolygonComponent) e.get("polygon");

            // Check for collisions with all other entities
            for (Entity craft : craft.getLocalPlayers()) {
                if (craft == null) continue;
                if (craft.getName().equals(e.getName())) continue;

                Rectangle otherBox = ((AABBComponent) craft.get("aabb")).getBox();
                if (!aabb.getBox().overlaps(otherBox)) continue;

                PolygonComponent otherPoly = (PolygonComponent) craft.get("polygon");
                ShapeCollision col = Collision.shapeWithShape(poly.getShape(), otherPoly.getShape(), null);
                while (col != null) {
                    // If we are colliding move apart until we aren't.
                    e.pos.x += (float)col.unitVectorX;
                    e.pos.y += (float)col.unitVectorY;
                    craft.pos.x -= (float)col.otherUnitVectorX;
                    craft.pos.y -= (float)col.otherUnitVectorY;

                    col = Collision.shapeWithShape(poly.getShape(), otherPoly.getShape(), null);
                }
            }
        }
    }
}
