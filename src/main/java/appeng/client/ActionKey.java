package appeng.client;


import org.lwjgl.input.Keyboard;


public enum ActionKey {
    TOGGLE_FOCUS(Keyboard.KEY_TAB);

    private final int defaultKey;

    ActionKey(int defaultKey) {
        this.defaultKey = defaultKey;
    }

    public String getTranslationKey() {
        return "key." + this.name().toLowerCase() + ".desc";
    }

    public int getDefaultKey() {
        return this.defaultKey;
    }
}
