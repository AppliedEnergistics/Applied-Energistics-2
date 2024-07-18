package appeng.client.gui.style;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Generates a background of arbitrary size by tiling a pre-defined background.
 */
public final class BackgroundGenerator {

    private static final int BORDER = 4;

    private static final int SIZE = 256;
    private static final int TILED_SIZE = SIZE - 2 * BORDER;
    private static final Blitter FULL = Blitter.texture("guis/background.png", SIZE, SIZE);

    private static final Blitter TOP_LEFT = FULL.copy().src(0, 0, BORDER, BORDER);

    private static final Blitter TOP_MIDDLE = FULL.copy().src(BORDER, 0, TILED_SIZE, BORDER);

    private static final Blitter TOP_RIGHT = FULL.copy().src(SIZE - BORDER, 0, BORDER, BORDER);

    private static final Blitter LEFT = FULL.copy().src(0, BORDER, BORDER, TILED_SIZE);

    private static final Blitter MIDDLE = FULL.copy().src(BORDER, BORDER, TILED_SIZE, TILED_SIZE);

    private static final Blitter RIGHT = FULL.copy().src(SIZE - BORDER, BORDER, BORDER, TILED_SIZE);

    private static final Blitter BOTTOM_LEFT = FULL.copy().src(0, SIZE - BORDER, BORDER, BORDER);

    private static final Blitter BOTTOM_MIDDLE = FULL.copy().src(BORDER, SIZE - BORDER, TILED_SIZE, BORDER);

    private static final Blitter BOTTOM_RIGHT = FULL.copy().src(SIZE - BORDER, SIZE - BORDER, BORDER, BORDER);

    private BackgroundGenerator() {
    }

    public static void draw(int width, int height, GuiGraphics guiGraphics, int x, int y) {
        if (width < 2 * BORDER || height < 2 * BORDER) {
            return;
        }

        var right = x + width;
        var bottom = y + height;

        // Corners first
        TOP_LEFT.dest(x, y).blit(guiGraphics);
        TOP_RIGHT.dest(right - BORDER, y).blit(guiGraphics);
        BOTTOM_LEFT.dest(x, bottom - BORDER).blit(guiGraphics);
        BOTTOM_RIGHT.dest(right - BORDER, bottom - BORDER).blit(guiGraphics);

        var innerWidth = width - 2 * BORDER;
        var innerHeight = height - 2 * BORDER;

        // Horizontally tiled
        for (var cx = 0; cx < innerWidth; cx += TILED_SIZE) {
            var tileWidth = Math.min(TILED_SIZE, innerWidth - cx);
            TOP_MIDDLE.copy()
                    .srcWidth(tileWidth)
                    .dest(x + BORDER + cx, y)
                    .blit(guiGraphics);
            BOTTOM_MIDDLE.copy()
                    .srcWidth(tileWidth)
                    .dest(x + BORDER + cx, y + height - BORDER)
                    .blit(guiGraphics);

            // Both horziontally and vertically tiled
            for (var cy = 0; cy < innerHeight; cy += TILED_SIZE) {
                var tileHeight = Math.min(TILED_SIZE, innerHeight - cy);
                MIDDLE.copy()
                        .srcWidth(tileWidth)
                        .srcHeight(tileHeight)
                        .dest(x + BORDER + cx, y + BORDER + cy)
                        .blit(guiGraphics);
            }
        }

        // Vertically tiled
        for (var cy = 0; cy < innerHeight; cy += TILED_SIZE) {
            var tileHeight = Math.min(TILED_SIZE, innerHeight - cy);
            LEFT.copy()
                    .srcHeight(tileHeight)
                    .dest(x, y + BORDER + cy)
                    .blit(guiGraphics);
            RIGHT.copy()
                    .srcHeight(tileHeight)
                    .dest(right - BORDER, y + BORDER + cy)
                    .blit(guiGraphics);
        }
    }

}
