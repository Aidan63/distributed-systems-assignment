package uk.aidanlee.dsp_assignment;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.utils.TimeUtils;
import imgui.Context;
import imgui.ContextKt;
import imgui.ImGui;
import imgui.impl.LwjglGL3;
import uk.aidanlee.dsp_assignment.states.Game;
import uk.aidanlee.dsp_assignment.states.Menu;
import uk.aidanlee.dsp_assignment.structural.StateMachine;
import uk.aidanlee.dsp_assignment.utils.ImGuiInputProcessor;
import uno.glfw.GlfwWindow;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class dsp_assignment extends ApplicationAdapter
{
    private double accumulator;
    private double currentTime;
    private static final float step = 1.0f / 60.0f;

    private StateMachine gameState;
    private Context imguiContext;

    @Override
    public void create() {
        // Get the window handle to pass to ImGui.
        Lwjgl3Graphics gfx = (Lwjgl3Graphics) Gdx.graphics;
        GlfwWindow imguiWindow = new GlfwWindow(gfx.getWindow().getWindowHandle());

        // Create the ImGui context and start the LWJGL3 backend with the window handle.
        imguiContext = new Context(null);
        LwjglGL3.INSTANCE.init(imguiWindow, false);

        // Setup the LibGDX input handling for ImGui so text entry works.
        Gdx.input.setInputProcessor(new ImGuiInputProcessor());

        // Game state machine setup
        gameState = new StateMachine();
        gameState.add(new Menu("menu"));
        gameState.add(new Game("game"));
        gameState.set("menu", null, null);
    }

    @Override
    public void render() {
        // Fixed time step loop
        // Loop will update the game state at a fixed rate of 60 times per second (0.1666 delta time)
        // but the game will render as fast the computer will allow it.

        double newTime   = TimeUtils.millis() / 1000.0;
        double frameTime = Math.min(newTime - currentTime, 0.25);

        currentTime = newTime;
        accumulator += frameTime;

        while (accumulator >= step)
        {
            accumulator -= step;

            LwjglGL3.INSTANCE.newFrame();
            gameState.update();
        }

        gameState.render();

        // Calculate the ImGui vert list and draw them.
        ImGui.INSTANCE.render();
        LwjglGL3.INSTANCE.renderDrawData(ImGui.INSTANCE.getDrawData());
    }

    @Override
    public void dispose() {
        LwjglGL3.INSTANCE.shutdown();
        ContextKt.destroy(imguiContext);
    }
}
