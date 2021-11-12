/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import org.apache.commons.lang3.time.DurationFormatUtils;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.localization.GuiText;
import appeng.menu.me.crafting.CraftingCPUMenu;
import appeng.menu.me.crafting.CraftingStatus;
import appeng.menu.me.crafting.CraftingStatusEntry;

/**
 * This screen shows the current crafting job that a crafting CPU is working on (if any).
 */
public class CraftingCPUScreen<T extends CraftingCPUMenu> extends AEBaseScreen<T> {

    private final CraftingStatusTableRenderer table;

    private final Button cancel;

    private final Scrollbar scrollbar;

    private CraftingStatus status;

    public CraftingCPUScreen(T menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.table = new CraftingStatusTableRenderer(this, 9, 19);

        this.scrollbar = widgets.addScrollBar("scrollbar");

        this.cancel = this.widgets.addButton("cancel", GuiText.Cancel.text(), menu::cancelCrafting);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        // Update the dialog title with an ETA if possible
        Component title = this.getGuiDisplayName(GuiText.CraftingStatus.text());
        if (status != null) {
            final long elapsedTime = status.getElapsedTime();
            final double remainingItems = status.getRemainingItemCount();
            final double startItems = status.getStartItemCount();
            final long eta = (long) (elapsedTime / Math.max(1d, startItems - remainingItems)
                    * remainingItems);

            if (eta > 0 && !getVisualEntries().isEmpty()) {
                final long etaInMilliseconds = TimeUnit.MILLISECONDS.convert(eta, TimeUnit.NANOSECONDS);
                final String etaTimeText = DurationFormatUtils.formatDuration(etaInMilliseconds,
                        GuiText.ETAFormat.getLocal());
                title = title.copy().append(" - " + etaTimeText);
            }
        }
        setTextContent(TEXT_ID_DIALOG_TITLE, title);

        final int size = this.status != null ? this.status.getEntries().size() : 0;
        scrollbar.setRange(0, CraftingStatusTableRenderer.getScrollableRows(size), 1);
    }

    private List<CraftingStatusEntry> getVisualEntries() {
        return this.status != null ? status.getEntries() : Collections.emptyList();
    }

    @Override
    public void render(PoseStack poseStack, final int mouseX, final int mouseY, final float btn) {
        this.cancel.active = !getVisualEntries().isEmpty();

        super.render(poseStack, mouseX, mouseY, btn);
    }

    @Override
    public void drawFG(PoseStack poseStack, int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(poseStack, offsetX, offsetY, mouseX, mouseY);

        if (status != null) {
            this.table.render(poseStack, mouseX, mouseY, status.getEntries(), scrollbar.getCurrentScroll());
        }
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(double mouseX, double mouseY) {
        IAEStack hovered = table.getHoveredStack();
        if (hovered instanceof IAEItemStack) {
            return ((IAEItemStack) hovered).getDefinition();
        } else if (hovered instanceof IAEFluidStack) {
            return ((IAEFluidStack) hovered).getFluidStack();
        }
        return null;
    }

    public void postUpdate(CraftingStatus status) {
        Map<Long, CraftingStatusEntry> entries;
        if (this.status == null || status.isFullStatus()) {
            // Start from scratch.
            // We can't just reuse the status because we have to filter out deleted entries.
            entries = new LinkedHashMap<>();
        } else {
            // Merge the status entries.
            entries = new LinkedHashMap<>(this.status.getEntries().size());
            for (CraftingStatusEntry entry : this.status.getEntries()) {
                entries.put(entry.getSerial(), entry);
            }
        }

        for (CraftingStatusEntry entry : status.getEntries()) {
            if (entry.isDeleted()) {
                entries.remove(entry.getSerial());
                continue;
            }

            CraftingStatusEntry existingEntry = entries.get(entry.getSerial());
            if (existingEntry != null) {
                entries.put(entry.getSerial(), new CraftingStatusEntry(
                        existingEntry.getSerial(),
                        existingEntry.getStack(),
                        entry.getStoredAmount(),
                        entry.getActiveAmount(),
                        entry.getPendingAmount()));
            } else {
                entries.put(entry.getSerial(), entry);
            }
        }

        List<CraftingStatusEntry> sortedEntries = new ArrayList<>(entries.values());
        this.status = new CraftingStatus(
                true,
                status.getElapsedTime(),
                status.getRemainingItemCount(),
                status.getStartItemCount(),
                sortedEntries);
    }

}
