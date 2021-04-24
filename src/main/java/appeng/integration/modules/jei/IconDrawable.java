package appeng.integration.modules.jei;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.gui.drawable.IDrawable;

/**
 * Creates {@link IDrawable} from {@link appeng.client.gui.Icon}
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
    public void draw(MatrixStack matrixStack, int xOffset, int yOffset) {
        blitter.dest(x + xOffset, y + yOffset).blit(matrixStack, 0);
    }
}
