package uk.aidanlee.dsp.server;

import com.badlogic.gdx.utils.TimeUtils;

/** Launches the server application. */
public class ServerRunner {
    public static void main(String[] args) {

        // Create the server.
        Server server = new Server(7777, 8);

        // Setup variables for server fixed time step.
        final float step = 1.0f / 60.0f;
        final float tick = 1.0f / 20.0f;

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