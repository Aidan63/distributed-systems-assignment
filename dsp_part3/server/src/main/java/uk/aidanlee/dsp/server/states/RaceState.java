package uk.aidanlee.dsp.server.states;

import uk.aidanlee.dsp.common.components.InputComponent;
import uk.aidanlee.dsp.common.data.Times;
import uk.aidanlee.dsp.common.data.circuit.Circuit;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.net.commands.CmdClientDisconnected;
import uk.aidanlee.dsp.common.net.commands.CmdClientInput;
import uk.aidanlee.dsp.common.net.commands.Command;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.common.structural.StateMachine;
import uk.aidanlee.dsp.common.structural.ec.Entity;
import uk.aidanlee.dsp.server.data.Craft;

import java.util.LinkedList;

public class RaceState extends State {

    /**
     * The Array which stores all game information on each conected client.
     */
    private Player[] players;

    /**
     * Holds data about the race circuit.
     */
    private Circuit circuit;

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
     * @param _players The players object to modify.
     */
    public RaceState(String _name, Player[] _players) {
        super(_name);

        players = _players;
    }

    @Override
    public void onEnter(Object _enterWith) {
        circuit = new Circuit("/media/aidan/BFE6-24C6/dsp/dsp_part2/assets/tracks/track.p2");
        craft   = new Craft(players, circuit.getSpawn());
        times   = new Times(craft.getRemotePlayers(), 3);
        states  = new StateMachine()
                .add(new RaceStateCountdown("countdown"))
                .add(new RaceStateGame("game", circuit, craft, times))
                .add(new RaceStateResults("results"));

        states.set("countdown", null, null);
    }

    @Override
    public void onUpdate(LinkedList<Command> _cmds) {

        // Process any commands which have came in.
        processCmds(_cmds);

        // Update the sub state.
        states.update();

        // Update the entities positions in the players array
        updatePlayerData();

        // Check if the game actually has clients connected.
        checkIfEmpty();
    }

    /**
     * Processes commands which have come into the game state.
     * In this state we are only interested in the client input command.
     * @param _cmds List of commands to process.
     */
    private void processCmds(LinkedList<Command> _cmds) {
        while (_cmds.size() > 0) {
            Command cmd = _cmds.removeFirst();
            switch (cmd.id) {
                case Command.CLIENT_INPUT:
                    cmdClientInput((CmdClientInput) cmd);
                    break;

                case Command.CLIENT_DISCONNECTED:
                    cmdClientDisconnected((CmdClientDisconnected) cmd);
                    break;
            }
        }
    }

    /**
     * Client Input Command. Contains all of the input keys for a client and if they are pressed.
     * Copy the values into the right entity component so it can be simulated.
     * @param _cmd Input Command.
     */
    private void cmdClientInput(CmdClientInput _cmd) {
        if (!craft.getPlayerEntity(_cmd.clientID).has("input")) return;

        InputComponent ip = (InputComponent) craft.getPlayerEntity(_cmd.clientID).get("input");
        ip.accelerate = _cmd.accel;
        ip.decelerate = _cmd.decel;
        ip.steerLeft  = _cmd.steerLeft;
        ip.steerRight = _cmd.steerRight;
        ip.airBrakeLeft  = _cmd.abLeft;
        ip.airBrakeRight = _cmd.abRight;
    }

    /**
     * Received when a client has disconnected. Remove the entity from the simulation and times class.
     * @param _cmd Disconnected command.
     */
    private void cmdClientDisconnected(CmdClientDisconnected _cmd) {
        times.playerDisconnected(craft.getRemotePlayers()[_cmd.clientID].getName());

        craft.getRemotePlayers()[_cmd.clientID].destroy();
        craft.getRemotePlayers()[_cmd.clientID] = null;
    }

    /**
     * Sets the player structure position and rotation info to that of the simulated entities.
     */
    private void updatePlayerData() {
        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) continue;

            Entity e = craft.getPlayerEntity(i);
            players[i].setX(e.pos.x);
            players[i].setY(e.pos.y);
            players[i].setRotation(e.rotation);
        }
    }

    /**
     * Checks if the game is empty. If it is return to the lobby so clients can join again.
     */
    private void checkIfEmpty() {
        int playerCount = 0;
        for (Player player : players) {
            if (player != null) playerCount++;
        }

        if (playerCount == 0) {
            changeState("lobby-active", null, null);
        }
    }
}
