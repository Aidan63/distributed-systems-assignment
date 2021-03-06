package uk.aidanlee.dsp.states.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import imgui.*;
import imgui.internal.Rect;
import uk.aidanlee.dsp.common.data.ServerEvent;
import uk.aidanlee.dsp.common.net.EndPoint;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.net.commands.*;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.data.ChatLog;
import uk.aidanlee.dsp.data.Resources;
import uk.aidanlee.dsp.data.events.EvAddReliableCommand;
import uk.aidanlee.dsp.data.events.EvSendPacket;
import uk.aidanlee.dsp.data.events.EvStateChange;
import uk.aidanlee.dsp.data.states.LobbyData;

import java.util.List;

/**
 * Lobby game sub state. Lobby state allows the player to change their settings. Once all players have clicked ready a
 * three second countdown is started on the server before switching to the race state.
 */
public class LobbyState extends State {

    /**
     * Access to the clients event bus.
     */
    private EventBus events;

    /**
     * Ip address and port of the server.
     */
    private EndPoint server;

    /**
     * Access to all of the clients resources.
     */
    private Resources resources;

    /**
     * Access to the clients chat log.
     */
    private ChatLog chatLog;

    /**
     * Access to the array of all client info.
     */
    private Player[] players;

    /**
     * Our clients ID. Used for quick lookup of our-self in the player array.
     */
    private int ourID;

    /**
     * If the client can edit its settings.
     */
    private boolean canEdit;

    /**
     * Chat input box buffer.
     */
    private char[] inputBox;

    /**
     * Index of the ship image.
     */
    private int[] ourShipIndex;

    /**
     * If this client is ready.
     */
    private boolean isReady;

    public LobbyState(String _name, Resources _resources, EventBus _events, EndPoint _server) {
        super(_name);
        resources = _resources;
        events    = _events;
        server    = _server;
    }

    @Override
    public void onEnter(Object _enterWith) {

        // Read the required data from the game state.
        LobbyData data = (LobbyData) _enterWith;
        chatLog = data.chat;
        players = data.players;
        ourID   = data.ourID;

        canEdit      = true;
        inputBox     = new char[255];
        ourShipIndex = new int[] { players[ourID].getShipIndex() };
        isReady      = false;

        events.register(this);
    }

    @Override
    public void onLeave(Object _leaveWith) {
        events.unregister(this);
    }

    @Override
    public void onUpdate() {
        drawClientList();
        drawChatBox();
        drawPlayerSettings();
    }

    // Event Functions

    @Subscribe
    public void onServerEvent(CmdServerEvent _cmd) {
        switch (_cmd.state) {
            case ServerEvent.EVENT_LOBBY_COUNTDOWN:
                canEdit = false;
                players[ourID].setReady(true);
                break;

            case ServerEvent.EVENT_RACE_ENTER:
                machine.set("race", new LobbyData(chatLog, players, ourID), null);
                break;
        }
    }

    @Subscribe
    public void onSnapshot(CmdSnapshot _cmd) {
        for (int i = 0; i < _cmd.snapshot.getPlayerCount(); i++) {
            Player player = _cmd.snapshot.getPlayer(i);
            int    id     = _cmd.snapshot.getID(i);

            //if (id == ourID) continue;

            players[id].setShipIndex(player.getShipIndex());

            players[id].getShipColor()[0] = player.getShipColor()[0];
            players[id].getShipColor()[1] = player.getShipColor()[1];
            players[id].getShipColor()[2] = player.getShipColor()[2];

            players[id].getTrailColor()[0] = player.getTrailColor()[0];
            players[id].getTrailColor()[1] = player.getTrailColor()[1];
            players[id].getTrailColor()[2] = player.getTrailColor()[2];

            players[id].setReady(player.isReady());

            players[id].setX(player.getX());
            players[id].setY(player.getY());
            players[id].setRotation(player.getRotation());
        }
    }

    // Private Functions

    /**
     * Draws a list of all clients connected to the server.
     */
    private void drawClientList() {
        ImGui.INSTANCE.setNextWindowPos(new Vec2(40, 40), Cond.Always, new Vec2());
        ImGui.INSTANCE.setNextWindowSize(new Vec2(440, 420), Cond.Always);
        ImGui.INSTANCE.begin("Clients Connected", null, WindowFlags.NoResize.getI() | WindowFlags.NoCollapse.getI());

        // Draws players names or "free slot" for all the clients.
        // ClientRunner ID is pushed as an ID for ImGui in-case two clients have the same name.
        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) continue;

            ImGui.INSTANCE.pushId(i);
            drawPlayer(players[i]);
            ImGui.INSTANCE.popId();
        }

        ImGui.INSTANCE.end();
    }

    /**
     * Manages the chat box.
     * Draws the chat back log and checks for the send button pressed.
     * Adds a chat net chan command when clicked.
     */
    private void drawChatBox() {
        // Setup the position, size, and non resize properties of the chat box window
        ImGui.INSTANCE.setNextWindowPos(new Vec2(40, 480), Cond.Always, new Vec2());
        ImGui.INSTANCE.setNextWindowSize(new Vec2(440, 200), Cond.Always);
        ImGui.INSTANCE.begin("Chat", null, WindowFlags.NoResize.getI() | WindowFlags.NoCollapse.getI());

        // Draw the chat backlog and make sure it is always scrolled to the bottom (latest message).
        ImGui.INSTANCE.beginChild("Text Log", new Vec2(-1, 140), true, 0);
        List<String> log = chatLog.getLog();
        for (int i = 0; i < log.size(); i++) {
            ImGui.INSTANCE.pushId(i);
            ImGui.INSTANCE.textWrapped(log.get(i));
            ImGui.INSTANCE.popId();
        }
        ImGui.INSTANCE.setScrollHere(0.5f);
        ImGui.INSTANCE.endChild();

        // Create a text input box.
        boolean reclaimFocus = false;
        ImGui.INSTANCE.inputTextEx("", inputBox, new Vec2(-1, 0), 0);

        if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            // Send chat message to server.
            String str = new String(inputBox).trim();
            if (str.length() != 0) {

                events.post(new EvAddReliableCommand(new CmdChatMessage(ourID, str)));
                chatLog.addPlayerMessage(players[ourID].getName(), str);

                // reset the input box.
                inputBox = new char[255];
                reclaimFocus = true;
            }
        }

        ImGui.INSTANCE.setItemDefaultFocus();
        if (reclaimFocus) {
            ImGui.INSTANCE.setKeyboardFocusHere(-1);
        }

        ImGui.INSTANCE.end();
    }

    /**
     * Draws controls to edit the local players settings.
     * Controls will be disable if the player has clicked "ready" or if the server has started its countdown.
     */
    private void drawPlayerSettings() {
        ImGui.INSTANCE.setNextWindowPos(new Vec2(520, 40), Cond.Always, new Vec2());
        ImGui.INSTANCE.setNextWindowSize(new Vec2(480, 280), Cond.Always);
        ImGui.INSTANCE.begin("Ship Settings", null, WindowFlags.NoResize.getI() | WindowFlags.NoCollapse.getI());

        // If we are ready, push some flags onto the ImGui stack to disable our settings controls
        if (players[ourID].isReady()) {
            ImGui.INSTANCE.pushItemFlag(ItemFlags.Disabled.getI(), true);
            ImGui.INSTANCE.pushStyleVar(StyleVar.Alpha, (float) (ImGui.INSTANCE.getStyle().getAlpha() * 0.5));
        }

        // Vec4 colours for drawing the ship image.
        Vec4 ourCcol = new Vec4(players[ourID].getShipColor() [0], players[ourID].getShipColor() [1], players[ourID].getShipColor() [2], 1);
        Vec4 ourTcol = new Vec4(players[ourID].getTrailColor()[0], players[ourID].getTrailColor()[1], players[ourID].getTrailColor()[2], 1);

        // Draw the currently selected ship tinted to the colour.
        TextureRegion region = resources.craftAtlas.findRegion("craft", players[ourID].getShipIndex());
        ImGui.INSTANCE.image(region.getTexture().getTextureObjectHandle(), new Vec2(128, 64), new Vec2(region.getU(), region.getV()), new Vec2(region.getU2(), region.getV2()), ourCcol, ourTcol);

        // Draw the player settings controls.
        boolean changed;
        changed  = ImGui.INSTANCE.sliderInt("ship", ourShipIndex, 0, 7, "%.0f");
        changed |= ImGui.INSTANCE.colorEdit3("ship color" , players[ourID].getShipColor() , 0);
        changed |= ImGui.INSTANCE.colorEdit3("trail color", players[ourID].getTrailColor(), 0);

        // Remove those disable flags from the ImGui stack so the "ready" and "disconnect" are always enabled.
        if (players[ourID].isReady()) {
            ImGui.INSTANCE.popItemFlag();
            ImGui.INSTANCE.popStyleVar(1);
        }

        // If we can't edit (the server countdown has begun) disable the ready button.
        if (!canEdit) {
            ImGui.INSTANCE.pushItemFlag(ItemFlags.Disabled.getI(), true);
            ImGui.INSTANCE.pushStyleVar(StyleVar.Alpha, (float) (ImGui.INSTANCE.getStyle().getAlpha() * 0.5));
        }

        // Toggle our ready state and send it to the server.
        if (ImGui.INSTANCE.button("Ready", new Vec2(-1, 0))) {
            players[ourID].setReady(!players[ourID].isReady());
            changed = true;
        }

        // Pop the disable effects off the imgui stack.
        if (!canEdit) {
            ImGui.INSTANCE.popItemFlag();
            ImGui.INSTANCE.popStyleVar(1);
        }

        // If any of the controls were modified, send a reliable client settings command
        if (changed) {
            players[ourID].setShipIndex(ourShipIndex[0]);
            events.post(new EvAddReliableCommand(
                    new CmdClientSettings(
                        ourID,
                        players[ourID].getShipIndex(),
                        players[ourID].getShipColor(),
                        players[ourID].getTrailColor(),
                        players[ourID].isReady())
                    )
            );
        }

        // Disconnection button to cleanly disconnect from the server.
        if (ImGui.INSTANCE.button("Disconnect", new Vec2(-1, 0))) {

            // Send 10 disconnect packets, hope one gets through.
            for (int i = 0; i < 10; i++) {
                events.post(new EvSendPacket(Packet.Disconnection(server)));
            }

            // Return to the main menu.
            events.post(new EvStateChange("menu", null, null));
        }

        ImGui.INSTANCE.end();
    }

    /**
     * Custom ImGui widget for the client list.
     * Draws a fancy box with the clients name along with a coloured preview of their chosen ship and their "ready" state.
     * @param _player The client to draw.
     */
    private void drawPlayer(Player _player) {
        // Get the position and size of ImGui.
        Vec2 size = new Vec2(ImGui.INSTANCE.getContentRegionAvailWidth(), 20);
        Vec2 pos  = ImGui.INSTANCE.getCursorScreenPos();

        Style s = ImGui.INSTANCE.getStyle();
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
        Vec4 shipCol = new Vec4(_player.getShipColor() [0], _player.getShipColor() [1], _player.getShipColor() [2], 1);

        // Draw the clients ready state as a small tick box icon
        p1 = new Vec2((total_bb.getMax().x - dim) - 2, (total_bb.getMax().y - dim) - 2);
        p2 = new Vec2(p1.x + dim, p1.y  + dim);
        d.addImage(
                resources.checkbox.getTextureObjectHandle(),
                p1,
                p2,
                new Vec2(0, 0),
                new Vec2(1, 1),
                ImGui.INSTANCE.getColorU32(new Vec4(1, 1, 1, _player.isReady() ? 1 : 0.25))
        );

        // Draw ship preview with the clients colour.
        p1.x -= ((dim * 2) + pad);
        p2.x -= (dim + pad);
        TextureRegion region = resources.craftAtlas.findRegion("craft", _player.getShipIndex());
        d.addImage(
                region.getTexture().getTextureObjectHandle(),
                p1,
                p2,
                new Vec2(region.getU(), region.getV()),
                new Vec2(region.getU2(), region.getV2()),
                ImGui.INSTANCE.getColorU32(shipCol)
        );

        // Draw the player name
        d.addText(total_bb.getMin().plus(new Vec2(4, 4)), ImGui.INSTANCE.getColorU32(s.getColors().get(Col.Text.getI())), _player.getName().toCharArray(), _player.getName().length());
    }
}
