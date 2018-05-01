package uk.aidanlee.dsp_assignment.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.google.common.eventbus.EventBus;
import uk.aidanlee.dsp_assignment.components.ShadowComponent;
import uk.aidanlee.dsp_assignment.components.TrailComponent;
import uk.aidanlee.dsp_assignment.data.Craft;
import uk.aidanlee.dsp_assignment.data.HUD;
import uk.aidanlee.dsp_assignment.data.Resources;
import uk.aidanlee.dsp_assignment.data.Views;
import uk.aidanlee.dsp_assignment.data.circuit.Circuit;
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

public class Game extends State {

    private Resources resources;

    private EventBus events;

    private Craft craft;

    private Circuit circuit;

    private Views views;

    private StateMachine raceState;

    private SpriteBatch spriteBatcher;

    private MeshBatch meshBatcher;

    private QuadMesh trackMesh;

    private HUD[] huds;

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

        // Race state machine
        raceState = new StateMachine();
        raceState.add(new RaceCountdownState("countdown"));
        raceState.add(new RaceState("race", circuit, craft, views));
        raceState.add(new RaceResultsState("results"));
        raceState.set("race", null, null);

        huds = new HUD[settings.getLocalPlayers()];
        for (int i = 0; i < settings.getLocalPlayers(); i++) {
            huds[i] = new HUD(resources);
            huds[i].showRace(craft.getLocalPlayers()[i]);
        }

    }

    @Override
    public void onLeave(Object _leaveWith) {
        super.onLeave(_leaveWith);

        spriteBatcher.dispose();
        meshBatcher.dispose();
    }

    @Override
    public void onUpdate() {
        raceState.update();
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
}
