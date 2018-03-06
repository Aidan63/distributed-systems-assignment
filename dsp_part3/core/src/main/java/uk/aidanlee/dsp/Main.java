package uk.aidanlee.dsp;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.utils.TimeUtils;
import imgui.Context;
import imgui.ContextKt;
import imgui.ImGui;
import imgui.impl.LwjglGL3;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.data.Game;
import uk.aidanlee.dsp.net.ConnectionState;
import uk.aidanlee.dsp.utils.ImGuiInputProcessor;
import uno.glfw.GlfwWindow;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private double accumulator;
    private double currentTime;
    private static final float step = 1.0f / 60.0f;

    private Context imguiContext;

    /**
     * Ran once LibGDX has started up.
     */
    @Override
    public void create() {
        // Create the ImGui window for our menu UI
        Lwjgl3Graphics gfx = (Lwjgl3Graphics) Gdx.graphics;
        GlfwWindow imguiWindow = new GlfwWindow(gfx.getWindow().getWindowHandle());

        // Create a new imgui context and pass the window to the LWJGL3 imgui backend.
        imguiContext = new Context(null);
        LwjglGL3.INSTANCE.init(imguiWindow, false);

        // Add a new input processor so ImGui is able to read key presses.
        Gdx.input.setInputProcessor(new ImGuiInputProcessor());

        // Setup the games state machine.
        Game.start();
    }

    /**
     * LibGDX game loop, called as fast as possible.
     */
    @Override
    public void render() {

        // Main game fixed time-step loop.
        double newTime   = TimeUtils.millis() / 1000.0;
        double frameTime = Math.min(newTime - currentTime, 0.25);

        currentTime = newTime;
        accumulator += frameTime;

        while (accumulator >= step) {
            accumulator -= step;

            LwjglGL3.INSTANCE.newFrame();
            Game.update();
        }

        Game.render();

        ImGui.INSTANCE.render();
        LwjglGL3.INSTANCE.renderDrawData(ImGui.INSTANCE.getDrawData());
    }

    /**
     * Called when LibGDX closes.
     */
    @Override
    public void dispose() {
        // If we are connected, send 10 disconnection packets and hope at-least one gets through.
        if (Game.connections.getState() == ConnectionState.Connected) {
            for (int i = 0; i < 10; i++) {
                Game.netManager.send(Packet.Disconnection(Game.connections.getServer()));
            }
        }

        // Clean up imgui implementation.
        LwjglGL3.INSTANCE.shutdown();
        ContextKt.destroy(imguiContext);

        // Clean up the game resources.
        Game.stop();
    }
}