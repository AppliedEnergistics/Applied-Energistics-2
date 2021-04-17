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

import com.mojang.blaze3d.matrix.MatrixStack;

import org.apache.commons.lang3.time.DurationFormatUtils;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Blitter;
import appeng.client.gui.widgets.Scrollbar;
import appeng.container.me.crafting.CraftingCPUContainer;
import appeng.container.me.crafting.CraftingStatus;
import appeng.container.me.crafting.CraftingStatusEntry;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;

/**
 * This screen shows the current crafting job that a crafting CPU is working on (if any).
 */
public class CraftingCPUScreen<T extends CraftingCPUContainer> extends AEBaseScreen<T> {
    private static final Blitter BACKGROUND = Blitter.texture("guis/craftingcpu.png").src(0, 0, 238, 184);

    private final CraftingStatusTableRenderer table;

    private Button cancel;

    private CraftingStatus status;

    public CraftingCPUScreen(T container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title, BACKGROUND);

        this.table = new CraftingStatusTableRenderer(this, 9, 19);

        this.setScrollBar(new Scrollbar().setTop(19).setLeft(218).setHeight(137));
    }

    private void cancel() {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("TileCrafting.Cancel", "Cancel"));
    }

    @Override
    public void init() {
        super.init();

        this.cancel = new Button(this.guiLeft + 163, this.guiTop + this.ySize - 25,
                50, 20, GuiText.Cancel.text(), btn -> cancel());
        this.addButton(this.cancel);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        final int size = this.status != null ? this.status.getEntries().size() : 0;
        this.getScrollBar().setRange(0, CraftingStatusTableRenderer.getScrollableRows(size), 1);
    }

    private List<CraftingStatusEntry> getVisualEntries() {
        return this.status != null ? status.getEntries() : Collections.emptyList();
    }

    @Override
    public void render(MatrixStack matrixStack, final int mouseX, final int mouseY, final float btn) {
        this.cancel.active = !getVisualEntries().isEmpty();

        super.render(matrixStack, mouseX, mouseY, btn);
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        ITextComponent title = this.getGuiDisplayName(GuiText.CraftingStatus.text());

        if (status != null) {
            this.table.render(matrixStack, mouseX, mouseY, status.getEntries(), getScrollBar().getCurrentScroll());

            final long elapsedTime = status.getElapsedTime();
            final double remainingItems = status.getRemainingItemCount();
            final double startItems = status.getStartItemCount();
            final long eta = (long) (elapsedTime / Math.max(1d, (startItems - remainingItems))
                    * remainingItems);

            if (eta > 0 && !getVisualEntries().isEmpty()) {
                final long etaInMilliseconds = TimeUnit.MILLISECONDS.convert(eta, TimeUnit.NANOSECONDS);
                final String etaTimeText = DurationFormatUtils.formatDuration(etaInMilliseconds,
                        GuiText.ETAFormat.getLocal());
                title = title.deepCopy().appendString(" - " + etaTimeText);
            }
        }

        setTextContent(TEXT_ID_DIALOG_TITLE, title);
    }

    public void postUpdate(CraftingStatus status) {
        if (this.status == null || status.isFullStatus()) {
            this.status = status;
        } else {
            Map<Long, CraftingStatusEntry> entries = new LinkedHashMap<>(this.status.getEntries().size());
            for (CraftingStatusEntry entry : this.status.getEntries()) {
                entries.put(entry.getSerial(), entry);
            }

            for (CraftingStatusEntry entry : status.getEntries()) {
                CraftingStatusEntry existingEntry = entries.get(entry.getSerial());
                if (existingEntry != null) {
                    entries.put(entry.getSerial(), new CraftingStatusEntry(
                            existingEntry.getSerial(),
                            existingEntry.getItem(),
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

}
