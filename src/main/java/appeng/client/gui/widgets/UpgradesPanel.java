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

import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.Upgrades;
import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Rects;
import appeng.client.gui.Tooltip;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A panel that can draw a dynamic number of upgrade slots in a vertical layout.
 */
public final class UpgradesPanel implements ICompositeWidget {

    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 3;
    private static final int MAX_ROWS = 8;

    private final List<Slot> slots;

    // Relative to current screen origin (not window)
    private int x;
    private int y;

    private final Supplier<List<Component>> tooltipSupplier;

    public UpgradesPanel(List<Slot> slots) {
        this(slots, Collections::emptyList);
    }

    public UpgradesPanel(List<Slot> slots, IUpgradeableObject upgradeableObject) {
        this(slots, () -> Upgrades.getTooltipLinesForMachine(upgradeableObject.getUpgrades().getUpgradableItem()));
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
    public void updateBeforeRender() {
        int slotOriginX = this.x;
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
    public void addBackgroundPanels(PanelBlitter panels, Rect2i screenBounds) {
        visitBackgroundPanels(panels::addBounds);
    }

    @Override
    public void addExclusionZones(List<Rect2i> exclusionZones, Rect2i screenBounds) {
        visitBackgroundPanels(rect -> exclusionZones.add(Rects.move(rect, screenBounds.getX(), screenBounds.getY())));
    }

    private void visitBackgroundPanels(Consumer<Rect2i> visitor) {
        int slotCount = getUpgradeSlotCount();

        // Add a single bounding rectangle for as many columns as are fully populated
        int fullCols = slotCount / MAX_ROWS;
        int rightEdge = x;
        if (fullCols > 0) {
            int fullColWidth = PADDING * 2 + fullCols * SLOT_SIZE;
            visitor.accept(new Rect2i(
                    rightEdge,
                    y,
                    fullColWidth,
                    PADDING * 2 + MAX_ROWS * SLOT_SIZE));
            rightEdge += fullColWidth;
        }

        // If there's a partially populated row at the end, add a smaller rectangle for it
        int remaining = slotCount - fullCols * MAX_ROWS;
        if (remaining > 0) {
            visitor.accept(new Rect2i(
                    rightEdge,
                    y,
                    // We need to add padding in case there's no full column that already includes it
                    SLOT_SIZE + (fullCols > 0 ? 0 : PADDING * 2),
                    PADDING * 2 + remaining * SLOT_SIZE));
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
