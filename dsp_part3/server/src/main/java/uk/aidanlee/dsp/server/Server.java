package uk.aidanlee.dsp.server;

import com.badlogic.gdx.utils.TimeUtils;
import uk.aidanlee.dsp.common.net.NetManager;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.server.data.Game;
import uk.aidanlee.dsp.server.net.Connections;

public class Server {
    /**
     * Sends and receives UDP packets on a separate thread for the server.
     */
    public static NetManager netManager;

    /**
     * Processes packets received by the net manager and stores info on all connected clients.
     */
    public static Connections connections;

    /**
     * Holds data and processes the game simulation.
     */
    public static Game game;

    /**
     *
     */
    public static void start(int _port, int _maxClients) {
        netManager  = new NetManager(_port);
        connections = new Connections(_maxClients, _port);

        // Start the socket thread for sending and receiving data.
        netManager.start();

        // Setup the game simulation.
        game = new Game(_maxClients);

        // Setup variables for server fixed time step.
        final float step = 1.0f / 60.0f;
        final float tick = 1.0f / 20.0f;

        double currentTime = 0;
        double stepAccumulator = 0;
        double tickAccumulator = 0;

        // Main server loop
        while (true) {
            // Read packets
            Packet pck = netManager.getPackets().poll();
            while (pck != null) {
                connections.processPacket(pck);
                pck = netManager.getPackets().poll();
            }

            // Server fixed time-step loop
            double newTime   = TimeUtils.millis() / 1000.0;
            double frameTime = Math.min(newTime - currentTime, 0.25);

            currentTime = newTime;
            stepAccumulator += frameTime;
            tickAccumulator += frameTime;

            // Simulate game world.
            while (stepAccumulator >= step) {
                stepAccumulator -= step;

                game.update();
            }

            // Send data out to clients.
            while (tickAccumulator >= tick) {
                tickAccumulator -= tick;

                connections.update();

                // TODO: Send a broadcast out about this server.
            }
        }
    }

    public static void shutdown() {
        //
    }
}
