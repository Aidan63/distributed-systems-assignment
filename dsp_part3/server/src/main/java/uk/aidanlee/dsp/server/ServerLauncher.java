package uk.aidanlee.dsp.server;

/** Launches the server application. */
public class ServerLauncher {
    public static void main(String[] args) {
        Server.start(7777, 8);
    }
}