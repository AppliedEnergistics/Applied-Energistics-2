package appeng.client.guidebook.render;

public enum SymbolicColor {
    BODY_TEXT(Colors.argb(255, 174, 174, 174), Colors.argb(255, 174, 174, 174)),
    THEMATIC_BREAK(Colors.argb(255, 55, 55, 55), Colors.argb(255, 155, 155, 155));

    final int lightMode;
    final int darkMode;

    SymbolicColor(int lightMode, int darkMode) {
        this.lightMode = lightMode;
        this.darkMode = darkMode;
    }

    private final ColorRef ref = new ColorRef(this);

    public ColorRef ref() {
        return ref;
    }

    public int resolve(LightDarkMode lightDarkMode) {
        return lightDarkMode == LightDarkMode.LIGHT_MODE ? lightMode : darkMode;
    }
}
