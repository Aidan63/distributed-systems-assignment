package uk.aidanlee.dsp.server;

import com.badlogic.gdx.utils.TimeUtils;

/**
 * ServerRunner, runs an infinite loop with two fixed timestep loops, one for network ticks, and another for game simulation steps.
 * Splitting the network send rate for the game simulation allows the send rate to vary based on the network conditions.
 */
public class ServerRunner {
    public static void main(String[] _args) {

        // Reads server CLI args
        String name          = _args[0];
        int    port          = Integer.parseInt(_args[1]);
        int    discoveryPort = Integer.parseInt(_args[2]);
        int    maxClients    = Integer.parseInt(_args[3]);
        float  tickRate      = Float.parseFloat(_args[4]);

        // Create the server.
        Server server = new Server(name, port, discoveryPort, maxClients);

        // Game is stepped at a constant 60 times per second. Game is currently tied to this number.
        // Changing from 60 to another number will speed up or slow down the entire simulation.
        final float step = 1.0f / 60.0f;

        // Tick rate can be freely changed and its value is read from a CLI argument.
        final float tick = 1.0f / tickRate;

        double currentTime = 0;
        double stepAccumulator = 0;
        double tickAccumulator = 0;

        // Server loop.
        while (true) {

            // Server fixed time-step loop
            double newTime   = TimeUtils.millis() / 1000.0;
            double frameTime = Math.min(newTime - currentTime, 0.25);

            currentTime = newTime;
            stepAccumulator += frameTime;
            tickAccumulator += frameTime;

            // Call the loop function each time the server loops.
            server.onLoop();

            // Call step when its time for the server to progress the game simulation.
            while (stepAccumulator >= step) {
                stepAccumulator -= step;
                server.onStep(step);
            }

            // Call tick when its time for the server to send out a netchan message.
            while (tickAccumulator >= tick) {
                tickAccumulator -= tick;
                server.onTick(tick);
            }

        }
    }
}