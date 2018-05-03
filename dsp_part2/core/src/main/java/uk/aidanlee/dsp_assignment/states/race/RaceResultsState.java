package uk.aidanlee.dsp_assignment.states.race;

import com.badlogic.gdx.Gdx;
import glm_.vec2.Vec2;
import imgui.Cond;
import imgui.ImGui;
import imgui.WindowFlags;
import uk.aidanlee.dsp_assignment.structural.State;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class RaceResultsState extends State {

    /**
     * Ordered time data.
     */
    private List<PlayerTimes> timesData;

    public RaceResultsState(String _name) {
        super(_name);
    }

    /**
     * When entering the state we sum up and order the time data we entered with.
     * @param _enterWith Unordered time data for each player.
     */
    @Override
    public void onEnter(Object _enterWith) {
        Map<String, List<Float>> times = (Map<String, List<Float>>) _enterWith;

        timesData = new ArrayList<>();
        for (Map.Entry<String, List<Float>> entry : times.entrySet()) {
            timesData.add(new PlayerTimes(entry.getKey(), entry.getValue()));
        }

        timesData.sort((_t1, _t2) -> Float.compare(_t1.totalTime, _t2.totalTime));
    }

    @Override
    public void onUpdate() {
        ImGui.INSTANCE.setNextWindowPos(new Vec2((Gdx.graphics.getWidth() / 2) - 300, (Gdx.graphics.getHeight() / 2) - 200), Cond.Always, new Vec2());
        ImGui.INSTANCE.setNextWindowSize(new Vec2(600, 400), Cond.Always);
        ImGui.INSTANCE.begin("Results", null, WindowFlags.NoResize.getI());

        for (PlayerTimes time : timesData) {
            // Create a collapsible header with all of that players times.
            // The header text contains the player name and total time, label under the header is a lap time.
            ImGui.INSTANCE.pushId(time.name);
            if (ImGui.INSTANCE.collapsingHeader(time.name + " : " + formatTime(time.totalTime), 0)) {
                for (float t : time.lapTimes) {
                    ImGui.INSTANCE.text(formatTime(t));
                }
            }
            ImGui.INSTANCE.popId();
        }

        ImGui.INSTANCE.end();
    }

    /**
     * Returns a "--:--:--" formatted time string of the provided time.
     *
     * @param _time Number of seconds
     * @return Formatted time string.
     */
    private String formatTime(float _time) {
        Date date = new Date((long) (_time * 1000));
        String formatted = new SimpleDateFormat("mm:ss:SS").format(date);
        return formatted.substring(0, Math.min(formatted.length(), 8));
    }

    /**
     * Simple class which holds all time data on a particular client.
     */
    private class PlayerTimes {
        /**
         * The player name.
         */
        final String name;

        /**
         * The total time for this race.
         */
        final float totalTime;

        /**
         * All individual lap times.
         */
        final List<Float> lapTimes;

        private PlayerTimes(String _name, List<Float> _times) {
            name      = _name;
            lapTimes  = _times;
            totalTime = _times.stream().mapToInt(Float::intValue).sum();
        }
    }
}
