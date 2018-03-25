package uk.aidanlee.dsp.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import glm_.vec2.Vec2;
import imgui.Cond;
import imgui.ImGui;
import imgui.SelectableFlags;
import imgui.WindowFlags;
import uk.aidanlee.dsp.common.net.EndPoint;
import uk.aidanlee.dsp.common.net.NetManager;
import uk.aidanlee.dsp.common.net.commands.Command;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.net.ConnectionSettings;
import uk.aidanlee.dsp.net.ServerDiscovery;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

public class MenuState extends State {
    private char[] ip;
    private char[] port;
    private char[] name;
    private ServerDiscovery discoverer;
    private int selected;

    public MenuState(String _name) {
        super(_name);
    }

    @Override
    public void onEnter(Object _enterWith) {
        ip   = new char[255];
        port = new char[255];
        name = new char[255];
        discoverer = new ServerDiscovery();
        selected   = -1;
    }

    @Override
    public void onLeave(Object _leaveWith) {
        discoverer.destroy();
    }

    @Override
    public void onUpdate(LinkedList<Command> _cmds) {

        // Keep track of LAN servers.
        discoverer.update();

        // Build the UI.
        buildDirectConnect();
        buildLANServers();
    }

    @Override
    public void onRender() {
        Gdx.gl.glClearColor(0.47f, 0.56f, 0.61f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Nothing is explicitly drawn since everything in this state is part of ImGui and drawn by that instead.
    }

    /**
     * Builds the direct IP connect UI.
     */
    private void buildDirectConnect() {
        ImGui.INSTANCE.setNextWindowPos(new Vec2(32, 32), Cond.Always, new Vec2());
        ImGui.INSTANCE.setNextWindowSize(new Vec2(400, 123), Cond.Always);
        ImGui.INSTANCE.begin("Connect to Server", null, WindowFlags.NoResize.getI() | WindowFlags.NoCollapse.getI());

        ImGui.INSTANCE.inputText("IP"  , ip, 0);
        ImGui.INSTANCE.inputText("Port", port, 0);
        ImGui.INSTANCE.inputText("Name", name, 0);

        if (ImGui.INSTANCE.button("Connect", new Vec2(-1, 0))) {
            try {
                // Get the servers location.
                String serverIP   = new String(ip).trim();
                int    serverPort = Integer.parseInt(new String(port).trim());

                // then switch to the connecting state to start sending connection packets.
                changeState("connecting", new ConnectionSettings(
                        new String(name).trim(),
                        new EndPoint(InetAddress.getByName(serverIP), serverPort)), null);

            } catch (UnknownHostException _ex) {
                System.out.println("Unable to resolve address : " + new String(ip).trim());
            }
        }

        ImGui.INSTANCE.end();
    }

    /**
     * Builds the UI which will show all discovered LAN servers.
     */
    private void buildLANServers() {
        ImGui.INSTANCE.setNextWindowPos(new Vec2(32, 187), Cond.Always, new Vec2());
        ImGui.INSTANCE.setNextWindowSize(new Vec2(400, 130), Cond.Always);
        ImGui.INSTANCE.begin("LAN Servers", null, WindowFlags.NoResize.getI() | WindowFlags.NoCollapse.getI());

        for (ServerDiscovery.ServerDetails details : discoverer.getLanServers()) {

            boolean selected = ImGui.INSTANCE.selectable(details.getConnected() + " / " + details.getMaxConnections(), false, 0, new Vec2(0, 0));
            ImGui.INSTANCE.sameLine(75);
            ImGui.INSTANCE.text(details.getName());
            ImGui.INSTANCE.sameLine(250);
            ImGui.INSTANCE.text(details.getIp().getCanonicalHostName() + ":" + details.getPort());

            if (selected) {
                System.out.println("TODO : Connect to that server");
            }
        }

        ImGui.INSTANCE.end();
    }
}
