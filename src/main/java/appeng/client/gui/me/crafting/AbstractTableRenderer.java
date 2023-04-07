/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.client.gui.me.crafting;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.StackWithBounds;
import appeng.client.gui.style.PaletteColor;

/**
 * Renders a 3x5 table where each cell displays an item and some text next to it.
 */
public abstract class AbstractTableRenderer<T> {

    private static final int CELL_WIDTH = 67;
    private static final int CELL_HEIGHT = 22;

    private static final int ROWS = 5;
    private static final int COLS = 3;

    // This border is only shown in-between cells, not around
    private static final int CELL_BORDER = 1;

    private static final int LINE_SPACING = 1;

    private static final float TEXT_SCALE = 0.5f;
    private static final float INV_TEXT_SCALE = 2.0f;

    protected final AEBaseScreen<?> screen;
    private final Font fontRenderer;
    private final float lineHeight;
    private final int x;
    private final int y;
    private StackWithBounds hoveredStack;

    public AbstractTableRenderer(AEBaseScreen<?> screen, int x, int y) {
        this.screen = screen;
        this.x = x;
        this.y = y;
        this.fontRenderer = Minecraft.getInstance().font;
        this.lineHeight = this.fontRenderer.lineHeight * TEXT_SCALE;
    }

    public final void render(PoseStack poseStack, int mouseX, int mouseY, List<T> entries, int scrollOffset) {
        mouseX -= screen.getGuiLeft();
        mouseY -= screen.getGuiTop();

        final int textColor = screen.getStyle().getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();
        List<Component> tooltipLines = null;
        StackWithBounds hovered = null;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int i = (row + scrollOffset) * COLS + col;
                if (i >= entries.size()) {
                    break;
                }

                T entry = entries.get(i);

                int cellX = x + col * (CELL_WIDTH + CELL_BORDER);
                int cellY = y + row * (CELL_HEIGHT + CELL_BORDER);

                int background = getEntryBackgroundColor(entry);
                if (background != 0) {
                    GuiComponent.fill(poseStack, cellX, cellY, cellX + CELL_WIDTH, cellY + CELL_HEIGHT, background);
                }

                List<Component> lines = getEntryDescription(entry);

                // Compute the full height of the text block to center it vertically
                float textHeight = lines.size() * lineHeight;
                if (lines.size() > 1) {
                    textHeight += (lines.size() - 1) * LINE_SPACING;
                }
                float textY = Math.round(cellY + (CELL_HEIGHT - textHeight) / 2.0f);

                // Position the item at the right side of the cell with a 3px margin
                int itemX = cellX + CELL_WIDTH - 19;

                poseStack.pushPose();
                poseStack.scale(TEXT_SCALE, TEXT_SCALE, 1.0f);
                for (Component line : lines) {
                    final int w = fontRenderer.width(line);
                    fontRenderer.draw(poseStack, line,
                            (int) ((itemX - 2 - w * TEXT_SCALE) * INV_TEXT_SCALE),
                            textY * INV_TEXT_SCALE, textColor);
                    textY += lineHeight + LINE_SPACING;
                }
                poseStack.popPose();

                var entryStack = getEntryStack(entry);

                int itemY = cellY + (CELL_HEIGHT - 16) / 2;
                AEKeyRendering.drawInGui(Minecraft.getInstance(), poseStack, itemX, itemY, entryStack);

                int overlay = getEntryOverlayColor(entry);
                if (overlay != 0) {
                    GuiComponent.fill(poseStack, cellX, cellY, cellX + CELL_WIDTH, cellY + CELL_HEIGHT, overlay);
                }

                if (mouseX >= cellX && mouseX <= cellX + CELL_WIDTH
                        && mouseY >= cellY && mouseY <= cellY + CELL_HEIGHT) {
                    tooltipLines = getEntryTooltip(entry);
                    hovered = new StackWithBounds(
                            new GenericStack(entryStack, 0),
                            new Rect2i(screen.getGuiLeft() + cellX, screen.getGuiTop() + cellY, CELL_WIDTH,
                                    CELL_HEIGHT));
                }
            }
        }
        hoveredStack = hovered;

        if (tooltipLines != null) {
            screen.drawTooltipWithHeader(poseStack, mouseX, mouseY, tooltipLines);
        }
    }

    public StackWithBounds getHoveredStack() {
        return hoveredStack;
    }

    /**
     * @return The number of rows that are off-screen given a number of entries.
     */
    public static int getScrollableRows(int size) {
        return (size + COLS - 1) / COLS - ROWS;
    }

    /**
     * Implement in subclass to determine the text to show next to an entry.
     */
    protected abstract List<Component> getEntryDescription(T entry);

    /**
     * Get the item to show for an entry.
     */
    protected abstract AEKey getEntryStack(T entry);

    /**
     * Get the tooltip lines to show for an entry.
     */
    protected abstract List<Component> getEntryTooltip(T entry);

    /**
     * Override and return a color to draw a colored rectangle behind an entry. Return 0 to not draw a rectangle.
     */
    protected int getEntryBackgroundColor(T entry) {
        return 0;
    }

    /**
     * Override and return a color to draw a colored rectangle above an entry. Return 0 to not draw a rectangle.
     */
    protected int getEntryOverlayColor(T entry) {
        return 0;
    }

}
