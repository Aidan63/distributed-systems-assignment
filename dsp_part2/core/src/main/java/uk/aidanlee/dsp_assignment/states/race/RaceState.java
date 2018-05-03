package uk.aidanlee.dsp_assignment.states.race;

import uk.aidanlee.dsp_assignment.data.Craft;
import uk.aidanlee.dsp_assignment.data.HUD;
import uk.aidanlee.dsp_assignment.data.Times;
import uk.aidanlee.dsp_assignment.structural.State;
import uk.aidanlee.dsp_assignment.structural.ec.Entity;
import uk.aidanlee.dsp_assignment.structural.ec.EntityStateMachine;

/**
 * Active race state. State is used when clients can freely move around.
 */
public class RaceState extends State {

    /**
     * Access to all of the local player entities.
     */
    private Craft craft;

    /**
     * Access to all of the local player HUDS.
     */
    private HUD[] huds;

    /**
     * Access to the time storage instance.
     */
    private Times times;

    public RaceState(String _name, Craft _craft, Times _times, HUD[] _huds) {
        super(_name);
        craft   = _craft;
        times   = _times;
        huds    = _huds;
    }

    @Override
    public void onEnter(Object _leaveWith) {
        for (int i = 0; i < huds.length; i++) {
            Entity e = craft.getLocalPlayers()[i];
            huds[i].showRace(e);

            if (!e.has("fsm")) continue;
            ((EntityStateMachine) e.get("fsm")).changeState("Active");
        }
    }

    @Override
    public void onUpdate() {
        checkLapStatus();
    }

    /**
     * Check the lap status of all entities. If an entity has completed all of its laps, disable it.
     */
    private void checkLapStatus() {
        for (int i = 0; i < huds.length; i++) {

            Entity entity = craft.getLocalPlayers()[i];
            // If an entity has finished all laps, disable it as its finished the race.
            // Also fires an event off to tell all other players that a player finished.
            if (times.playerFinished(entity.getName())) {
                if (((EntityStateMachine) entity.get("fsm")).getState().equals("Active")) {
                    ((EntityStateMachine) entity.get("fsm")).changeState("InActive");
                    huds[i].showWaiting();
                }
            }
        }

        // If all players have finished fire off all of the players times and transition into the results sub game event.
        if (times.allPlayersFinished()) {
            for (HUD hud : huds) {
                hud.showEmpty();
            }

            machine.set("results", times.getTimes(), null);
        }
    }
}
