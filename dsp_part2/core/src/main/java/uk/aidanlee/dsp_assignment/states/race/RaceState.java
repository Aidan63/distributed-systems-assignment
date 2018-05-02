package uk.aidanlee.dsp_assignment.states.race;

import com.badlogic.gdx.math.Rectangle;
import uk.aidanlee.dsp_assignment.components.AABBComponent;
import uk.aidanlee.dsp_assignment.components.PolygonComponent;
import uk.aidanlee.dsp_assignment.data.Craft;
import uk.aidanlee.dsp_assignment.data.HUD;
import uk.aidanlee.dsp_assignment.data.Times;
import uk.aidanlee.dsp_assignment.data.Views;
import uk.aidanlee.dsp_assignment.data.circuit.Circuit;
import uk.aidanlee.dsp_assignment.data.circuit.TreeTileWall;
import uk.aidanlee.dsp_assignment.structural.State;
import uk.aidanlee.dsp_assignment.structural.ec.Entity;
import uk.aidanlee.dsp_assignment.structural.ec.EntityStateMachine;
import uk.aidanlee.dsp_assignment.structural.ec.Visual;
import uk.aidanlee.jDiffer.Collision;
import uk.aidanlee.jDiffer.data.ShapeCollision;
import uk.aidanlee.jDiffer.shapes.Polygon;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RaceState extends State {

    private Circuit circuit;

    private Craft craft;

    private Views views;

    private HUD[] huds;

    private Times times;

    public RaceState(String _name, Circuit _circuit, Craft _craft, Views _views, Times _times, HUD[] _huds) {
        super(_name);
        circuit = _circuit;
        craft   = _craft;
        views   = _views;
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
