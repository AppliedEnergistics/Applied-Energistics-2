package appeng.parts;

public enum IndicatorState {
    OFF(false, 0, 0, 0),
    BOOTING(false, 255, 128, 16),
    MISSING_CHANNEL(false, 255, 128, 16),
    ONLINE(false, 0, 255, 0);

    private final boolean blinking;
    private final int red;
    private final int green;
    private final int blue;

    IndicatorState(boolean blinking, int red, int green, int blue) {
        this.blinking = blinking;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public boolean isBlinking() {
        return blinking;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }
}
