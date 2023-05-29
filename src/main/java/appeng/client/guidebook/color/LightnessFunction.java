package appeng.client.guidebook.color;

/**
 * Applies a function to an existing color that makes it lighter or darker by a given percentage.
 */
public class LightnessFunction implements ColorValue {
    private final ColorValue color;
    private final float percentage;

    public LightnessFunction(ColorValue color, float percentage) {
        this.color = color;
        this.percentage = percentage;
    }

    @Override
    public int resolve(LightDarkMode lightDarkMode) {
        var mutableColor = MutableColor.of(color, lightDarkMode);
        if (percentage < 0) {
            mutableColor.darker(-percentage);
        } else if (percentage > 0) {
            mutableColor.lighter(percentage);
        }
        return mutableColor.toArgb32();
    }
}
