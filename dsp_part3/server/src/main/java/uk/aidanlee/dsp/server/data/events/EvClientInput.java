package uk.aidanlee.dsp.server.data.events;

import uk.aidanlee.dsp.common.net.commands.CmdClientInput;

public class EvClientInput {
    public final CmdClientInput cmd;

    public EvClientInput(CmdClientInput _cmd) {
        cmd = _cmd;
    }
}
