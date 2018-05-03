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

/**
 * Game client class holds the state machine for the entire game and a netmanager for sending and receiving network packets.
 */
public class Client {

    /**
     * Main client event bus. Allows different components of the client to communicate to each other.
     */
    private EventBus events;

    /**
     * Threaded network sender and receiver. Queues all received packets in a thread safe queue.
     */
    private NetManager netManager;

    /**
     * Main game state machine.
     */
    private StateMachine states;

    /**
     * ImGui context for drawing ImGui UIs.
     */
    private Context imguiContext;

    /**
     * Creates a new client.
     */
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

        // Disables ImGui ini saving, prevents crashing on college systems where it might now have write permissions.
        ImGui.INSTANCE.getIo().setIniFilename(null);
    }

    /**
     * Main client loop. Reads any packets from the net manager thread and runs the game state.
     * @param _dt Delta time.
     */
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

    /**
     * Called when the client should draw to the screen.
     * Renders the current state, then draws ImGui data.
     */
    public void onRender() {
        Gdx.gl.glClearColor(0.47f, 0.56f, 0.61f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        states.render();

        ImGui.INSTANCE.render();
        LwjglGL3.INSTANCE.renderDrawData(ImGui.INSTANCE.getDrawData());
    }

    /**
     * Cleans up client resources for exit.
     */
    public void dispose() {
        netManager.interrupt();
        states.unset(null);

        LwjglGL3.INSTANCE.shutdown();
        ContextKt.destroy(imguiContext);
    }

    // Event Functions

    /**
     * When a part of the client want to send data to the server, it posts the EvSendPacket event into the event bus.
     * This is picked up here and sent out the net manager.
     * @param _event Event containing packet to sent.
     */
    @Subscribe
    public void onPacket(EvSendPacket _event) {
        netManager.send(_event.packet);
    }

    // Private Functions

    /**
     * Reads the initial bit of a packet and posts an OOB packet event or NetChan packet event.
     * @param _packet
     */
    private void processPacket(Packet _packet) {
        if (_packet.getData().readBoolean()) {
            events.post(new EvOOBData(_packet));
        } else {
            events.post(new EvNetChanData(_packet));
        }
    }
}
