package uk.aidanlee.dsp.utils;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.IntMap;
import glm_.vec2.Vec2d;
import imgui.impl.LwjglGL3;
import org.lwjgl.glfw.GLFW;

public class ImGuiInputProcessor extends InputAdapter {
    private final IntMap<Integer> gdxGLFWKeyMap;

    public ImGuiInputProcessor() {
        gdxGLFWKeyMap = new IntMap<>();
        gdxGLFWKeyMap.put(Input.Keys.TAB, GLFW.GLFW_KEY_TAB);

        gdxGLFWKeyMap.put(Input.Keys.LEFT , GLFW.GLFW_KEY_LEFT);
        gdxGLFWKeyMap.put(Input.Keys.RIGHT, GLFW.GLFW_KEY_RIGHT);
        gdxGLFWKeyMap.put(Input.Keys.UP   , GLFW.GLFW_KEY_UP);
        gdxGLFWKeyMap.put(Input.Keys.DOWN , GLFW.GLFW_KEY_DOWN);

        gdxGLFWKeyMap.put(Input.Keys.PAGE_UP  , GLFW.GLFW_KEY_PAGE_UP);
        gdxGLFWKeyMap.put(Input.Keys.PAGE_DOWN, GLFW.GLFW_KEY_PAGE_DOWN);

        gdxGLFWKeyMap.put(Input.Keys.HOME, GLFW.GLFW_KEY_HOME);
        gdxGLFWKeyMap.put(Input.Keys.END , GLFW.GLFW_KEY_END);

        gdxGLFWKeyMap.put(Input.Keys.BACKSPACE, GLFW.GLFW_KEY_BACKSPACE);
        gdxGLFWKeyMap.put(Input.Keys.SPACE    , GLFW.GLFW_KEY_SPACE);

        gdxGLFWKeyMap.put(Input.Keys.ENTER , GLFW.GLFW_KEY_ENTER);
        gdxGLFWKeyMap.put(Input.Keys.ESCAPE, GLFW.GLFW_KEY_ESCAPE);

        gdxGLFWKeyMap.put(Input.Keys.CONTROL_LEFT , GLFW.GLFW_KEY_LEFT_CONTROL);
        gdxGLFWKeyMap.put(Input.Keys.CONTROL_RIGHT, GLFW.GLFW_KEY_RIGHT_CONTROL);
        gdxGLFWKeyMap.put(Input.Keys.ALT_LEFT   , GLFW.GLFW_KEY_LEFT_ALT);
        gdxGLFWKeyMap.put(Input.Keys.ALT_RIGHT  , GLFW.GLFW_KEY_RIGHT_ALT);
        gdxGLFWKeyMap.put(Input.Keys.SHIFT_LEFT , GLFW.GLFW_KEY_LEFT_SHIFT);
        gdxGLFWKeyMap.put(Input.Keys.SHIFT_RIGHT, GLFW.GLFW_KEY_RIGHT_SHIFT);

        gdxGLFWKeyMap.put(Input.Keys.A, GLFW.GLFW_KEY_A);
        gdxGLFWKeyMap.put(Input.Keys.C, GLFW.GLFW_KEY_C);
        gdxGLFWKeyMap.put(Input.Keys.V, GLFW.GLFW_KEY_V);
        gdxGLFWKeyMap.put(Input.Keys.X, GLFW.GLFW_KEY_X);
        gdxGLFWKeyMap.put(Input.Keys.Y, GLFW.GLFW_KEY_Y);
        gdxGLFWKeyMap.put(Input.Keys.Z, GLFW.GLFW_KEY_Z);
    }

    @Override
    public boolean keyTyped(char character) {
        LwjglGL3.INSTANCE.getCharCallback().invoke((int)character);
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        LwjglGL3.INSTANCE.getScrollCallback().invoke(new Vec2d(0, -amount));
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (!gdxGLFWKeyMap.containsKey(keycode)) return false;
        LwjglGL3.INSTANCE.getKeyCallback().invoke(gdxGLFWKeyMap.get(keycode), 0, GLFW.GLFW_RELEASE, 0);

        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (!gdxGLFWKeyMap.containsKey(keycode)) return false;
        LwjglGL3.INSTANCE.getKeyCallback().invoke(gdxGLFWKeyMap.get(keycode), 0, GLFW.GLFW_PRESS, 0);

        return false;
    }
}
