package uk.aidanlee.dsp.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import glm_.vec2.Vec2;
import imgui.Cond;
import imgui.ImGui;
import imgui.WindowFlags;
import org.lwjgl.system.CallbackI;
import uk.aidanlee.dsp.common.net.EndPoint;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.data.Game;
import uk.aidanlee.dsp.net.ConnectionState;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MenuState extends State {
    private char[] ip;
    private char[] port;
    private char[] name;

    public MenuState(String _name) {
        super(_name);
    }

    @Override
    public void onEnter(Object _enterWith) {
        ip   = new char[255];
        port = new char[255];
        name = new char[255];

        Game.connections.setState(ConnectionState.Disconnected);
    }

    @Override
    public void onUpdate() {
        ImGui.INSTANCE.setNextWindowPos(new Vec2(32, 32), Cond.Always, new Vec2());
        ImGui.INSTANCE.setNextWindowSize(new Vec2(400, 123), Cond.Always);
        ImGui.INSTANCE.begin("Connect to Server", null, WindowFlags.NoResize.getI() | WindowFlags.NoCollapse.getI());

        ImGui.INSTANCE.inputText("IP"  , ip, 0);
        ImGui.INSTANCE.inputText("Port", port, 0);
        ImGui.INSTANCE.inputText("Name", name, 0);
        if (ImGui.INSTANCE.button("Connect", new Vec2(-1, 0))) {
            // Attempt to resolve and set the servers location.
            try {
                String strIP = new String(ip).trim();
                int intPort  = Integer.parseInt(new String(port).trim());

                EndPoint location = new EndPoint(InetAddress.getByName(strIP), intPort);
                Game.netManager.setDestination(location);
            } catch (UnknownHostException _ex) {
                System.out.println("Unable to resolve address : " + new String(ip).trim());
            }

            // then switch to the connecting state to start sending connection packets.
            changeState("connecting", new String(name).trim(), null);
        }

        ImGui.INSTANCE.end();
    }

    @Override
    public void onRender() {
        Gdx.gl.glClearColor(0.47f, 0.56f, 0.61f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Nothing is explicitly drawn since everything in this state is part of ImGui and drawn by that instead.
    }
}
