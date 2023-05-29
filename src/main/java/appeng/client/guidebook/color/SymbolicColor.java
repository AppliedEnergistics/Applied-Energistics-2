package appeng.client.guidebook.color;

/**
 * TODO Colors from WIP patchouli book "nameplate_color": "8A5BA4", "link_color": "8A5BA4", "link_hover_color":
 * "D7BBEC", "macros": { "$(item)": "$(#582E70)", "$(thing)": "$(#582E70)", "$(todo)": "$(#FF0000)" },
 */
public enum SymbolicColor implements ColorValue {
    LINK(Colors.rgb(0, 213, 255), Colors.rgb(0, 213, 255)),
    BODY_TEXT(Colors.rgb(174, 174, 174), Colors.rgb(174, 174, 174)),
    ERROR_TEXT(Colors.rgb(255, 0, 0), Colors.rgb(255, 0, 0)),
    /**
     * Color used for the type of crafting shown in recipe blocks.
     */
    CRAFTING_RECIPE_TYPE(Colors.rgb(64, 64, 64), Colors.rgb(64, 64, 64)),
    THEMATIC_BREAK(Colors.rgb(55, 55, 55), Colors.rgb(155, 155, 155)),

    HEADER1_SEPARATOR(Colors.argb(127, 255, 255, 255), Colors.argb(127, 255, 255, 255)),
    HEADER2_SEPARATOR(Colors.argb(127, 174, 174, 174), Colors.argb(127, 174, 174, 174)),

    NAVBAR_BG_TOP(Colors.rgb(0, 0, 0), Colors.rgb(0, 0, 0)),
    NAVBAR_BG_BOTTOM(Colors.argb(127, 0, 0, 0), Colors.argb(127, 0, 0, 0)),
    NAVBAR_ROW_HOVER(Colors.rgb(33, 33, 33), Colors.rgb(33, 33, 33)),
    NAVBAR_EXPAND_ARROW(Colors.rgb(238, 238, 238), Colors.rgb(238, 238, 238)),
    TABLE_BORDER(Colors.rgb(124, 124, 124), Colors.rgb(124, 124, 124)),

    ICON_BUTTON_NORMAL(Colors.mono(200), Colors.mono(200)),
    ICON_BUTTON_DISABLED(Colors.mono(64), Colors.mono(64)),
    ICON_BUTTON_HOVER(Colors.rgb(0, 213, 255), Colors.rgb(0, 213, 255)),

    IN_WORLD_BLOCK_HIGHLIGHT(Colors.argb(0xcc, 0x99, 0x99, 0x99), Colors.argb(0xcc, 0x99, 0x99, 0x99));

    final int lightMode;
    final int darkMode;

    SymbolicColor(int lightMode, int darkMode) {
        this.lightMode = lightMode;
        this.darkMode = darkMode;
    }

    @Override
    public int resolve(LightDarkMode lightDarkMode) {
        return lightDarkMode == LightDarkMode.LIGHT_MODE ? lightMode : darkMode;
    }
}
