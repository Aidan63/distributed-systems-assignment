package uk.aidanlee.dsp.server.data;

import com.badlogic.gdx.math.Vector2;
import uk.aidanlee.dsp.common.components.*;
import uk.aidanlee.dsp.common.data.circuit.CircuitSpawn;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.structural.ec.Entity;
import uk.aidanlee.dsp.common.structural.ec.EntityStateMachine;
import uk.aidanlee.dsp.server.Server;
import uk.aidanlee.jDiffer.math.Vector;

public class Craft {
    /**
     * Array of all player ship entities.
     */
    private final Entity[] remotePlayers;

    /**
     *
     * @param _players
     */
    public Craft(Player[] _players, CircuitSpawn _spawns) {
        remotePlayers = new Entity[_players.length];

        createCraft(_players, _spawns);
    }

    /**
     * Returns the entity at the requested index.
     * @param _index Client ID of the entity to get.
     */
    public Entity getPlayerEntity(int _index) {
        return remotePlayers[_index];
    }

    /**
     * Create all of the craft in entities.
     */
    public void createCraft(Player[] _players, CircuitSpawn _spawns) {
        int index = 0;
        for (int i = 0; i < _players.length; i++) {
            if (_players[i] != null) continue;

            // Get the spawn position and tangent for the initial position and rotation
            int spawnIndex = (_spawns.spawns.length - 1) - index;
            Vector2 tangent = _spawns.spawns[spawnIndex].tangent;

            // Create and set the initial position, rotation, and origin
            Entity craft = new Entity("Client " + i);

            craft.origin.x = 32;
            craft.origin.y = 32;
            craft.rotation = (float)(Math.atan2(tangent.y, tangent.x) * 180 / Math.PI);

            craft.pos.x = _spawns.spawns[spawnIndex].position.x - (craft.origin.x);
            craft.pos.y = _spawns.spawns[spawnIndex].position.y - (craft.origin.y);

            // Ordering is Important!
            // The order in which components are added to the entity defines the order in which they are updated.

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
            index++;
        }
    }
}
