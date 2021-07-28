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

package appeng.client.gui.widgets;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Rects;
import appeng.client.gui.Tooltip;
import appeng.client.gui.style.Blitter;
import appeng.container.slot.AppEngSlot;

/**
 * A panel that can draw a dynamic number of upgrade slots in a vertical layout.
 */
public final class UpgradesPanel implements ICompositeWidget {

    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 7;
    private static final int MAX_ROWS = 8;

    private static final Blitter BACKGROUND = Blitter.texture("guis/extra_panels.png", 128, 128);
    private static final Blitter INNER_CORNER = BACKGROUND.copy().src(12, 33, SLOT_SIZE, SLOT_SIZE);

    private final List<Slot> slots;

    // The screen origin in window space (used to layout slots)
    private Point screenOrigin = Point.ZERO;

    // Relative to current screen origin (not window)
    private int x;
    private int y;

    private final Supplier<List<Component>> tooltipSupplier;

    public UpgradesPanel(List<Slot> slots) {
        this(slots, Collections::emptyList);
    }

    public UpgradesPanel(List<Slot> slots, Supplier<List<Component>> tooltipSupplier) {
        this.slots = slots;
        this.tooltipSupplier = tooltipSupplier;
    }

    @Override
    public void setPosition(Point position) {
        x = position.getX();
        y = position.getY();
    }

    /**
     * Changes where the panel is positioned. Coordinates are relative to the current screen's origin.
     */
    @Override
    public void setSize(int width, int height) {
        // Size of upgrades panel cannot be set via JSON
    }

    /**
     * The overall bounding box in screen coordinates.
     */
    @Override
    public Rect2i getBounds() {
        int slotCount = getUpgradeSlotCount();

        int height = 2 * PADDING + Math.min(MAX_ROWS, slotCount) * SLOT_SIZE;
        int width = 2 * PADDING + (slotCount + MAX_ROWS - 1) / MAX_ROWS * SLOT_SIZE;
        return new Rect2i(x, y, width, height);
    }

    @Override
    public void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
        this.screenOrigin = Point.fromTopLeft(bounds);
    }

    @Override
    public void updateBeforeRender() {
        int slotOriginX = this.x + PADDING;
        int slotOriginY = this.y + PADDING;

        for (Slot slot : slots) {
            if (!slot.isActive()) {
                continue;
            }

            slot.x = slotOriginX + 1;
            slot.y = slotOriginY + 1;
            slotOriginY += SLOT_SIZE;
        }
    }

    @Override
    public void drawBackgroundLayer(PoseStack matrices, int zIndex, Rect2i bounds, Point mouse) {
        int slotCount = getUpgradeSlotCount();
        if (slotCount <= 0) {
            return;
        }

        // This is the absolute x,y coord of the first slot within the panel
        int slotOriginX = screenOrigin.getX() + this.x + PADDING;
        int slotOriginY = screenOrigin.getY() + this.y + PADDING;

        for (int i = 0; i < slotCount; i++) {
            // Unlike other UIs, this is drawn top-to-bottom,left-to-right
            int row = i % MAX_ROWS;
            int col = i / MAX_ROWS;

            int x = slotOriginX + col * SLOT_SIZE;
            int y = slotOriginY + row * SLOT_SIZE;

            boolean borderLeft = col == 0;
            boolean borderTop = row == 0;
            // The panel can have a "jagged" edge if the number of slots is not divisible by MAX_ROWS
            boolean lastSlot = i + 1 >= slotCount;
            boolean lastRow = row + 1 >= MAX_ROWS;
            boolean borderBottom = lastRow || lastSlot;
            boolean borderRight = i >= slotCount - MAX_ROWS;

            drawSlot(matrices, zIndex, x, y, borderLeft, borderTop, borderRight, borderBottom);

            // Cover up the inner corner when we just drew a rather ugly "inner corner"
            if (col > 0 && lastSlot && !lastRow) {
                INNER_CORNER.dest(x, y + SLOT_SIZE).blit(matrices, zIndex);
            }
        }
    }

    @Override
    public void addExclusionZones(List<Rect2i> exclusionZones, Rect2i screenBounds) {
        int offsetX = screenBounds.getX();
        int offsetY = screenBounds.getY();

        int slotCount = getUpgradeSlotCount();

        // Use a bit of a margin around the zone to avoid things looking too cramped
        final int margin = 2;

        // Add a single bounding rectangle for as many columns as are fully populated
        int fullCols = slotCount / MAX_ROWS;
        int rightEdge = offsetX + x;
        if (fullCols > 0) {
            int fullColWidth = PADDING * 2 + fullCols * SLOT_SIZE;
            exclusionZones.add(Rects.expand(new Rect2i(
                    rightEdge,
                    offsetY + y,
                    fullColWidth,
                    PADDING * 2 + MAX_ROWS * SLOT_SIZE), margin));
            rightEdge += fullColWidth;
        }

        // If there's a partially populated row at the end, add a smaller rectangle for it
        int remaining = slotCount - fullCols * MAX_ROWS;
        if (remaining > 0) {
            exclusionZones.add(Rects.expand(new Rect2i(
                    rightEdge,
                    offsetY + y,
                    // We need to add padding in case there's no full column that already includes it
                    SLOT_SIZE + (fullCols > 0 ? 0 : PADDING * 2),
                    PADDING * 2 + remaining * SLOT_SIZE), margin));
        }

    }

    @Nullable
    @Override
    public Tooltip getTooltip(int mouseX, int mouseY) {
        if (getUpgradeSlotCount() == 0) {
            return null;
        }

        List<Component> tooltip = this.tooltipSupplier.get();
        if (tooltip.isEmpty()) {
            return null;
        }

        return new Tooltip(tooltip);
    }

    private static void drawSlot(PoseStack matrices, int zIndex, int x, int y,
            boolean borderLeft, boolean borderTop, boolean borderRight, boolean borderBottom) {
        int srcX = PADDING;
        int srcY = PADDING;
        int srcWidth = SLOT_SIZE;
        int srcHeight = SLOT_SIZE;

        if (borderLeft) {
            x -= PADDING;
            srcX = 0;
            srcWidth += PADDING;
        }
        if (borderRight) {
            srcWidth += PADDING;
        }
        if (borderTop) {
            y -= PADDING;
            srcY = 0;
            srcHeight += PADDING;
        }
        if (borderBottom) {
            srcHeight += PADDING;
        }

        BACKGROUND.src(srcX, srcY, srcWidth, srcHeight)
                .dest(x, y)
                .blit(matrices, zIndex);
    }

    /**
     * We need this function since the cell workbench can dynamically change how many upgrade slots are active based on
     * the cell in the workbench.
     */
    private int getUpgradeSlotCount() {
        int count = 0;
        for (Slot slot : slots) {
            if (slot instanceof AppEngSlot && ((AppEngSlot) slot).isSlotEnabled()) {
                count++;
            }
        }
        return count;
    }

}
