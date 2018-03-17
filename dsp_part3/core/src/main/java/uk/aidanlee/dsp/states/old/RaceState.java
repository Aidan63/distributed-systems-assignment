package uk.aidanlee.dsp.states.old;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import uk.aidanlee.dsp.common.components.InputComponent;
import uk.aidanlee.dsp.common.net.Packet;
import uk.aidanlee.dsp.common.net.commands.CmdClientInput;
import uk.aidanlee.dsp.common.net.commands.Command;
import uk.aidanlee.dsp.common.structural.State;
import uk.aidanlee.dsp.common.structural.ec.Visual;
import uk.aidanlee.dsp.common.utils.MathsUtil;
import uk.aidanlee.dsp.data.race.Race;

import java.util.LinkedList;

public class RaceState extends State {
    private SpriteBatch spriteBatch;

    public RaceState(String _name) {
        super(_name);
    }

    @Override
    public void onEnter(Object _enterWith) {
        Game.race = new Race();
        Game.race.craft.createCraft();
        Game.race.view.setup();

        spriteBatch = new SpriteBatch();
    }

    @Override
    public void onUpdate(LinkedList<Command> _cmds) {
        // Update the viewport size
        Game.race.view.resize();

        // Process all entities
        for (Visual v : Game.race.craft.getRemotePlayers()) {
            if (v == null) continue;
            v.update(0);
        }

        // Get the player Visual and update the camera.
        Visual player = Game.race.craft.getRemotePlayers()[Game.connections.getUs().getId()];
        Game.race.view.getCamera().position.x = (float)(player.pos.x + MathsUtil.lengthdirX(200, player.rotation));
        Game.race.view.getCamera().position.y = (float)(player.pos.y + MathsUtil.lengthdirY(200, player.rotation));

        // Send our currently pressed inputs to the server.
        Game.netChan.addCommand(new CmdClientInput(Game.connections.getUs().getId(), (InputComponent) player.get("input")));

        // Send a netchan update out.
        Packet packet = Game.netChan.send();
        if (packet != null) {
            Game.netManager.send(packet);
        }
    }

    @Override
    public void onRender() {
        Gdx.gl.glClearColor(0.47f, 0.56f, 0.61f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Game.race.view.getViewport().apply();
        Matrix4 projCamera = Game.race.view.getCamera().combined;

        spriteBatch.setProjectionMatrix(projCamera);
        spriteBatch.begin();

        for (Visual v : Game.race.craft.getRemotePlayers()) {
            if (v == null) continue;

            v.draw(spriteBatch);
        }

        spriteBatch.end();
    }
}
