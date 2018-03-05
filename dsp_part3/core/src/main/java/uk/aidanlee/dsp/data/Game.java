package uk.aidanlee.dsp.data;

import uk.aidanlee.dsp.common.net.NetChan;
import uk.aidanlee.dsp.common.structural.StateMachine;
import uk.aidanlee.dsp.data.race.Race;
import uk.aidanlee.dsp.net.Connections;
import uk.aidanlee.dsp.common.net.NetManager;
import uk.aidanlee.dsp.states.ConnectingState;
import uk.aidanlee.dsp.states.LobbyState;
import uk.aidanlee.dsp.states.MenuState;

public class Game {
    public static NetManager netManager;
    public static Connections connections;
    public static NetChan netChan;
    public static ChatLog chatlog;
    public static StateMachine state;
    public static Race race;

    public static void init() {
        netManager = new NetManager();
        netManager.start();

        connections = new Connections();
        netChan     = new NetChan(null);
        chatlog     = new ChatLog();

        state = new StateMachine();
        state.add(new MenuState("menu"));
        state.add(new ConnectingState("connecting"));
        state.add(new LobbyState("lobby"));
        state.set("menu", null, null);
    }
}
