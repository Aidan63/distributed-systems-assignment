package uk.aidanlee.dsp.server.data.events;

import uk.aidanlee.dsp.common.net.commands.CmdClientSettings;

public class EvClientSettings {
    public final CmdClientSettings cmd;

    public EvClientSettings(CmdClientSettings _cmd) {
        cmd = _cmd;
    }
}
