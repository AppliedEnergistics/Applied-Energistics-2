package appeng.client;

import java.util.Locale;

import org.lwjgl.glfw.GLFW;

public enum ActionKey {
    TOGGLE_FOCUS(GLFW.GLFW_KEY_TAB);

    private final int defaultKey;

    private ActionKey(int defaultKey) {
        this.defaultKey = defaultKey;
    }

    public String getTranslationKey() {
        return "key." + this.name().toLowerCase(Locale.ROOT) + ".desc";
    }

    public int getDefaultKey() {
        return this.defaultKey;
    }
}
