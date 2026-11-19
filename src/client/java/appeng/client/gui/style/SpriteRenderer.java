/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2026, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.gui.style;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.Identifier;

/**
 * Draws sprites from the GUI sprite atlas. This abstraction exists so that dynamically assembled screen backgrounds can
 * also be drawn outside of the game (i.e. in tests).
 * <p>
 * Implementations are responsible for resolving the sprite for the current UI {@link Theme}.
 */
public interface SpriteRenderer {
    static SpriteRenderer of(GuiGraphicsExtractor guiGraphics) {
        return new GuiGraphicsSpriteRenderer(guiGraphics);
    }

    /**
     * Gets the logical size of a sprite and how it scales.
     */
    SpriteInfo getSprite(Identifier sprite);

    /**
     * Draws a sub-region of a sprite into the destination rectangle. The region is specified in the logical coordinate
     * space of the sprite (see {@link SpriteInfo}).
     *
     * @param tile If true, the region is repeated to fill the destination rectangle, otherwise it is stretched.
     */
    void blitRegion(Identifier sprite,
            int srcX, int srcY, int srcWidth, int srcHeight,
            int x, int y, int width, int height,
            boolean tile,
            int color);

    /**
     * Draws a sprite using its scaling metadata (stretch, tile or nine-slice), just like Vanilla would.
     */
    default void blitSprite(Identifier sprite, int x, int y, int width, int height, int color) {
        if (width <= 0 || height <= 0) {
            return;
        }

        var info = getSprite(sprite);
        switch (info.scaling()) {
            case GuiSpriteScaling.Tile tile -> blitRegion(sprite, 0, 0, tile.width(), tile.height(),
                    x, y, width, height, true, color);
            case GuiSpriteScaling.NineSlice nineSlice -> blitNineSlice(sprite, nineSlice, x, y, width, height,
                    color);
            default -> blitRegion(sprite, 0, 0, info.width(), info.height(), x, y, width, height, false, color);
        }
    }

    /**
     * Draws a sprite at its natural size.
     */
    default void blitSprite(Identifier sprite, int x, int y, int color) {
        var info = getSprite(sprite);
        blitSprite(sprite, x, y, info.width(), info.height(), color);
    }

    private void blitNineSlice(Identifier sprite, GuiSpriteScaling.NineSlice nineSlice, int x, int y, int width,
            int height, int color) {
        var border = nineSlice.border();
        int left = Math.min(border.left(), width / 2);
        int right = Math.min(border.right(), width / 2);
        int top = Math.min(border.top(), height / 2);
        int bottom = Math.min(border.bottom(), height / 2);
        int innerSrcWidth = nineSlice.width() - border.left() - border.right();
        int innerSrcHeight = nineSlice.height() - border.top() - border.bottom();
        int innerWidth = width - left - right;
        int innerHeight = height - top - bottom;
        boolean tile = !nineSlice.stretchInner();
        int srcRight = nineSlice.width() - right;
        int srcBottom = nineSlice.height() - bottom;

        // Corners
        blitRegion(sprite, 0, 0, left, top, x, y, left, top, false, color);
        blitRegion(sprite, srcRight, 0, right, top, x + width - right, y, right, top, false, color);
        blitRegion(sprite, 0, srcBottom, left, bottom, x, y + height - bottom, left, bottom, false, color);
        blitRegion(sprite, srcRight, srcBottom, right, bottom, x + width - right, y + height - bottom, right,
                bottom, false, color);

        // Edges
        blitRegion(sprite, border.left(), 0, innerSrcWidth, top, x + left, y, innerWidth, top, tile, color);
        blitRegion(sprite, border.left(), srcBottom, innerSrcWidth, bottom, x + left, y + height - bottom,
                innerWidth, bottom, tile, color);
        blitRegion(sprite, 0, border.top(), left, innerSrcHeight, x, y + top, left, innerHeight, tile, color);
        blitRegion(sprite, srcRight, border.top(), right, innerSrcHeight, x + width - right, y + top, right,
                innerHeight, tile, color);

        // Center
        blitRegion(sprite, border.left(), border.top(), innerSrcWidth, innerSrcHeight, x + left, y + top,
                innerWidth, innerHeight, tile, color);
    }

    /**
     * @param width   The logical width of the sprite. For nine-slice and tiled sprites, this is the size given in the
     *                scaling metadata, otherwise it is the actual width of the sprite.
     * @param height  The logical height of the sprite. See width.
     * @param scaling How the sprite scales.
     */
    record SpriteInfo(int width, int height, GuiSpriteScaling scaling) {
        public static SpriteInfo of(int textureWidth, int textureHeight, GuiSpriteScaling scaling) {
            return switch (scaling) {
                case GuiSpriteScaling.Tile tile -> new SpriteInfo(tile.width(), tile.height(), scaling);
                case GuiSpriteScaling.NineSlice nineSlice -> new SpriteInfo(nineSlice.width(), nineSlice.height(),
                        scaling);
                default -> new SpriteInfo(textureWidth, textureHeight, scaling);
            };
        }
    }
}
