package uk.aidanlee.dsp.server.states;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import uk.aidanlee.dsp.common.components.InputComponent;
import uk.aidanlee.dsp.common.data.ServerEvent;
import uk.aidanlee.dsp.common.data.Times;
import uk.aidanlee.dsp.common.data.circuit.Circuit;
import uk.aidanlee.dsp.common.data.events.EvLapTime;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.common.structural.StateMachine;
import uk.aidanlee.dsp.common.structural.ec.Entity;
import uk.aidanlee.dsp.server.data.Craft;
import uk.aidanlee.dsp.server.data.events.*;

import java.util.Map;

public class RaceState extends State {

    /**
     * Access to the game event bus.
     */
    private EventBus events;

    /**
     * The Array which stores all game information on each conected client.
     */
    private Map<Integer, Player> players;

    /**
     * Stores the entities used in the simulation.
     */
    private Craft craft;

    /**
     * Records the lap times for all of the players in the server.
     */
    private Times times;

    /**
     * State machine of the races sub-states.
     */
    private StateMachine states;

    /**
     * Creates a new race state to be added to a machine.
     * @param _name    The name of this race state.
     * @param _events  //
     * @param _players The players object to modify.
     */
    public RaceState(String _name, EventBus _events, Map<Integer, Player> _players) {
        super(_name);

        events  = _events;
        players = _players;
    }

    @Override
    public void onEnter(Object _enterWith) {

        events.register(this);

        Circuit circuit = new Circuit("/track.p2");
        craft  = new Craft(players, circuit);
        times  = new Times(craft.getRemotePlayers().values(), 3);
        states = new StateMachine()
                .add(new RaceStateCountdown("countdown", events))
                .add(new RaceStateGame("game", circuit, events, craft, times))
                .add(new RaceStateResults("results", events));

        states.set("countdown", null, null);

        // Register ourselves for events from the entities.
        for (Entity e : craft.getRemotePlayers().values()) {
            e.getEvents().register(this);
        }
    }

    @Override
    public void onLeave(Object _leaveWith) {
        events.unregister(this);

        // Unregister ourselves for events from the entities.
        for (Entity e : craft.getRemotePlayers().values()) {
            e.getEvents().unregister(this);
        }
    }

    @Override
    public void onUpdate() {

        // Update the sub state.
        states.update();

        // Update the entities positions in the players array
        updatePlayerData();

        // Check if the game actually has clients connected.
        checkIfEmpty();
    }

    // Event Functions

    /**
     * Client Input Command. Contains all of the input keys for a client and if they are pressed.
     * Copy the values into the right entity component so it can be simulated.
     * @param //
     */
    @Subscribe
    public void onClientInput(EvClientInput _event) {
        Entity entity = craft.getRemotePlayers().get(_event.cmd.clientID);
        if (!entity.has("input")) return;

        InputComponent ip = (InputComponent) entity.get("input");
        ip.accelerate    = _event.cmd.accel;
        ip.decelerate    = _event.cmd.decel;
        ip.steerLeft     = _event.cmd.steerLeft;
        ip.steerRight    = _event.cmd.steerRight;
        ip.airBrakeLeft  = _event.cmd.abLeft;
        ip.airBrakeRight = _event.cmd.abRight;
    }

    /**
     * Received when a client has disconnected. Remove the entity from the simulation and times class.
     * @param //
     */
    @Subscribe
    public void onClientDisconnected(EvClientDisconnected _event) {
        Entity entity = craft.getRemotePlayers().remove(_event.clientID);
        times.playerDisconnected(entity.getName());

        entity.getEvents().unregister(this);
        entity.destroy();
    }

    /**
     *
     * @param _event
     */
    @Subscribe
    public void onLapTime(EvLapTime _event) {
        times.addTime(_event.name, _event.time);
    }

    @Subscribe
    public void onGameEvent(EvGameEvent _event) {
        if (_event.event == ServerEvent.EVENT_LOBBY_ENTER) {
            changeState("lobby-active", null, null);
        }
    }

    // Private Functions

    private void updatePlayerData() {
        for (Map.Entry<Integer, Entity> entry : craft.getRemotePlayers().entrySet()) {
            Entity e = craft.getPlayerEntity(entry.getKey());
            players.get(entry.getKey()).setX(e.pos.x);
            players.get(entry.getKey()).setY(e.pos.y);
            players.get(entry.getKey()).setRotation(e.rotation);
        }
    }

    private void checkIfEmpty() {
        if (players.size() == 0) {
            changeState("lobby-active", null, null);
        }
    }
}
