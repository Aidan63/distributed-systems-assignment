package uk.aidanlee.dsp_assignment;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.GL20;
import imgui.Context;
import imgui.ContextKt;
import imgui.ImGui;
import imgui.impl.LwjglGL3;
import uk.aidanlee.dsp_assignment.data.Resources;
import uk.aidanlee.dsp_assignment.states.Game;
import uk.aidanlee.dsp_assignment.states.Menu;
import uk.aidanlee.dsp_assignment.structural.StateMachine;
import uk.aidanlee.dsp_assignment.utils.ImGuiInputProcessor;
import uno.glfw.GlfwWindow;

public class Client {

    /**
     *
     */
    private Resources resources;

    /**
     *
     */
    private StateMachine states;

    /**
     *
     */
    private Context imguiContext;

    public Client() {
        // Load all needed resources.
        resources = new Resources();

        // Game state machine setup
        states = new StateMachine();
        states.add(new Menu("menu", resources));
        states.add(new Game("game", resources));
        states.set("menu", null, null);

        // Setup ImGui

        // Create the ImGui window for our menu UI
        Lwjgl3Graphics gfx = (Lwjgl3Graphics) Gdx.graphics;
        GlfwWindow imguiWindow = new GlfwWindow(gfx.getWindow().getWindowHandle());

        // Create a new imgui context and pass the window to the LWJGL3 imgui backend.
        imguiContext = new Context(null);
        LwjglGL3.INSTANCE.init(imguiWindow, false);

        // Add a new input processor so ImGui is able to read key presses.
        Gdx.input.setInputProcessor(new ImGuiInputProcessor());
    }

    public void onUpdate(float _dt) {
        LwjglGL3.INSTANCE.newFrame();
        states.update();
    }

    public void onRender() {
        Gdx.gl.glClearColor(0.47f, 0.56f, 0.61f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        states.render();

        ImGui.INSTANCE.render();
        LwjglGL3.INSTANCE.renderDrawData(ImGui.INSTANCE.getDrawData());
    }

    public void dispose() {
        LwjglGL3.INSTANCE.shutdown();
        ContextKt.destroy(imguiContext);
    }
}
