package uk.aidanlee.dsp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.GL20;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import imgui.Context;
import imgui.ContextKt;
import imgui.ImGui;
import imgui.impl.LwjglGL3;
import uk.aidanlee.dsp.common.net.NetManager;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.structural.StateMachine;
import uk.aidanlee.dsp.data.events.*;
import uk.aidanlee.dsp.states.ConnectingState;
import uk.aidanlee.dsp.states.GameState;
import uk.aidanlee.dsp.states.MenuState;
import uk.aidanlee.dsp.utils.ImGuiInputProcessor;
import uno.glfw.GlfwWindow;

public class Client {

    private EventBus events;

    private NetManager netManager;

    private StateMachine states;

    private Context imguiContext;

    public Client() {
        events = new EventBus();
        events.register(this);

        netManager = new NetManager();
        netManager.start();

        states = new StateMachine()
                .add(new MenuState("menu"))
                .add(new ConnectingState("connecting", events))
                .add(new GameState("game", events));
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

        // Read all packets from the net manager thread.
        Packet pck = netManager.getPackets().poll();
        while (pck != null) {
            processPacket(pck);
            pck = netManager.getPackets().poll();
        }

        // Start a new ImGui frame and progress the simulation.
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
        netManager.interrupt();
        states.unset(null);

        LwjglGL3.INSTANCE.shutdown();
        ContextKt.destroy(imguiContext);
    }

    // Event Functions

    @Subscribe
    public void onPacket(EvSendPacket _event) {
        netManager.send(_event.packet);
    }

    // Private Functions

    private void processPacket(Packet _packet) {
        if (_packet.getData().readBoolean()) {
            events.post(new EvOOBData(_packet));
        } else {
            events.post(new EvNetChanData(_packet));
        }
    }
}
