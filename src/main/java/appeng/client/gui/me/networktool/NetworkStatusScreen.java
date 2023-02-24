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

package appeng.client.gui.me.networktool;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.client.AEKeyRendering;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.CommonButtons;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.localization.GuiText;
import appeng.menu.me.networktool.MachineGroup;
import appeng.menu.me.networktool.NetworkStatus;
import appeng.menu.me.networktool.NetworkStatusMenu;
import appeng.util.Platform;

public class NetworkStatusScreen extends AEBaseScreen<NetworkStatusMenu> {

    private static final int ROWS = 4;
    private static final int COLUMNS = 5;

    // Origin of the machine table
    private static final int TABLE_X = 14;
    private static final int TABLE_Y = 41;

    // Dimensions of each table cell
    private static final int CELL_WIDTH = 30;
    private static final int CELL_HEIGHT = 18;

    private NetworkStatus status = new NetworkStatus();

    private final Scrollbar scrollbar;

    public NetworkStatusScreen(NetworkStatusMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.scrollbar = widgets.addScrollBar("scrollbar");

        this.addToLeftToolbar(CommonButtons.togglePowerUnit());
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        setTextContent("dialog_title", GuiText.NetworkDetails.text(status.getChannelsUsed()));
        setTextContent("stored_power", GuiText.StoredPower.text(Platform.formatPower(status.getStoredPower(), false)));
        setTextContent("max_power", GuiText.MaxPower.text(Platform.formatPower(status.getMaxStoredPower(), false)));
        setTextContent("power_input_rate",
                GuiText.PowerInputRate.text(Platform.formatPower(status.getAveragePowerInjection(), true)));
        setTextContent("power_usage_rate",
                GuiText.PowerUsageRate.text(Platform.formatPower(status.getAveragePowerUsage(), true)));
        setTextContent("channel_power_rate",
                GuiText.ChannelEnergyDrain.text(Platform.formatPower(status.getChannelPower(), true)));
    }

    @Override
    public void drawFG(PoseStack poseStack, int offsetX, int offsetY, int mouseX,
            int mouseY) {
        int x = 0;
        int y = 0;
        final int viewStart = scrollbar.getCurrentScroll() * COLUMNS;
        final int viewEnd = viewStart + COLUMNS * ROWS;

        List<Component> tooltip = null;
        List<MachineGroup> machines = status.getGroupedMachines();
        for (int i = viewStart; i < Math.min(viewEnd, machines.size()); i++) {
            MachineGroup entry = machines.get(i);

            int cellX = TABLE_X + x * CELL_WIDTH;
            int cellY = TABLE_Y + y * CELL_HEIGHT;

            // Position the item with a margin of 1px to the right of the cell
            int itemX = cellX + CELL_WIDTH - 17;
            int itemY = cellY + 1;

            drawMachineCount(poseStack, itemX, cellY, entry.getCount());

            AEKeyRendering.drawInGui(Minecraft.getInstance(), poseStack, itemX, itemY, entry.getDisplay());

            // Update the tooltip based on the calculated cell rectangle and mouse position
            if (isHovering(cellX, cellY, CELL_WIDTH, CELL_HEIGHT, mouseX, mouseY)) {
                tooltip = new ArrayList<>();
                tooltip.add(entry.getDisplay().getDisplayName());

                tooltip.add(GuiText.Installed.text(entry.getCount()));
                if (entry.getIdlePowerUsage() > 0) {
                    tooltip.add(GuiText.EnergyDrain
                            .text(Platform.formatPower(entry.getIdlePowerUsage(), true)));
                }
            }

            if (++x >= COLUMNS) {
                y++;
                x = 0;
            }
        }

        if (tooltip != null) {
            // We need to relativize the offset because the matrix stack is currently "pushed" to the local coordinates
            this.drawTooltipWithHeader(poseStack, mouseX - offsetX, mouseY - offsetY, tooltip);
        }
    }

    // x,y is upper left corner of related machine icon (which is 16x16)
    private void drawMachineCount(PoseStack poseStack, int x, int y, long count) {
        String str;
        if (count >= 10000) {
            str = Long.toString(count / 1000) + 'k';
        } else {
            str = Long.toString(count);
        }

        // Keep in mind the text will be scaled down 50%
        float textWidth = this.font.width(str) / 2.0f;
        float textHeight = this.font.lineHeight / 2.0f;

        // Draw the count at half-size
        poseStack.pushPose();
        poseStack.translate(
                x - 1 - textWidth,
                y + (CELL_HEIGHT - textHeight) / 2.0f,
                0);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        this.font.draw(poseStack, str, 0, 0,
                style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB());
        poseStack.popPose();
    }

    public void processServerUpdate(NetworkStatus status) {
        this.status = status;
        this.setScrollBar();
    }

    private void setScrollBar() {
        final int size = this.status.getGroupedMachines().size();
        int overflowRows = (size + COLUMNS - 1) / COLUMNS - ROWS;
        scrollbar.setRange(0, overflowRows, 1);
    }

}
