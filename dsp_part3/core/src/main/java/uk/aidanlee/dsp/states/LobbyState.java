package uk.aidanlee.dsp.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import glm_.vec2.Vec2;
import imgui.Cond;
import imgui.ImGui;
import imgui.WindowFlags;
import uk.aidanlee.dsp.common.net.BitPacker;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.net.commands.CmdChatMessage;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.data.Game;
import uk.aidanlee.dsp.net.Client;

import java.util.List;

public class LobbyState extends State {
    private char[] inputBox;

    public LobbyState(String _name) {
        super(_name);

        inputBox = new char[255];
    }

    @Override
    public void onUpdate() {

        // Send a netchan update out.
        BitPacker data = Game.netChan.send();
        if (data != null) {
            Game.netManager.send(new Packet(data.toBytes(), Game.connections.getServer()));
        }

        drawClientList();

        drawChatBox();
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
            System.out.println("Adding chat message");
            Game.netChan.addReliableCommand(new CmdChatMessage(Game.connections.getUs().getId(), str));

            // Add the message to out chat log
            Game.chatlog.addPlayerMessage(Game.connections.getUs().getName(), str);

            // reset the input box.
            inputBox  = new char[255];
        }

        ImGui.INSTANCE.end();
    }
}
