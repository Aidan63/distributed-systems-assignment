package uk.aidanlee.dsp.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import uk.aidanlee.dsp.Client;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.net.ConnectionResponse;
import uk.aidanlee.dsp.net.ConnectionSettings;

public class ConnectingState extends State {
    private ConnectionSettings settings;

    public ConnectingState(String _name) {
        super(_name);
    }

    @Override
    public void onEnter(Object _enterWith) {
        settings = (ConnectionSettings) _enterWith;
    }

    @Override
    public void onUpdate() {
        readPackets();

        // Send a connection request each step if we haven't heard anything back.
        Client.netManager.send(Packet.Connection(settings.getName(), settings.getEp()));
    }

    @Override
    public void onRender() {
        Gdx.gl.glClearColor(0.47f, 0.56f, 0.61f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Nothing is explicitly drawn since everything in this state is part of ImGui and drawn by that instead.
    }

    /**
     * Reads packets from the net manager.
     * In the connecting state we are interested in Connection Response packets.
     */
    private void readPackets() {
        Packet pck = Client.netManager.getPackets().poll();
        while (pck != null) {
            processPacket(pck);
            pck = Client.netManager.getPackets().poll();
        }
    }

    /**
     *
     * @param _packet
     */
    private void processPacket(Packet _packet) {
        // If the packet is not OOB or a connection response we don't care about it.
        if (!_packet.getData().readBoolean()) return;
        if (!(_packet.getData().readByte() == Packet.CONNECTION_RESPONSE)) return;

        if (_packet.getData().readBoolean()) {
            // Connection Accepted
            System.out.println("Connection Accepted");
            changeState("game", new ConnectionResponse(settings.getEp(), _packet), null);
        } else {
            // Connection Denied
            System.out.println("Connection Denied");
            changeState("menu", null, null);
        }
    }
}
