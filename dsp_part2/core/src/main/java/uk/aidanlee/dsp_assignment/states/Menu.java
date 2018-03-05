package uk.aidanlee.dsp_assignment.states;

import java.util.HashMap;
import java.util.Map;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import imgui.Cond;
import imgui.ImGui;
import imgui.WindowFlags;
import uk.aidanlee.dsp_assignment.data.SplitType;
import uk.aidanlee.dsp_assignment.race.PlayerSetting;
import uk.aidanlee.dsp_assignment.race.RaceSettings;
import uk.aidanlee.dsp_assignment.structural.State;

public class Menu extends State {
    /*
      Arrays are used to hold single values since that's how the java port of ImGui handles modifying variables.
      Saves having to create other variables which are arrays containing other variables
     */

    /**
     * One element containing how many local players there are.
     */
    private int[] localPlayers;

    /**
     * One element (0 - 1) mapping onto the SplitType enum for how the screen should be split for 2 or 3 players.
     */
    private int[] splitType;

    /**
     * One element which sets if boost pads should be enabled in game.
     */
    private boolean[] boostPads;

    /**
     * Staggered array storing the index for each players image in the texture atlas.
     * The first array index is the player ID (0 - 3) and the second array has one element which is the image index.
     */
    private int[][] playerShips;

    /**
     * Map storing all the player colours.
     */
    private Map<Integer, Vec4> playerShipColours;

    /**
     * Map storing all the players trail colours.
     */
    private Map<Integer, Vec4> playerTrailColours;

    /**
     * Texture atlas for the player image data.
     */
    private TextureAtlas texture;

    public Menu(String _name) {
        super(_name);
    }

    @Override
    public void onEnter(Object _enterWith) {
        super.onEnter(_enterWith);

        // Setup default values for the players

        localPlayers = new int[] { 1 };
        splitType    = new int[] { 0 };
        boostPads    = new boolean[] { true };
        texture      = new TextureAtlas(Gdx.files.internal("assets/textures/craft.atlas"));

        playerShipColours = new HashMap<>();
        playerShipColours.put(0, new Vec4(1, 1, 1, 1));
        playerShipColours.put(1, new Vec4(1, 1, 1, 1));
        playerShipColours.put(2, new Vec4(1, 1, 1, 1));
        playerShipColours.put(3, new Vec4(1, 1, 1, 1));

        playerTrailColours = new HashMap<>();
        playerTrailColours.put(0, new Vec4(1, 1, 1, 1));
        playerTrailColours.put(1, new Vec4(1, 1, 1, 1));
        playerTrailColours.put(2, new Vec4(1, 1, 1, 1));
        playerTrailColours.put(3, new Vec4(1, 1, 1, 1));

        playerShips = new int[][] {
                new int[] { 0 },
                new int[] { 0 },
                new int[] { 0 },
                new int[] { 0 }
        };
    }

    @Override
    public void onLeave(Object _leaveWith) {
        super.onLeave(_leaveWith);

        texture.dispose();
    }

    @Override
    public void onUpdate() {
        uiRaceSettings();
        uiPlayerSettings();
    }

    @Override
    public void onRender() {
        Gdx.gl.glClearColor(0.47f, 0.56f, 0.61f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Nothing is explicitly drawn since everything in this state is part of ImGui and drawn by that instead.
    }

    /**
     * Creates a ImGui window with options for number of players, split screen type, boost pads, and a button to start the race.
     */
    private void uiRaceSettings() {
        ImGui.INSTANCE.setNextWindowPos(new Vec2(32, 32), Cond.Always, new Vec2());
        ImGui.INSTANCE.setNextWindowSize(new Vec2(400, 180), Cond.Always);
        ImGui.INSTANCE.begin("Race Settings", null, WindowFlags.NoResize.getI() | WindowFlags.NoCollapse.getI());
        ImGui.INSTANCE.sliderInt("players", localPlayers, 1, 4, "%.0f");

        ImGui.INSTANCE.text("Splitscreen Type");
        ImGui.INSTANCE.indent(8);
        ImGui.INSTANCE.radioButton("Vertical"  , splitType, 0);
        ImGui.INSTANCE.radioButton("Horizontal", splitType, 1);
        ImGui.INSTANCE.unindent(8);

        ImGui.INSTANCE.text("Game Options");
        ImGui.INSTANCE.indent(8);
        ImGui.INSTANCE.checkbox("Boost pads", boostPads);
        ImGui.INSTANCE.unindent(8);

        if (ImGui.INSTANCE.button("Start Race", new Vec2(-1, 0))) {
            startRace();
        }
        ImGui.INSTANCE.end();
    }

    /**
     * Creates an ImGui window with controls to modify each local players options.
     */
    private void uiPlayerSettings() {
        ImGui.INSTANCE.setNextWindowPos(new Vec2(464, 32), Cond.Always, new Vec2());
        ImGui.INSTANCE.setNextWindowSize(new Vec2(500, 660), Cond.Always);
        ImGui.INSTANCE.begin("Player Settings", null, WindowFlags.NoResize.getI() | WindowFlags.NoCollapse.getI());

        for (int i = 0; i < localPlayers[0]; i++) {
            uiDrawPlayer(i);
        }

        ImGui.INSTANCE.end();
    }

    /**
     * Draw all the controls to modify one local players options.
     * @param _i The local players ID to modify.
     */
    private void uiDrawPlayer(int _i) {
        ImGui.INSTANCE.pushId(_i);

        // Get the current players texture region.
        TextureRegion region = texture.findRegion("craft", playerShips[_i][0]);

        ImGui.INSTANCE.text("Player " + (_i + 1));
        ImGui.INSTANCE.image(region.getTexture().getTextureObjectHandle(), new Vec2(128, 64), new Vec2(region.getU(), region.getV()), new Vec2(region.getU2(), region.getV2()), playerShipColours.get(_i), playerTrailColours.get(_i));
        ImGui.INSTANCE.sliderInt("ship", playerShips[_i], 0, 7, "%.0f");
        ImGui.INSTANCE.colorEdit3("ship colour" , playerShipColours.get(_i) , 0);
        ImGui.INSTANCE.colorEdit3("trail colour", playerTrailColours.get(_i), 0);

        ImGui.INSTANCE.popId();
    }

    /**
     * Called once the start race button has been pressed.
     * Create a RaceSettings instances and switches to the game state passing that instance as the onEnter data.
     */
    private void startRace() {
        PlayerSetting[] playerSettings = new PlayerSetting[localPlayers[0]];
        for (int i = 0; i < localPlayers[0]; i++) {
            playerSettings[i] = new PlayerSetting(
                    playerShips[i][0],
                    new float[] { playerShipColours.get(i).x, playerShipColours.get(i).y, playerShipColours.get(i).z },
                    new float[] { playerTrailColours.get(i).x, playerTrailColours.get(i).y, playerTrailColours.get(i).z }
            );
        }

        RaceSettings settings = new RaceSettings(
                localPlayers[0],
                SplitType.values()[splitType[0]],
                boostPads[0],
                playerSettings
        );

        changeState("game", settings, null);
    }
}
