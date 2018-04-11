package uk.aidanlee.dsp;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.utils.TimeUtils;
import imgui.Context;
import imgui.ContextKt;
import imgui.ImGui;
import imgui.impl.LwjglGL3;
import jdk.nashorn.internal.ir.annotations.Ignore;
import uk.aidanlee.dsp.common.net.NetManager;
import uk.aidanlee.dsp.common.structural.StateMachine;
import uk.aidanlee.dsp.data.Resources;
import uk.aidanlee.dsp.states.ConnectingState;
import uk.aidanlee.dsp.states.GameState;
import uk.aidanlee.dsp.states.MenuState;
import uk.aidanlee.dsp.utils.ImGuiInputProcessor;
import uno.glfw.GlfwWindow;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Client extends ApplicationAdapter {
    private double accumulator;
    private double currentTime;
    private static final float step = 1.0f / 60.0f;

    private Context imguiContext;

    /**
     * Globally accessible resources used by the game.
     */
    public static Resources resources;

    /**
     * Globally accessible network manager to send and receive packets.
     */
    public static NetManager netManager;

    /**
     * Global state machine for the game.
     */
    public static StateMachine clientState;

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

        // Set up the game stuff.
        resources = new Resources();

        netManager = new NetManager();
        netManager.start();

        clientState = new StateMachine();
        clientState.add(new MenuState("menu"));
        clientState.add(new ConnectingState("connecting"));
        clientState.add(new GameState("game"));
        clientState.set("menu", null, null);
    }

    /**
     * LibGDX game loop, called as fast as possible.
     */
    @Override
    public void render() {

        // Client game fixed time-step loop.
        double newTime   = TimeUtils.millis() / 1000.0;
        double frameTime = Math.min(newTime - currentTime, 0.25);

        currentTime = newTime;
        accumulator += frameTime;

        while (accumulator >= step) {
            accumulator -= step;

            LwjglGL3.INSTANCE.newFrame();
            clientState.update();
        }

        clientState.render();

        ImGui.INSTANCE.render();
        LwjglGL3.INSTANCE.renderDrawData(ImGui.INSTANCE.getDrawData());
    }

    /**
     * Called when LibGDX closes.
     */
    @Override
    public void dispose() {
        clientState.unset(null);
        resources.dispose();

        // Clean up imgui implementation.
        LwjglGL3.INSTANCE.shutdown();
        ContextKt.destroy(imguiContext);
    }
}