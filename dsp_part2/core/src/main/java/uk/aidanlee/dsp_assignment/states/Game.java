package uk.aidanlee.dsp_assignment.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import glm_.vec2.Vec2;
import imgui.Cond;
import imgui.ImGui;
import imgui.WindowFlags;
import uk.aidanlee.dsp_assignment.components.AABBComponent;
import uk.aidanlee.dsp_assignment.components.PolygonComponent;
import uk.aidanlee.dsp_assignment.components.ShadowComponent;
import uk.aidanlee.dsp_assignment.components.TrailComponent;
import uk.aidanlee.dsp_assignment.geometry.MeshBatch;
import uk.aidanlee.dsp_assignment.structural.ec.Entity;
import uk.aidanlee.dsp_assignment.structural.ec.Visual;
import uk.aidanlee.dsp_assignment.race.Race;
import uk.aidanlee.dsp_assignment.race.RaceSettings;
import uk.aidanlee.dsp_assignment.structural.State;
import uk.aidanlee.dsp_assignment.utils.Debug;
import uk.aidanlee.jDiffer.math.Vector;

public class Game extends State {

    /**
     * Batcher used to draw all of the players and their shadows to the screen.
     */
    private SpriteBatch spriteBatcher;

    /**
     *
     */
    private MeshBatch meshBatcher;

    /**
     * LibGDX shape drawer for showing debug information such as collision masks.
     */
    private ShapeRenderer shape;

    /**
     * GL Profiler to get GPU related drawing info.
     */
    private GLProfiler prof;
    private int prevTextureBindings;
    private int prevShaderSwitches;
    private int prevDrawCalls;

    public Game(String _name) {
        super(_name);

        prof = new GLProfiler(Gdx.graphics);
        prof.enable();
    }

    @Override
    public void onEnter(Object _enterWith) {
        Race.init((RaceSettings)_enterWith);
        Race.circuit.load();
        Race.views.setup();
        Race.craft.createCraft();

        spriteBatcher = new SpriteBatch();
        meshBatcher   = new MeshBatch(Race.resources.trackTexture);
        meshBatcher.addShader("track", new ShaderProgram(Gdx.files.internal("assets/shaders/track.vert"), Gdx.files.internal("assets/shaders/track.frag")));
        meshBatcher.addShader("trail", new ShaderProgram(Gdx.files.internal("assets/shaders/trail.vert"), Gdx.files.internal("assets/shaders/trail.frag")));
        shape = new ShapeRenderer();

        prevTextureBindings = 0;
        prevShaderSwitches = 0;
        prevDrawCalls = 0;
    }

    @Override
    public void onLeave(Object _leaveWith) {
        super.onLeave(_leaveWith);

        Race.dispose();

        spriteBatcher.dispose();
        meshBatcher.dispose();
        shape.dispose();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        // Resize the views in case the window size has changed and update all player entities.
        Race.views.resize();

        // Update entities.
        for (Entity e : Race.craft.getLocalPlayers()) {
            e.update(0);
        }
        for (Entity e : Race.circuit.getBoostPads()) {
            e.update(0);
        }

        // Check for debug toggle key
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            Debug.enabled = !Debug.enabled;
        }

        // Draw collision debug rendering window
        if (Debug.enabled) {
            ImGui.INSTANCE.setNextWindowSize(new Vec2(150, 150), Cond.Always);
            ImGui.INSTANCE.begin("Debug Rendering", null, WindowFlags.NoResize.getI());
            ImGui.INSTANCE.checkbox("Quadtree", Debug._drawQuadtree);
            ImGui.INSTANCE.checkbox("AABBs", Debug._drawAABBs);
            ImGui.INSTANCE.checkbox("Polygons", Debug._drawPolys);
            ImGui.INSTANCE.end();

            // Draw GL performance window
            ImGui.INSTANCE.setNextWindowSize(new Vec2(200, 100), Cond.Always);
            ImGui.INSTANCE.begin("GL Profile", null, WindowFlags.NoResize.getI());
            ImGui.INSTANCE.text("Texture bindings : " + (prof.getTextureBindings() - prevTextureBindings));
            ImGui.INSTANCE.text("Shader switches  : " + (prof.getShaderSwitches() - prevShaderSwitches));
            ImGui.INSTANCE.text("Draw calls       : " + (prof.getDrawCalls() - prevDrawCalls));
            ImGui.INSTANCE.end();
        }

        prevTextureBindings = prof.getTextureBindings();
        prevShaderSwitches  = prof.getShaderSwitches();
        prevDrawCalls = prof.getDrawCalls();
    }

    @Override
    public void onRender() {
        super.onRender();

        // Clear the screen.
        Gdx.gl.glClearColor(0.47f, 0.56f, 0.61f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // For each local player draw what they would see.
        for (int i = 0; i < Race.settings.getLocalPlayers(); i++) {

            // Setup the viewport and get the camera projection matrix
            Race.views.getViewports()[i].apply();
            Matrix4 cameraProjection = Race.views.getCameras()[i].combined;

            // Draw the track.
            meshBatcher.begin();
            meshBatcher.setShader("track", cameraProjection);
            meshBatcher.draw(Race.circuit.getMesh().getMesh(), GL20.GL_TRIANGLES);
            meshBatcher.end();

            // Draw player shadows
            spriteBatcher.setProjectionMatrix(cameraProjection);
            spriteBatcher.begin();
            for (Visual craft : Race.craft.getLocalPlayers()) {
                ((ShadowComponent)craft.get("shadow")).draw(spriteBatcher);
            }
            spriteBatcher.end();

            // Draw the trails.
            meshBatcher.begin();
            meshBatcher.setShader("trail", cameraProjection);
            for (Entity craft : Race.craft.getLocalPlayers()) {
                TrailComponent trail = (TrailComponent) craft.get("trail");
                meshBatcher.draw(trail.mesh, GL20.GL_TRIANGLE_STRIP);
            }
            meshBatcher.end();

            // Draw all of the player craft.
            spriteBatcher.setProjectionMatrix(cameraProjection);
            spriteBatcher.begin();
            for (Visual craft : Race.craft.getLocalPlayers()) {
                craft.draw(spriteBatcher);
            }
            spriteBatcher.end();

            // DEBUG: Draw quad tree and polygon data.
            drawDebug(cameraProjection);
        }
    }

    private void drawDebug(Matrix4 _projection) {
        // Apply the camera matrix.
        shape.setProjectionMatrix(_projection);
        shape.begin(ShapeRenderer.ShapeType.Line);

        // Draw the Quadtree AABBs
        Race.circuit.getWallTree().debugDraw(shape);

        // Draw Quad bounds
        if (Debug.drawPolys()) {
            Race.circuit.getMesh().debugRender(shape);
        }

        // Draw the AABB and collision polygon of all the local players.
        for (Entity craft : Race.craft.getLocalPlayers()) {
            AABBComponent    aabb    = (AABBComponent)    craft.get("aabb");
            PolygonComponent polygon = (PolygonComponent) craft.get("polygon");

            if (Debug.drawAABBs()) {
                Rectangle box = aabb.getBox();
                shape.setColor(Color.GREEN);
                shape.rect(box.x, box.y, box.width, box.height);
            }

            if (Debug.drawPolys()) {
                shape.setColor(Color.BLUE);

                Vector[] transformed = polygon.getShape().get_transformedVertices();
                float[] verts = new float[transformed.length * 2];
                int index = 0;

                for (Vector v : transformed) {
                    verts[index++] = (float)v.x;
                    verts[index++] = (float)v.y;
                }

                shape.polygon(verts);
            }
        }

        shape.end();
    }
}
