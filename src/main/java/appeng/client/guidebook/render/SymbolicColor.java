package appeng.client.guidebook.render;

/**
 * TODO Colors from WIP patchouli book "nameplate_color": "8A5BA4", "link_color": "8A5BA4", "link_hover_color":
 * "D7BBEC", "macros": { "$(item)": "$(#582E70)", "$(thing)": "$(#582E70)", "$(todo)": "$(#FF0000)" },
 */
public enum SymbolicColor {
    LINK(Colors.argb(255, 0, 213, 255), Colors.argb(255, 0, 213, 255)),
    BODY_TEXT(Colors.argb(255, 174, 174, 174), Colors.argb(255, 174, 174, 174)),
    /**
     * Color used for the type of crafting shown in recipe blocks.
     */
    CRAFTING_RECIPE_TYPE(Colors.argb(255, 64, 64, 64), Colors.argb(255, 64, 64, 64)),
    THEMATIC_BREAK(Colors.argb(255, 55, 55, 55), Colors.argb(255, 155, 155, 155)),

    NAVBAR_BG_TOP(Colors.argb(255, 0, 0, 0), Colors.argb(255, 0, 0, 0)),
    NAVBAR_BG_BOTTOM(Colors.argb(127, 0, 0, 0), Colors.argb(127, 0, 0, 0)),
    NAVBAR_ROW_HOVER(Colors.argb(255, 33, 33, 33), Colors.argb(255, 33, 33, 33)),
    NAVBAR_EXPAND_ARROW(Colors.argb(255, 238, 238, 238), Colors.argb(255, 238, 238, 238)),
    TABLE_BORDER(Colors.argb(255, 124, 124, 124), Colors.argb(255, 124, 124, 124));

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
