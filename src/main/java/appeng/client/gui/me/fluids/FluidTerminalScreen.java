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

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import appeng.api.storage.data.AEFluidKey;
import appeng.client.gui.me.common.MEMonitorableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.core.AELog;
import appeng.helpers.InventoryAction;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.fluids.FluidTerminalMenu;
import appeng.util.prioritylist.IPartitionList;

public class FluidTerminalScreen<C extends FluidTerminalMenu> extends MEMonitorableScreen<AEFluidKey, C> {

    public FluidTerminalScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Override
    protected IPartitionList createPartitionList(List<ItemStack> viewCells) {
        return null;
    }

    @Override
    protected void handleGridInventoryEntryMouseClick(@Nullable GridInventoryEntry entry,
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
