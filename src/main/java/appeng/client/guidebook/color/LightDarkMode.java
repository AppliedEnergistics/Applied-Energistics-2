package appeng.client.guidebook.color;

public enum LightDarkMode {
    LIGHT_MODE,
    DARK_MODE;

    public static LightDarkMode current() {
        return LIGHT_MODE;
    }
}
