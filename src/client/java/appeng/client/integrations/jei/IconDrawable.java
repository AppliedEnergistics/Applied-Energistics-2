package appeng.client.integrations.jei;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import mezz.jei.api.gui.drawable.IDrawable;

import appeng.client.gui.style.Blitter;
import appeng.util.Icon;

/**
 * Creates {@link IDrawable} from {@link Icon}
 */
final class IconDrawable implements IDrawable {
    private final Blitter blitter;
    private final int x;
    private final int y;

    IconDrawable(Icon icon, int x, int y) {
        this.blitter = Blitter.icon(icon);
        this.x = x;
        this.y = y;
    }

    IconDrawable(Icon icon) {
        this(icon, 0, 0);
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
    public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
        blitter.dest(x + xOffset, y + yOffset).blit(guiGraphics);
    }
}
