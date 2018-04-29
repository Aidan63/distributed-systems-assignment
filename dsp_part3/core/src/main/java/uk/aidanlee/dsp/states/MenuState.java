package uk.aidanlee.dsp.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import glm_.vec2.Vec2;
import imgui.Cond;
import imgui.ImGui;
import imgui.SelectableFlags;
import imgui.WindowFlags;
import uk.aidanlee.dsp.common.net.EndPoint;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.net.ConnectionSettings;
import uk.aidanlee.dsp.net.ServerDiscovery;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Games Main Menu State.
 *
 * Allows the player to enter their name and shows a list of LAN servers which have been found.
 * Also has direct connect functionality to allow the player to connect to a specific remote address.
 */
public class MenuState extends State {

    /**
     * Sends out LAN discovery packets and stores info on found servers.
     */
    private ServerDiscovery discoverer;

    /**
     * Char array for the players name.
     */
    private char[] name;

    /**
     * Char array for the IP address when directly connecting.
     */
    private char[] ip;

    /**
     * Char array for the port when directly connecting.
     */
    private char[] port;

    public MenuState(String _name) {
        super(_name);
    }

    @Override
    public void onEnter(Object _enterWith) {
        discoverer = new ServerDiscovery();
        name       = new char[255];

        ip   = new char[255];
        port = new char[255];

        // Insert a default name for the player.
        char[] defaultName = "player".toCharArray();
        System.arraycopy(defaultName, 0, name, 0, defaultName.length);
    }

    @Override
    public void onLeave(Object _leaveWith) {
        discoverer.destroy();
    }

    @Override
    public void onUpdate() {

        // Keep track of LAN servers.
        discoverer.update();

        // Draw the ImGui menu.
        drawMenu();
    }

    @Override
    public void onRender() {
        Gdx.gl.glClearColor(0.47f, 0.56f, 0.61f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Nothing is explicitly drawn since everything in this state is part of ImGui and drawn by that instead.
    }

    /**
     * Creates the ImGui main menu.
     */
    private void drawMenu() {
        ImGui.INSTANCE.setNextWindowPos(new Vec2(32, 32), Cond.Once, new Vec2());
        ImGui.INSTANCE.setNextWindowSize(new Vec2(600, 300), Cond.Once);
        ImGui.INSTANCE.begin("Main Menu", null, WindowFlags.NoCollapse.getI());

        // Draw the player name input box.
        ImGui.INSTANCE.inputText("Name", name, 0);

        // Draw the LAN servers.
        float height = ImGui.INSTANCE.getContentRegionAvail().y;

        ImGui.INSTANCE.text("LAN Servers");
        ImGui.INSTANCE.beginChild("LAN Servers", new Vec2(-1, height - 40), true, 0);

        // Iterate over all of the found servers and create a clickable list of all of them.
        // when one is double clicked attempt to join that server.
        for (ServerDiscovery.ServerDetails details : discoverer.getLanServers()) {

            float width = ImGui.INSTANCE.getContentRegionAvail().x;

            boolean selected = ImGui.INSTANCE.selectable(
                    details.getConnected() + " / " + details.getMaxConnections(),
                    false,
                    SelectableFlags.AllowDoubleClick.getI(),
                    new Vec2(0, 0)
            );

            ImGui.INSTANCE.sameLine(75);
            ImGui.INSTANCE.text(details.getName());
            ImGui.INSTANCE.sameLine((int) (width - 150));
            ImGui.INSTANCE.text(details.getIp().getCanonicalHostName() + ":" + details.getPort());

            if (selected) {
                if (ImGui.INSTANCE.isMouseClicked(0, true)) {
                    changeState("connecting", new ConnectionSettings(
                            new String(name).trim(),
                            new EndPoint(details.getIp(), details.getPort())), null);
                }
            }
        }

        ImGui.INSTANCE.endChild();

        if (ImGui.INSTANCE.button("Direct Connect", new Vec2(-1, 0))) {
            ImGui.INSTANCE.openPopup("modal_connect");
        }

        drawDirectConnect();

        ImGui.INSTANCE.end();
    }

    /**
     * Draw the direct connect popup if its active.
     */
    private void drawDirectConnect() {
        ImGui.INSTANCE.setNextWindowPos(new Vec2((Gdx.graphics.getWidth() / 2) - 200, (Gdx.graphics.getHeight() / 2) - 80), Cond.Always, new Vec2());
        ImGui.INSTANCE.setNextWindowSize(new Vec2(400, 160), Cond.Always);
        if (ImGui.INSTANCE.beginPopupModal("modal_connect", new boolean[] { true }, WindowFlags.NoResize.getI())) {

            ImGui.INSTANCE.text("Direct Connect");
            ImGui.INSTANCE.inputText("IP Address", ip  , 0);
            ImGui.INSTANCE.inputText("Port"      , port, 0);

            // Change to the connection state with the entered information.
            if (ImGui.INSTANCE.button("Connect", new Vec2(-1, -1))) {
                try {
                    // Parse the IP and Port address and attempt to connect.
                    String sIP   = new String(ip).trim();
                    int    sPort = Integer.parseInt(new String(port).trim());

                    changeState("connecting", new ConnectionSettings(
                            new String(name).trim(),
                            new EndPoint(InetAddress.getByName(sIP), sPort)), null);
                }  catch (UnknownHostException _ex) {
                    System.out.println("Unable to resolve address : " + new String(ip).trim());
                }
            }
            ImGui.INSTANCE.endPopup();
        }
    }

}
