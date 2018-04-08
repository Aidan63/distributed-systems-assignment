package uk.aidanlee.dsp.server.data;

import com.badlogic.gdx.math.Vector2;
import uk.aidanlee.dsp.common.components.*;
import uk.aidanlee.dsp.common.components.craft.LapTracker;
import uk.aidanlee.dsp.common.data.circuit.Circuit;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.structural.ec.Entity;
import uk.aidanlee.dsp.common.structural.ec.EntityStateMachine;
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
    public Craft(Player[] _players, Circuit _circuit) {
        remotePlayers = new Entity[_players.length];

        int index = 0;
        for (int i = 0; i < _players.length; i++) {
            if (_players[i] == null) continue;

            // Get the spawn position and tangent for the initial position and rotation
            int spawnIndex = (_circuit.getSpawn().spawns.length - 1) - index;
            Vector2 tangent = _circuit.getSpawn().spawns[spawnIndex].tangent;

            // Create and set the initial position, rotation, and origin
            Entity craft = new Entity("Client " + i);

            craft.origin.x = 32;
            craft.origin.y = 32;
            craft.rotation = (float)(Math.atan2(tangent.y, tangent.x) * 180 / Math.PI);

            craft.pos.x = _circuit.getSpawn().spawns[spawnIndex].position.x - (craft.origin.x);
            craft.pos.y = _circuit.getSpawn().spawns[spawnIndex].position.y - (craft.origin.y);

            // Update the player pos and rotation
            _players[i].setX((int) craft.pos.x);
            _players[i].setY((int) craft.pos.y);
            _players[i].setRotation((int) craft.rotation);

            // Ordering is Important!
            // The order in which components are added to the entity defines the order in which they are updated.

            craft.add(new StatsComponent("stats"));
            craft.add(new VelocityComponent("velocity"));
            craft.add(new PhysicsComponent("physics"));
            craft.add(new AABBComponent("aabb", 256, 256, true));
            craft.add(new PolygonComponent("polygon", new Vector[] {
                    new Vector(0, -16), new Vector(70, -5), new Vector(70, 5), new Vector(0, 16)
            }));

            // The state of an entity is explicitly defined by the components attached to it.
            // The FSM component will add and remove components from its entity based on which state it is in.

            EntityStateMachine fsm = new EntityStateMachine("fsm");
            fsm.createState("InActive");
            fsm.createState("Active")
                    .add(new InputComponent("input"))
                    .add(new LapTracker("lap_tracker", _circuit.getCheckpoints()));

            craft.add(fsm);
            fsm.changeState("InActive");

            remotePlayers[i] = craft;
            index++;
        }
    }

    /**
     * Returns the entity at the requested index.
     * @param _index Client ID of the entity to get.
     */
    public Entity getPlayerEntity(int _index) {
        return remotePlayers[_index];
    }

    /**
     *
     * @return
     */
    public Entity[] getRemotePlayers() {
        return remotePlayers;
    }
}
