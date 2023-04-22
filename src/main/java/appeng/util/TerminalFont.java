package appeng.util;

public class TerminalFont {
    public static final float DEFAULT_SIZE = 0.5f;
    private final float fontSize;
    private final boolean isLargeFont;

    public TerminalFont(double fontSize) {
        this.fontSize = (float) fontSize;
        this.isLargeFont = this.fontSize >= 0.85f;
    }

    public float getFontSize() {
        return fontSize;
    }

    public boolean isUseLargeFonts() {
        return isLargeFont;
    }
}
