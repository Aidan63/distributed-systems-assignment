package uk.aidanlee.dsp.data.race;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import uk.aidanlee.dsp.common.components.*;
import uk.aidanlee.dsp.common.components.craft.LapTracker;
import uk.aidanlee.dsp.common.data.circuit.CircuitSpawn;
import uk.aidanlee.dsp.common.net.Player;
import uk.aidanlee.dsp.common.structural.ec.EntityStateMachine;
import uk.aidanlee.dsp.common.structural.ec.Visual;
import uk.aidanlee.dsp.components.LocalInputComponent;
import uk.aidanlee.dsp.components.ShadowComponent;
import uk.aidanlee.dsp.components.TrailComponent;
import uk.aidanlee.dsp.data.Resources;
import uk.aidanlee.jDiffer.math.Vector;
import uk.aidanlee.jDiffer.shapes.Ray;

public class Craft {
    /**
     * Array of all player ship entities.
     */
    private Visual[] remotePlayers;

    /**
     * Creates a new set of craft entities.
     * @param _resources   The resources class to get ship images from.
     * @param _players     Player data to create the entities from.
     * @param _spawn       The spawn points of the current track to determine starting positions.
     * @param _checkpoints All the checkpoints in this track.
     * @param _ourID       Which ID belongs to the client.
     */
    public Craft(Resources _resources, Player[] _players, CircuitSpawn _spawn, Ray[] _checkpoints, int _ourID) {

        remotePlayers = new Visual[_players.length];

        for (int i = 0; i < _players.length; i++) {
            if (_players[i] == null) continue;

            // Get the spawn position and tangent for the initial position and rotation
            int spawnIndex = (_spawn.spawns.length - 1) - i;
            Vector2 tangent = _spawn.spawns[spawnIndex].tangent;

            // Find the correct atlas region for this player.
            TextureAtlas.AtlasRegion texture = _resources.craftAtlas.findRegion("craft", _players[i].getShipIndex());

            // Create and set the initial position, rotation, and origin
            Visual craft = new Visual("local player " + i);
            craft.region = texture;
            craft.color  = new Color(
                    _players[i].getShipColor()[0],
                    _players[i].getShipColor()[1],
                    _players[i].getShipColor()[2], 1);

            craft.origin.x = 32;
            craft.origin.y = 32;
            craft.rotation = (float)(Math.atan2(tangent.y, tangent.x) * 180 / Math.PI);

            craft.pos.x = _spawn.spawns[spawnIndex].position.x - (craft.origin.x);
            craft.pos.y = _spawn.spawns[spawnIndex].position.y - (craft.origin.y);

            if (i == _ourID) {
                remotePlayers[i] = createLocalPlayer(craft, _players[i], _checkpoints);
            } else {
                remotePlayers[i] = createNetworkPlayer(craft, _players[i]);
            }
        }

    }

    /**
     * Returns all of the player entities in this race.
     * @return Visual[]
     */
    public Visual[] getRemotePlayers() {
        return remotePlayers;
    }

    /**
     * Calls destroy on all player entities.
     */
    public void destroy() {
        for (Visual v : remotePlayers) {
            if (v == null) continue;
            v.destroy();
        }
    }

    /**
     * Creates a local player entity.
     * This entity will have all of the components needed to move and calculate collisions itself.
     * @return Visual entity.
     */
    private Visual createLocalPlayer(Visual _visual, Player _player, Ray[] _checkpoints) {
        _visual.add(new InputComponent("input"));
        _visual.add(new StatsComponent("stats"));
        _visual.add(new VelocityComponent("velocity"));
        _visual.add(new PhysicsComponent("physics"));
        _visual.add(new ShadowComponent("shadow"));
        _visual.add(new AABBComponent("aabb", 256, 256, true));
        _visual.add(new PolygonComponent("polygon", new Vector[] {
                new Vector(0, -16), new Vector(70, -5), new Vector(70, 5), new Vector(0, 16)
        }));
        _visual.add(new TrailComponent("trail", new Color(_player.getTrailColor()[0], _player.getTrailColor()[1], _player.getTrailColor()[2], 1)));

        EntityStateMachine fsm = new EntityStateMachine("fsm");
        fsm.createState("InActive");
        fsm.createState("Active")
                .add(new LocalInputComponent("local-input"))
                .add(new LapTracker("lap_tracker", _checkpoints));

        _visual.add(fsm);
        fsm.changeState("InActive");

        return _visual;
    }

    /**
     * Creates a networked player entity.
     * This entity will not have any of the components needed for moving by itself.
     * @return Visual entity.
     */
    private Visual createNetworkPlayer(Visual _visual, Player _player) {
        _visual.add(new ShadowComponent("shadow"));
        _visual.add(new AABBComponent("aabb", 256, 256, true));
        _visual.add(new PolygonComponent("polygon", new Vector[] {
                new Vector(0, -16), new Vector(70, -5), new Vector(70, 5), new Vector(0, 16)
        }));
        _visual.add(new TrailComponent("trail", new Color(_player.getTrailColor()[0], _player.getTrailColor()[1], _player.getTrailColor()[2], 1)));

        return _visual;
    }
}
