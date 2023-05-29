package appeng.client.guidebook.color;

public record ConstantColor(int color) implements ColorValue {
    public static ConstantColor WHITE = new ConstantColor(-1);

    @Override
    public int resolve(LightDarkMode lightDarkMode) {
        return color;
    }
}
