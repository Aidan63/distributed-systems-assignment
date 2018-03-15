package uk.aidanlee.dsp.server.states;

import uk.aidanlee.dsp.common.net.Snapshot;
import uk.aidanlee.dsp.common.net.commands.CmdSnapshot;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.common.structural.ec.Entity;
import uk.aidanlee.dsp.server.Server;
import uk.aidanlee.dsp.server.race.Race;

public class RaceState extends State {
    public RaceState(String _name) {
        super(_name);
    }

    @Override
    public void onEnter(Object _enterWith) {
        Server.race = new Race();

        Server.race.circuit.load("/media/aidan/BAD1-1589/dsp/dsp_part2/assets/tracks/track.p2");
        Server.race.craft.createCraft();
    }

    @Override
    public void onUpdate() {
        Snapshot state = new Snapshot();

        for (int i = 0; i < Server.connections.getMaxClients(); i++) {
            if (!Server.connections.getClientConnected()[i]) continue;

            Entity e = Server.race.craft.getRemotePlayers()[i];
            e.update(0);

            state.addPlayer(i, e.pos.x, e.pos.y, e.rotation);
        }

        Server.connections.addCommandAll(new CmdSnapshot(state));
    }
}
