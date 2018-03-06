package uk.aidanlee.dsp.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import glm_.vec2.Vec2;
import imgui.Cond;
import imgui.ImGui;
import imgui.WindowFlags;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.net.commands.CmdChatMessage;
import uk.aidanlee.dsp.common.net.commands.CmdClientUpdated;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.data.Game;
import uk.aidanlee.dsp.net.Client;

import java.util.List;

public class LobbyState extends State {
    private char[] inputBox;

    private int[] ourShipIndex;

    public LobbyState(String _name) {
        super(_name);

        inputBox = new char[255];
    }

    @Override
    public void onEnter(Object _enterWith) {
        ourShipIndex = new int[] { Game.connections.getUs().getShipIndex() };
    }

    @Override
    public void onUpdate() {
        // Send a netchan update out.
        Packet packet = Game.netChan.send();
        if (packet != null) {
            Game.netManager.send(packet);
        }

        drawClientList();
        drawChatBox();
        drawPlayerSettings();
    }

    @Override
    public void onRender() {
        Gdx.gl.glClearColor(0.47f, 0.56f, 0.61f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Nothing is explicitly drawn since everything in this state is part of ImGui and drawn by that instead.
    }

    private void drawClientList() {
        ImGui.INSTANCE.setNextWindowPos(new Vec2(40, 40), Cond.Always, new Vec2());
        ImGui.INSTANCE.setNextWindowSize(new Vec2(440, 420), Cond.Always);
        ImGui.INSTANCE.begin("Clients Connected", null, WindowFlags.NoResize.getI() | WindowFlags.NoCollapse.getI());

        // Draws players names or "free slot" for all the clients.
        // Client ID is pushed as an ID for ImGui in-case two clients have the same name.
        Client[] clients = Game.connections.getClients();
        for (int i = 0; i < clients.length; i++) {
            ImGui.INSTANCE.pushId(i);
            ImGui.INSTANCE.text(clients[i] == null ? "free slot" : clients[i].getName());
            ImGui.INSTANCE.popId();
        }

        ImGui.INSTANCE.end();
    }

    private void drawChatBox() {
        ImGui.INSTANCE.setNextWindowPos(new Vec2(40, 480), Cond.Always, new Vec2());
        ImGui.INSTANCE.setNextWindowSize(new Vec2(440, 200), Cond.Always);
        ImGui.INSTANCE.begin("Chat", null, WindowFlags.NoResize.getI() | WindowFlags.NoCollapse.getI());

        // Draw the char backlog
        ImGui.INSTANCE.beginChild("Text Log", new Vec2(-1, 140), true, 0);
        List<String> log = Game.chatlog.getLog();
        for (int i = 0; i < log.size(); i++) {
            ImGui.INSTANCE.pushId(i);
            ImGui.INSTANCE.textWrapped(log.get(i));
            ImGui.INSTANCE.popId();
        }
        ImGui.INSTANCE.setScrollHere(0.5f);
        ImGui.INSTANCE.endChild();

        // Create a text input box and send button.
        ImGui.INSTANCE.inputText("", inputBox, 0);
        ImGui.INSTANCE.sameLine(0);
        if (ImGui.INSTANCE.button("send", new Vec2(-1, 0))) {
            // Send chat message to server.
            String str = new String(inputBox).trim();

            Game.netChan.addReliableCommand(new CmdChatMessage(Game.connections.getUs().getId(), str));
            Game.chatlog.addPlayerMessage(Game.connections.getUs().getName(), str);

            // reset the input box.
            inputBox  = new char[255];
        }

        ImGui.INSTANCE.end();
    }

    private void drawPlayerSettings() {
        ImGui.INSTANCE.setNextWindowPos(new Vec2(520, 40), Cond.Always, new Vec2());
        ImGui.INSTANCE.setNextWindowSize(new Vec2(480, 280), Cond.Always);
        ImGui.INSTANCE.begin("Ship Settings", null, WindowFlags.NoResize.getI() | WindowFlags.NoCollapse.getI());

        boolean changed;
        changed  = ImGui.INSTANCE.sliderInt("ship", ourShipIndex, 0, 7, "%.0f");
        changed |= ImGui.INSTANCE.colorEdit3("ship color" , Game.connections.getUs().getShipColor() , 0);
        changed |= ImGui.INSTANCE.colorEdit3("trail color", Game.connections.getUs().getTrailColor(), 0);

        if (changed) {
            // TODO : Send out reliable settings changed packets.
            Game.connections.getUs().setShipIndex(ourShipIndex[0]);
            Game.netChan.addReliableCommand(new CmdClientUpdated(
                    Game.connections.getUs().getId(),
                    ourShipIndex[0],
                    Game.connections.getUs().getShipColor(),
                    Game.connections.getUs().getTrailColor()));
        }

        ImGui.INSTANCE.end();
    }
}
