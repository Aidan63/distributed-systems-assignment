package uk.aidanlee.dsp.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.sun.corba.se.spi.activation.IIOP_CLEAR_TEXT;
import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import imgui.*;
import imgui.internal.Rect;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.net.commands.CmdChatMessage;
import uk.aidanlee.dsp.common.net.commands.CmdClientReady;
import uk.aidanlee.dsp.common.net.commands.CmdClientUnready;
import uk.aidanlee.dsp.common.net.commands.CmdClientUpdated;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.data.Game;
import uk.aidanlee.dsp.net.Client;

import java.util.List;

public class LobbyState extends State {

    private char[] inputBox;
    private int[] ourShipIndex;
    private boolean isReady;

    public LobbyState(String _name) {
        super(_name);

        inputBox = new char[255];
        isReady  = false;
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
            if (clients[i] == null) continue;

            ImGui.INSTANCE.pushId(i);
            drawPlayer(clients[i]);
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

            Game.netChan.addCommand(new CmdChatMessage(Game.connections.getUs().getId(), str));
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

        // If we are ready, push some flags onto the ImGui stack to disable our settings controls
        if (Game.connections.getUs().isReady()) {
            ImGui.INSTANCE.pushItemFlag(ItemFlags.Disabled.getI(), true);
            ImGui.INSTANCE.pushStyleVar(StyleVar.Alpha, (float) (ImGui.INSTANCE.getStyle().getAlpha() * 0.5));
        }

        // Vec4 colours for drawing the ship image.
        Vec4 ourCcol = new Vec4(Game.connections.getUs().getShipColor() [0], Game.connections.getUs().getShipColor() [1], Game.connections.getUs().getShipColor() [2], 1);
        Vec4 ourTcol = new Vec4(Game.connections.getUs().getTrailColor()[0], Game.connections.getUs().getTrailColor()[1], Game.connections.getUs().getTrailColor()[2], 1);

        // Draw the currently selected ship tinted to the colour.
        TextureRegion region = Game.resources.craftAtlas.findRegion("craft", Game.connections.getUs().getShipIndex());
        ImGui.INSTANCE.image(region.getTexture().getTextureObjectHandle(), new Vec2(128, 64), new Vec2(region.getU(), region.getV()), new Vec2(region.getU2(), region.getV2()), ourCcol, ourTcol);

        // Draw the player settings controls.
        boolean changed;
        changed  = ImGui.INSTANCE.sliderInt("ship", ourShipIndex, 0, 7, "%.0f");
        changed |= ImGui.INSTANCE.colorEdit3("ship color" , Game.connections.getUs().getShipColor() , 0);
        changed |= ImGui.INSTANCE.colorEdit3("trail color", Game.connections.getUs().getTrailColor(), 0);

        // If any of the controls were modified, send a reliable client update command
        if (changed) {
            Game.connections.getUs().setShipIndex(ourShipIndex[0]);
            Game.netChan.addReliableCommand(new CmdClientUpdated(
                    Game.connections.getUs().getId(),
                    Game.connections.getUs().getShipIndex(),
                    Game.connections.getUs().getShipColor(),
                    Game.connections.getUs().getTrailColor()));
        }

        // Remove those disable flags from the ImGui stack so the "ready" and "disconnect" are always enabled.
        if (Game.connections.getUs().isReady()) {
            ImGui.INSTANCE.popItemFlag();
            ImGui.INSTANCE.popStyleVar(1);
        }

        // Toggle our ready state and send it to the server.
        if (ImGui.INSTANCE.button("Ready", new Vec2(-1, 0))) {
            Game.connections.getUs().setReady(!Game.connections.getUs().isReady());

            // Add reliable ready or unready commands
            if (Game.connections.getUs().isReady()) {
                Game.netChan.addReliableCommand(new CmdClientReady(Game.connections.getUs().getId()));
            } else {
                Game.netChan.addReliableCommand(new CmdClientUnready(Game.connections.getUs().getId()));
            }
        }

        // Disconnection button to cleanly disconnect from the server.
        if (ImGui.INSTANCE.button("Disconnect", new Vec2(-1, 0))) {

            // Send 10 disconnect packets, hope one gets through.
            for (int i = 0; i < 10; i++) {
                Game.netManager.send(Packet.Disconnection(Game.connections.getServer()));
            }

            // Return to the main menu.
            Game.state.set("menu", null, null);
        }

        ImGui.INSTANCE.end();
    }

    /**
     * Custom ImGui widget for the client list.
     * Draws a fancy box with the clients name along with a coloured preview of their chosen ship and their "ready" state.
     * @param _client The client to draw.
     */
    private void drawPlayer(Client _client) {
        // Get the position and size of ImGui.
        Vec2 size = new Vec2(ImGui.INSTANCE.getContentRegionAvailWidth(), 20);
        Vec2 pos  = ImGui.INSTANCE.getCursorScreenPos();

        Style    s = ImGui.INSTANCE.getStyle();
        DrawList d = ImGui.INSTANCE.getWindowDrawList();

        // Create and draw the background frame which will hold the client info.
        Rect total_bb = new Rect(pos, pos.plus(size));
        ImGui.INSTANCE.itemSize(total_bb, s.getFramePadding().y);
        ImGui.INSTANCE.renderFrame(total_bb.getMin(), total_bb.getMax(), ImGui.INSTANCE.getColorU32(s.getColors().get(Col.FrameBg.getI())), true, s.getFrameRounding());

        // Set up size variables
        Vec2 p1, p2;
        float pad = 2;
        float dim = size.y - (2 * pad);

        // get the ship and trail color and put them into a vec4 instance for imgui use.
        Vec4 shipCol = new Vec4(_client.getShipColor() [0], _client.getShipColor() [1], _client.getShipColor() [2], 1);

        // Draw the clients ready state as a small tick box icon
        p1 = new Vec2((total_bb.getMax().x - dim) - 2, (total_bb.getMax().y - dim) - 2);
        p2 = new Vec2(p1.x + dim, p1.y  + dim);
        d.addImage(
                Game.resources.checkbox.getTextureObjectHandle(),
                p1,
                p2,
                new Vec2(0, 0),
                new Vec2(1, 1),
                ImGui.INSTANCE.getColorU32(new Vec4(1, 1, 1, _client.isReady() ? 1 : 0.25))
        );

        // Draw ship preview with the clients colour.
        p1.x -= ((dim * 2) + pad);
        p2.x -= (dim + pad);
        TextureRegion region = Game.resources.craftAtlas.findRegion("craft", _client.getShipIndex());
        d.addImage(
                region.getTexture().getTextureObjectHandle(),
                p1,
                p2,
                new Vec2(region.getU(), region.getV()),
                new Vec2(region.getU2(), region.getV2()),
                ImGui.INSTANCE.getColorU32(shipCol)
        );

        // Draw the player name
        d.addText(total_bb.getMin().plus(new Vec2(4, 4)), ImGui.INSTANCE.getColorU32(s.getColors().get(Col.Text.getI())), _client.getName().toCharArray(), _client.getName().length());
    }
}
