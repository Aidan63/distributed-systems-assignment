package uk.aidanlee.dsp.data.events;

import uk.aidanlee.dsp.common.net.commands.Command;

public class EvAddUnreliableCommand {
    public final Command cmd;

    public EvAddUnreliableCommand(Command _cmd) {
        cmd = _cmd;
    }
}
