package uk.aidanlee.dsp;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.utils.TimeUtils;
import imgui.Context;
import imgui.ContextKt;
import imgui.ImGui;
import imgui.impl.LwjglGL3;
import uk.aidanlee.dsp.common.net.BitPacker;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.structural.StateMachine;
import uk.aidanlee.dsp.data.Game;
import uk.aidanlee.dsp.net.ConnectionState;
import uk.aidanlee.dsp.states.MenuState;
import uk.aidanlee.dsp.utils.ImGuiInputProcessor;
import uno.glfw.GlfwWindow;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private double accumulator;
    private double currentTime;
    private static final float step = 1.0f / 60.0f;

    private Context imguiContext;

    @Override
    public void create() {
        Lwjgl3Graphics gfx = (Lwjgl3Graphics) Gdx.graphics;
        GlfwWindow imguiWindow = new GlfwWindow(gfx.getWindow().getWindowHandle());

        imguiContext = new Context(null);
        LwjglGL3.INSTANCE.init(imguiWindow, false);

        Gdx.input.setInputProcessor(new ImGuiInputProcessor());

        Game.init();
    }

    @Override
    public void render() {
        // Read a packet (if any) and process it.
        Packet pck = Game.netManager.getPackets().poll();
        if (pck != null) {
            Game.connections.processPacket(pck);
        }

        // Main game fixed time-step loop.
        double newTime   = TimeUtils.millis() / 1000.0;
        double frameTime = Math.min(newTime - currentTime, 0.25);

        currentTime = newTime;
        accumulator += frameTime;

        while (accumulator >= step) {
            accumulator -= step;

            LwjglGL3.INSTANCE.newFrame();
            Game.state.update();
        }

        Game.state.render();

        ImGui.INSTANCE.render();
        LwjglGL3.INSTANCE.renderDrawData(ImGui.INSTANCE.getDrawData());
    }

    @Override
    public void dispose() {
        if (Game.connections.getState() == ConnectionState.Connected) {
            // Send 10 disconnection packets and hope at-least one gets through
            for (int i = 0; i < 10; i++) {
                // Send a connection packet!
                Game.netManager.send(Packet.Disconnection(Game.connections.getServer()));
            }
        }

        LwjglGL3.INSTANCE.shutdown();
        ContextKt.destroy(imguiContext);
    }
}