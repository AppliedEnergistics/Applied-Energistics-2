/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.client.gui.me.fluids;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.me.common.MEMonitorableScreen;
import appeng.client.gui.me.common.Repo;
import appeng.client.gui.style.FluidBlitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.IScrollSource;
import appeng.core.AELog;
import appeng.helpers.InventoryAction;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.fluids.FluidTerminalMenu;
import appeng.util.Platform;
import appeng.util.prioritylist.IPartitionList;

public class FluidTerminalScreen extends MEMonitorableScreen<IAEFluidStack, FluidTerminalMenu> {

    public FluidTerminalScreen(FluidTerminalMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Override
    protected Repo<IAEFluidStack> createRepo(IScrollSource scrollSource) {
        return new FluidRepo(scrollSource, this);
    }

    @Override
    protected IPartitionList<IAEFluidStack> createPartitionList(List<ItemStack> viewCells) {
        return null;
    }

    @Override
    protected void renderGridInventoryEntry(PoseStack poseStack, int x, int y,
            GridInventoryEntry<IAEFluidStack> entry) {
        IAEFluidStack fs = entry.getStack();
        FluidBlitter.create(fs.getFluidStack())
                .dest(x, y, 16, 16)
                .blit(poseStack, getBlitOffset());
    }

    @Override
    protected void renderGridInventoryEntryTooltip(PoseStack poseStack, GridInventoryEntry<IAEFluidStack> entry, int x,
            int y) {
        IAEFluidStack fluidStack = entry.getStack();
        String formattedAmount = NumberFormat.getNumberInstance(Locale.US)
                .format(entry.getStoredAmount() / 1000.0) + " B";

        String modName = Platform.getModName(Platform.getModId(fluidStack));

        List<Component> list = new ArrayList<>();
        list.add(fluidStack.getFluidStack().getDisplayName());
        list.add(new TextComponent(formattedAmount));
        list.add(new TextComponent(modName));

        this.renderComponentToolTip(poseStack, list, x, y, this.font);
    }

    @Override
    protected void handleGridInventoryEntryMouseClick(@Nullable GridInventoryEntry<IAEFluidStack> entry,
            int mouseButton, ClickType clickType) {
        if (clickType == ClickType.PICKUP) {
            // TODO: Allow more options
            if (mouseButton == 0 && entry != null) {
                AELog.debug("mouse0 GUI STACK SIZE %s", entry.getStoredAmount());
                menu.handleInteraction(entry.getSerial(), InventoryAction.FILL_ITEM);
            } else {
                if (entry != null) {
                    AELog.debug("mouse1 GUI STACK SIZE %s", entry.getStoredAmount());
                }
                menu.handleInteraction(-1, InventoryAction.EMPTY_ITEM);
            }
        }
    }

}
