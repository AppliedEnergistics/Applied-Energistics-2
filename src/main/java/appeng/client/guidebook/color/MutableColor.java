package appeng.client.guidebook.color;

import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * A helper for mutating color values.
 */
public final class MutableColor implements ColorValue {
    private float r = 1f;
    private float g = 1f;
    private float b = 1f;
    private float a = 1f;

    public MutableColor() {
    }

    public MutableColor(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public MutableColor(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public static MutableColor ofArgb32(int packedColor) {
        var r = FastColor.ARGB32.red(packedColor);
        var g = FastColor.ARGB32.green(packedColor);
        var b = FastColor.ARGB32.blue(packedColor);
        var a = FastColor.ARGB32.alpha(packedColor);
        return MutableColor.ofBytes(r, g, b, a);
    }

    public static MutableColor ofBytes(int r, int g, int b) {
        return ofBytes(r, g, b, 255);
    }

    public static MutableColor ofBytes(int r, int g, int b, int a) {
        return new MutableColor(
                fromByte(r),
                fromByte(g),
                fromByte(b),
                fromByte(a));
    }

    public static MutableColor of(ColorValue color) {
        return of(color, LightDarkMode.current());
    }

    public static MutableColor of(ColorValue color, LightDarkMode mode) {
        return ofArgb32(color.resolve(mode));
    }

    public int toArgb32() {
        return FastColor.ARGB32.color(
                alphaByte(),
                redByte(),
                greenByte(),
                blueByte());
    }

    public int toAbgr32() {
        return FastColor.ABGR32.color(
                alphaByte(),
                blueByte(),
                greenByte(),
                redByte());
    }

    public float red() {
        return r;
    }

    public float green() {
        return g;
    }

    public float blue() {
        return b;
    }

    public float alpha() {
        return a;
    }

    public MutableColor setRed(float r) {
        this.r = Mth.clamp(r, 0, 1);
        return this;
    }

    public MutableColor setGreen(float g) {
        this.g = Mth.clamp(g, 0, 1);
        return this;
    }

    public MutableColor setBlue(float b) {
        this.b = Mth.clamp(b, 0, 1);
        return this;
    }

    public MutableColor setAlpha(float a) {
        this.a = a;
        return this;
    }

    public int redByte() {
        return toByte(r);
    }

    public int greenByte() {
        return toByte(g);
    }

    public int blueByte() {
        return toByte(b);
    }

    public int alphaByte() {
        return toByte(a);
    }

    public MutableColor setRedByte(int value) {
        r = fromByte(value);
        return this;
    }

    public MutableColor setGreenByte(int value) {
        r = fromByte(value);
        return this;
    }

    public MutableColor setBlueByte(int value) {
        r = fromByte(value);
        return this;
    }

    public MutableColor setAlphaByte(int value) {
        r = fromByte(value);
        return this;
    }

    private static int toByte(float v) {
        return Mth.clamp(Math.round(v * 255), 0, 255);
    }

    private static float fromByte(int v) {
        return Mth.clamp(v / 255f, 0f, 1f);
    }

    public MutableColor lighter(float percentage) {
        addLuminance(percentage);
        return this;
    }

    public MutableColor darker(float percentage) {
        addLuminance(-percentage);
        return this;
    }

    private void addLuminance(float offset) {
        var lab = toOklab();
        lab[0] = Mth.clamp(lab[0] * (100 + offset) / 100.0f, 0.0f, 1.0f);
        fromOklab(lab);
    }

    /**
     * https://bottosson.github.io/posts/oklab/#converting-from-linear-srgb-to-oklab
     */
    private float[] toOklab() {
        float l = 0.4122214708f * this.r + 0.5363325363f * this.g + 0.0514459929f * this.b;
        float m = 0.2119034982f * this.r + 0.6806995451f * this.g + 0.1073969566f * this.b;
        float s = 0.0883024619f * this.r + 0.2817188376f * this.g + 0.6299787005f * this.b;

        float l_ = (float) Math.cbrt(l);
        float m_ = (float) Math.cbrt(m);
        float s_ = (float) Math.cbrt(s);

        return new float[] {
                0.2104542553f * l_ + 0.7936177850f * m_ - 0.0040720468f * s_,
                1.9779984951f * l_ - 2.4285922050f * m_ + 0.4505937099f * s_,
                0.0259040371f * l_ + 0.7827717662f * m_ - 0.8086757660f * s_,
        };
    }

    /**
     * https://bottosson.github.io/posts/oklab/#converting-from-linear-srgb-to-oklab
     */
    private void fromOklab(float[] c) {
        float l_ = c[0] + 0.3963377774f * c[1] + 0.2158037573f * c[2];
        float m_ = c[0] - 0.1055613458f * c[1] - 0.0638541728f * c[2];
        float s_ = c[0] - 0.0894841775f * c[1] - 1.2914855480f * c[2];

        float l = l_ * l_ * l_;
        float m = m_ * m_ * m_;
        float s = s_ * s_ * s_;

        setRed(+4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s);
        setGreen(-1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s);
        setBlue(-0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s);
    }

    public MutableColor copy() {
        return new MutableColor(r, g, b, a);
    }

    @Override
    public int resolve(LightDarkMode lightDarkMode) {
        return toArgb32();
    }
}
