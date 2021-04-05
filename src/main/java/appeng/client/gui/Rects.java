package appeng.client.gui;

import net.minecraft.client.renderer.Rectangle2d;

/**
 * Utility class for dealing with immutable {@link Rectangle2d}.
 */
public final class Rects {

    public static final Rectangle2d ZERO = new Rectangle2d(0, 0, 0, 0);

    private Rects() {
    }

    public static Rectangle2d expand(Rectangle2d rect, int amount) {
        return new Rectangle2d(
                rect.getX() - amount,
                rect.getY() - amount,
                rect.getWidth() + 2 * amount,
                rect.getHeight() + 2 * amount);
    }

    public static Rectangle2d move(Rectangle2d rect, int x, int y) {
        return new Rectangle2d(
                rect.getX() + x,
                rect.getY() + y,
                rect.getWidth(),
                rect.getHeight());
    }

}
