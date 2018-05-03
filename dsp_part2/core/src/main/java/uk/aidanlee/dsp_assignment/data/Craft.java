package uk.aidanlee.dsp_assignment.data;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import uk.aidanlee.dsp_assignment.components.craft.*;
import uk.aidanlee.dsp_assignment.data.circuit.Circuit;
import uk.aidanlee.dsp_assignment.race.PlayerSetting;
import uk.aidanlee.dsp_assignment.structural.ec.EntityStateMachine;
import uk.aidanlee.dsp_assignment.structural.ec.Visual;
import uk.aidanlee.jDiffer.math.Vector;
import uk.aidanlee.dsp_assignment.components.*;

public class Craft {
    /**
     * All of the local player entities.
     */
    private Visual[] localPlayers;

    /**
     * Creates a new set of craft entities.
     * @param _players   Player data to create the entities from.
     * @param _circuit   The loaded circuit data.
     * @param _views     All the views for local players.
     * @param _resources The resources class to get ship images from.
     */
    public Craft(PlayerSetting[] _players, Circuit _circuit, Views _views, Resources _resources) {
        localPlayers = new Visual[_players.length];

        for (int i = 0; i < localPlayers.length; i++) {
            // Get the spawn position and tangent for the initial position and rotation.
            int spawnIndex = (_circuit.getSpawn().spawns.length - 1) - i;
            Vector2 tangent = _circuit.getSpawn().spawns[spawnIndex].tangent;

            // Find the correct atlas region for this player.
            TextureAtlas.AtlasRegion texture = _resources.craftAtlas.findRegion("craft", _players[i].getCraftIndex());

            // Create and set the initial position, rotation, and origin
            Visual craft = new Visual("local player " + i);
            craft.region = texture;
            craft.color = new Color(_players[i].getCraftColor()[0], _players[i].getCraftColor()[1], _players[i].getCraftColor()[2], 1);

            craft.origin.x = 32;
            craft.origin.y = 32;
            craft.rotation = (float)(Math.atan2(tangent.y, tangent.x) * 180 / Math.PI);

            craft.pos.x = _circuit.getSpawn().spawns[spawnIndex].position.x - (craft.origin.x);
            craft.pos.y = _circuit.getSpawn().spawns[spawnIndex].position.y - (craft.origin.y);
            System.out.println(craft.pos.x + ":" + craft.pos.y);

            // Ordering is Important!
            // The order in which components are added to the entity defines the order in which they are updated.

            craft.add(new StatsComponent("stats"));
            craft.add(new VelocityComponent("velocity"));
            craft.add(new PhysicsComponent("physics"));
            craft.add(new ShadowComponent("shadow"));
            craft.add(new AABBComponent("aabb", 256, 256, true));
            craft.add(new PolygonComponent("polygon", new Vector[] {
                    new Vector(0, -16), new Vector(70, -5), new Vector(70, 5), new Vector(0, 16)
            }));
            craft.add(new CameraComponent("camera", _views.getCameras()[i]));
            craft.add(new TrailComponent("trail", new Color(_players[i].getTrailColor()[0], _players[i].getTrailColor()[1], _players[i].getTrailColor()[2], 1)));

            // The state of an entity is explicitly defined by the components attached to it.
            // The FSM component will add and remove components from its entity based on which state it is in.

            EntityStateMachine fsm = new EntityStateMachine("fsm");
            fsm.createState("InActive");
            fsm.createState("Active")
                    .add(new InputComponent("input"))
                    .add(new LapTracker("lap_tracker", _circuit.getCheckpoints()));

            craft.add(fsm);
            fsm.changeState("InActive");

            localPlayers[i] = craft;
        }
    }

    public Visual[] getLocalPlayers() {
        return localPlayers;
    }

    public void dispose() {
        for (Visual v : localPlayers) {
            v.destroy();
        }
    }
}
