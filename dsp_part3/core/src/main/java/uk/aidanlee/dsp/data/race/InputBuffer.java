package uk.aidanlee.dsp.data.race;

import uk.aidanlee.dsp.common.net.commands.CmdClientInput;

import java.util.LinkedList;
import java.util.List;

/**
 * Keeps a sliding window of the previous input commands sent to the server along with the time sent and netchan sequence.
 */
public class InputBuffer {
    /**
     * The maximum size of the sliding window.
     */
    private int maxSize;

    /**
     * The sliding window array.
     */
    private List<InputRecord> inputs;

    /**
     * Creates a new input buffer of the specified size.
     * @param _maxSize Maximum number of inputs to store.
     */
    public InputBuffer(int _maxSize) {
        maxSize = _maxSize;
        inputs  = new LinkedList<>();
    }

    /**
     * Returns the sliding window of input commands.
     * @return array of inputs.
     */
    public List<InputRecord> getInputs() {
        return inputs;
    }

    /**
     * Adds a new input command to the buffer indexed by the netchan sequence.
     * @param _input    Input command sent.
     */
    public void addEntry(CmdClientInput _input) {
        inputs.add(new InputRecord(_input));

        if (inputs.size() > maxSize) {
            inputs.remove(0);
        }
    }

    public class InputRecord {
        public final int sentTime;
        public final CmdClientInput input;

        public InputRecord(CmdClientInput _input) {
            sentTime = (int) System.currentTimeMillis();
            input    = _input;
        }
    }
}
