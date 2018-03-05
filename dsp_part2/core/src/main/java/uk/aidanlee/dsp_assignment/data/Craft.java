package uk.aidanlee.dsp_assignment.data;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import uk.aidanlee.dsp_assignment.components.craft.*;
import uk.aidanlee.dsp_assignment.race.PlayerSetting;
import uk.aidanlee.dsp_assignment.structural.ec.EntityStateMachine;
import uk.aidanlee.dsp_assignment.structural.ec.Visual;
import uk.aidanlee.jDiffer.math.Vector;
import uk.aidanlee.dsp_assignment.components.*;
import uk.aidanlee.dsp_assignment.race.Race;

public class Craft {
    /**
     * All of the local player entities.
     */
    private Visual[] localPlayers;

    public Visual[] getLocalPlayers() {
        return localPlayers;
    }

    public void createCraft() {
        localPlayers = new Visual[Race.settings.getLocalPlayers()];

        for (int i = 0; i < Race.settings.getLocalPlayers(); i++) {
            // Get the spawn position and tangent for the initial position and rotation.
            int spawnIndex = (Race.circuit.getSpawn().spawns.length - 1) - i;
            Vector2 tangent = Race.circuit.getSpawn().spawns[spawnIndex].tangent;

            // Find the correct atlas region for this player.
            PlayerSetting settings = Race.settings.getPlayers()[i];
            TextureAtlas.AtlasRegion texture = Race.resources.craftAtlas.findRegion("craft", settings.getCraftIndex());

            // Create and set the initial position, rotation, and origin
            Visual craft = new Visual("local player " + i);
            craft.region = texture;
            craft.color = new Color(settings.getCraftColor()[0], settings.getCraftColor()[1], settings.getCraftColor()[2], 1);

            craft.origin.x = 32;
            craft.origin.y = 32;
            craft.rotation = (float)(Math.atan2(tangent.y, tangent.x) * 180 / Math.PI);

            craft.pos.x = Race.circuit.getSpawn().spawns[spawnIndex].position.x - (craft.origin.x);
            craft.pos.y = Race.circuit.getSpawn().spawns[spawnIndex].position.y - (craft.origin.y);

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
            craft.add(new CameraComponent("camera", Race.views.getCameras()[i]));
            craft.add(new TrailComponent("trail", new Color(settings.getTrailColor()[0], settings.getTrailColor()[1], settings.getTrailColor()[2], 1)));

            // The state of an entity is explicitly defined by the components attached to it.
            // The FSM component will add and remove components from its entity based on which state it is in.

            EntityStateMachine fsm = new EntityStateMachine("fsm");
            fsm.createState("InActive");
            fsm.createState("Active")
                    .add(new InputComponent("input"))
                    .add(new CraftCollisionsComponent("collision_craft"))
                    .add(new CircuitCollisionsComponent("collision_circuit"))
                    .add(new BoostPadCollisionComponent("collision_boost_pad"))
                    .add(new LapTracker("lap_tracker"));

            craft.add(fsm);
            fsm.changeState("Active");

            localPlayers[i] = craft;
        }
    }
}
