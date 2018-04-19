package uk.aidanlee.dsp.common.net.commands;

import uk.aidanlee.dsp.common.net.Packet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CmdRaceResults extends Command {
    public final int laps;
    public final Map<Integer, List<Float>> times;

    public CmdRaceResults(Map<Integer, List<Float>> _times, int _laps) {
        super(Command.RACE_RESULTS);
        times = _times;
        laps  = _laps;
    }

    public CmdRaceResults(Packet _packet, int _sentTime) {
        super(Command.RACE_RESULTS, _sentTime);
        laps  = _packet.getData().readByte();
        times = new HashMap<>();

        int players = _packet.getData().readByte();
        for (int i = 0; i < players; i++) {

            List<Float> t = new LinkedList<>();
            int clientID  = _packet.getData().readByte();

            for (int j = 0; j < laps; j++) {
                t.add(_packet.getData().readFloat());
            }
            times.put(clientID, t);
        }
    }

    @Override
    public void add(Packet _packet) {
        _packet.getData().writeByte(Command.RACE_RESULTS);
        _packet.getData().writeByte((byte) laps);
        _packet.getData().writeByte((byte) times.size());

        for (Map.Entry<Integer, List<Float>> entry : times.entrySet()) {
            _packet.getData().writeByte(entry.getKey().byteValue());
            for (float t : entry.getValue()) {
                _packet.getData().writeFloat(t);
            }
        }
    }
}
