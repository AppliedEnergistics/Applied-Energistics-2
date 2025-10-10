package appeng.client.gui.widgets;

import net.minecraft.resources.ResourceLocation;

import appeng.client.gui.style.Blitter;
import appeng.core.AppEng;

public enum ModifyIcon {

    MULTIPLY_2(192, 224),
    MULTIPLY_3(208, 224),
    MULTIPLY_8(224, 224),
    DIVISION_2(192, 240),
    DIVISION_3(208, 240),
    DIVISION_8(224, 240),;

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private static final ResourceLocation TEXTURE = AppEng.makeId("textures/guis/states.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    ModifyIcon(int x, int y) {
        this.x = x;
        this.y = y;
        width = 16;
        height = 16;
    }

    public Blitter getBlitter() {
        return Blitter.texture(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT).src(x, y, width, height);
    }
}
