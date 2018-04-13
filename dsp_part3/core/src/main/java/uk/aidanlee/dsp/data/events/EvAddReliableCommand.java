package uk.aidanlee.dsp.data.events;

import uk.aidanlee.dsp.common.net.commands.Command;

public class EvAddReliableCommand {
    public final Command cmd;

    public EvAddReliableCommand(Command _cmd) {
        cmd = _cmd;
    }
}
