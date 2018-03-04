package uk.aidanlee.dsp;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.TimeUtils;
import glm_.vec2.Vec2;
import imgui.*;
import imgui.impl.LwjglGL3;
import uno.glfw.GlfwWindow;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private double accumulator;
    private double currentTime;
    private static final float step = 1.0f / 60.0f;

    /**
     * ImGui context
     */
    private Context imguiContext;

    /**
     * Texture atlas for the player images.
     */
    private TextureAtlas playerAtlas;

    /**
     * Sprite batch to draw the players
     */
    private SpriteBatch spriteBatch;

    private int[] player1Design;
    private int[] player2Design;

    private float[] player1Rotation;
    private float[] player2Rotation;

    private float[] player1Color;
    private float[] player2Color;

    private Sprite player1;
    private Sprite player2;

    @Override
    public void create() {
        super.create();

        // ImGui setup
        Lwjgl3Graphics gfx = (Lwjgl3Graphics) Gdx.graphics;
        GlfwWindow imguiWindow = new GlfwWindow(gfx.getWindow().getWindowHandle());

        imguiContext = new Context(null);
        LwjglGL3.INSTANCE.init(imguiWindow, false);

        // Drawing setup
        playerAtlas = new TextureAtlas(Gdx.files.internal("assets/craft.atlas"));
        spriteBatch = new SpriteBatch();

        player1Design = new int[] { 0 };
        player2Design = new int[] { 0 };

        player1Rotation = new float[] { 0 };
        player2Rotation = new float[] { 0 };

        player1Color = new float[] { 1, 1, 1, 1 };
        player2Color = new float[] { 1, 1, 1, 1 };

        player1 = playerAtlas.createSprite("craft", player1Design[0]);
        player2 = playerAtlas.createSprite("craft", player2Design[0]);

        player1.setOrigin(64, 32);
        player2.setOrigin(64, 32);

        player1.setPosition(620, 356);
        player2.setPosition(620, 178);
    }

    @Override
    public void render() {
        // Fixed time step loop
        // Loop will update the game state at a fixed rate of 60 times per second (0.1666 delta time)
        // but the game will render as fast the computer will allow it.

        double newTime   = TimeUtils.millis() / 1000.0;
        double frameTime = Math.min(newTime - currentTime, 0.25);
        currentTime = newTime;

        accumulator += frameTime;

        while (accumulator >= step)
        {
            accumulator -= step;

            LwjglGL3.INSTANCE.newFrame();
            update();
        }

        draw();

        ImGui.INSTANCE.render();
        LwjglGL3.INSTANCE.renderDrawData(ImGui.INSTANCE.getDrawData());
    }

    /**
     * Update function.
     */
    private void update() {
        ImGui.INSTANCE.setNextWindowPos(new Vec2(20, 20), Cond.Always, new Vec2());
        ImGui.INSTANCE.setNextWindowSize(new Vec2(430, 210), Cond.Always);
        ImGui.INSTANCE.begin("Player Settings", null, WindowFlags.NoResize.getI() | WindowFlags.NoCollapse.getI() | WindowFlags.NoMove.getI());

        ImGui.INSTANCE.sliderFloat("Player 1 Rotation", player1Rotation, 0f, 360f, "%.3f", 1f);
        ImGui.INSTANCE.sliderFloat("Player 2 Rotation", player2Rotation, 0f, 360f, "%.3f", 1f);

        ImGui.INSTANCE.newLine();

        ImGui.INSTANCE.colorEdit3("Player 1 Colour", player1Color, 0);
        ImGui.INSTANCE.colorEdit3("Player 2 Colour", player2Color, 0);

        ImGui.INSTANCE.newLine();

        ImGui.INSTANCE.sliderInt("Player 1 Image", player1Design, 0, playerAtlas.findRegions("craft").size - 1, "%.0f");
        ImGui.INSTANCE.sliderInt("Player 2 Image", player2Design, 0, playerAtlas.findRegions("craft").size - 1, "%.0f");

        ImGui.INSTANCE.end();

        player1.setRotation(player1Rotation[0]);
        player2.setRotation(player2Rotation[0]);

        player1.setColor(player1Color[0], player1Color[1], player1Color[2], 1);
        player2.setColor(player2Color[0], player2Color[1], player2Color[2], 1);

        player1.setRegion(playerAtlas.findRegion("craft", player1Design[0]));
        player2.setRegion(playerAtlas.findRegion("craft", player2Design[0]));
    }

    /**
     *
     */
    private void draw() {
        Gdx.gl.glClearColor(0.47f, 0.56f, 0.61f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriteBatch.begin();
        player1.draw(spriteBatch);
        player2.draw(spriteBatch);
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        playerAtlas.dispose();
        spriteBatch.dispose();

        LwjglGL3.INSTANCE.shutdown();
        ContextKt.destroy(imguiContext);
    }
}