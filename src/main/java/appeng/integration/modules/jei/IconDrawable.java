package appeng.integration.modules.jei;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.gui.drawable.IDrawable;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;

/**
 * Creates {@link IDrawable} from {@link Icon}
 */
final class IconDrawable implements IDrawable {
    private final Blitter blitter;
    private final int x;
    private final int y;

    IconDrawable(Icon icon, int x, int y) {
        this.blitter = icon.getBlitter();
        this.x = x;
        this.y = y;
    }

    @Override
    public int getWidth() {
        return blitter.getSrcWidth();
    }

    @Override
    public int getHeight() {
        return blitter.getSrcHeight();
    }

    @Override
    public void draw(PoseStack poseStack, int xOffset, int yOffset) {
        blitter.dest(x + xOffset, y + yOffset).blit(poseStack);
    }
}
