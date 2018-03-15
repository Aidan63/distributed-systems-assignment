package uk.aidanlee.dsp.data.race;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import uk.aidanlee.dsp.common.components.*;
import uk.aidanlee.dsp.common.structural.ec.EntityStateMachine;
import uk.aidanlee.dsp.common.structural.ec.Visual;
import uk.aidanlee.dsp.components.LocalInputComponent;
import uk.aidanlee.dsp.data.Game;
import uk.aidanlee.jDiffer.math.Vector;

public class Craft {
    /**
     * Array of all player ship entities.
     */
    private Visual[] remotePlayers;

    public Visual[] getRemotePlayers() {
        return remotePlayers;
    }

    public void createCraft() {
        remotePlayers = new Visual[Game.connections.getClients().length];

        for (int i = 0; i < Game.connections.getClients().length; i++) {
            if (Game.connections.getClients()[i] == null) continue;

            // Get the spawn position and tangent for the initial position and rotation
            int spawnIndex = (Game.race.circuit.getSpawn().spawns.length - 1) - i;
            Vector2 tangent = Game.race.circuit.getSpawn().spawns[spawnIndex].tangent;

            // Find the correct atlas region for this player.
            TextureAtlas.AtlasRegion texture = Game.resources.craftAtlas.findRegion("craft", Game.connections.getClients()[i].getShipIndex());

            // Create and set the initial position, rotation, and origin
            Visual craft = new Visual("local player " + i);
            craft.region = texture;
            craft.color  = new Color(
                    Game.connections.getClients()[i].getShipColor()[0],
                    Game.connections.getClients()[i].getShipColor()[1],
                    Game.connections.getClients()[i].getShipColor()[2], 1);

            craft.origin.x = 32;
            craft.origin.y = 32;
            craft.rotation = (float)(Math.atan2(tangent.y, tangent.x) * 180 / Math.PI);

            craft.pos.x = Game.race.circuit.getSpawn().spawns[spawnIndex].position.x - (craft.origin.x);
            craft.pos.y = Game.race.circuit.getSpawn().spawns[spawnIndex].position.y - (craft.origin.y);

            // Ordering is Important!
            // The order in which components are added to the entity defines the order in which they are updated.

            // If the current player entity we are creating is the local player add a local input component.
            if (i == Game.connections.getUs().getId()) {
                craft.add(new LocalInputComponent("local-input"));
            }

            craft.add(new StatsComponent("stats"));
            craft.add(new VelocityComponent("velocity"));
            craft.add(new PhysicsComponent("physics"));
            craft.add(new InputComponent("input"));
            craft.add(new AABBComponent("aabb", 256, 256, true));
            craft.add(new PolygonComponent("polygon", new Vector[] {
                    new Vector(0, -16), new Vector(70, -5), new Vector(70, 5), new Vector(0, 16)
            }));

            // The state of an entity is explicitly defined by the components attached to it.
            // The FSM component will add and remove components from its entity based on which state it is in.

            EntityStateMachine fsm = new EntityStateMachine("fsm");
            fsm.createState("InActive");
            fsm.createState("Active");
            //.add(new InputComponent("input"))
            //.add(new CraftCollisionsComponent("collision_craft"))
            //.add(new CircuitCollisionsComponent("collision_circuit"))
            //.add(new BoostPadCollisionComponent("collision_boost_pad"))
            //.add(new LapTracker("lap_tracker"));

            craft.add(fsm);
            fsm.changeState("Active");

            remotePlayers[i] = craft;
            System.out.println("Created Craft at : " + remotePlayers[i].pos.x + "x" + remotePlayers[i].pos.y);
        }
    }
}
