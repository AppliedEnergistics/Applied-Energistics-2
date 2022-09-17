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

package appeng.items.contents;


import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.networking.IGridHost;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;


public class NetworkToolViewer implements INetworkTool, IAEAppEngInventory {

    private final AppEngInternalInventory inv;
    private final ItemStack is;
    private final IGridHost gh;

    public NetworkToolViewer(final ItemStack is, final IGridHost gHost) {
        this.is = is;
        this.gh = gHost;
        this.inv = new AppEngInternalInventory(this, 9);
        this.inv.setFilter(new NetworkToolInventoryFilter());
        if (is.hasTagCompound()) // prevent crash when opening network status screen.
        {
            this.inv.readFromNBT(Platform.openNbtData(is), "inv");
        }
    }

    @Override
    public void saveChanges() {
        this.inv.writeToNBT(Platform.openNbtData(this.is), "inv");
    }

    @Override
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {
    }

    @Override
    public ItemStack getItemStack() {
        return this.is;
    }

    @Override
    public IGridHost getGridHost() {
        return this.gh;
    }

    private static class NetworkToolInventoryFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(IItemHandler inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(IItemHandler inv, int slot, ItemStack stack) {
            return stack.getItem() instanceof IUpgradeModule && ((IUpgradeModule) stack.getItem()).getType(stack) != null;
        }
    }

    public IItemHandler getInternalInventory() {
        return this.inv;
    }

    @Override
    public IItemHandler getInventory() {
        return this.inv;
    }
}
