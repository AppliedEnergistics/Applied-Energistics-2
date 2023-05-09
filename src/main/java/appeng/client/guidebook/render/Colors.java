package appeng.client.guidebook.render;

import net.minecraft.util.FastColor;

final class Colors {
    public static int argb(int a, int r, int g, int b) {
        return FastColor.ARGB32.color(a, r, g, b);
    }

    public static int rgb(int r, int g, int b) {
        return argb(255, r, g, b);
    }

    public static int mono(int w) {
        return rgb(w, w, w);
    }
}
