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

import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.blockentities.InternalInventory;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.blockentity.inventory.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.IAEItemFilter;

public class NetworkToolViewer implements INetworkTool, InternalInventoryHost {

    private final AppEngInternalInventory inv;
    private final ItemStack is;
    private final IInWorldGridNodeHost host;
    private final boolean remote;

    public NetworkToolViewer(final ItemStack is, final IInWorldGridNodeHost host, boolean remote) {
        this.is = is;
        this.host = host;
        this.remote = remote;
        this.inv = new AppEngInternalInventory(this, 9);
        this.inv.setFilter(new NetworkToolInventoryFilter());
        if (is.hasTag()) // prevent crash when opening network status screen.
        {
            this.inv.readFromNBT(is.getOrCreateTag(), "inv");
        }
    }

    @Override
    public void saveChanges() {
        this.inv.writeToNBT(this.is.getOrCreateTag(), "inv");
    }

    @Override
    public void onChangeInventory(Object inv, int slot, InvOperation mc, ItemStack removedStack,
            ItemStack newStack) {
    }

    @Override
    public boolean isRemote() {
        return this.remote;
    }

    @Override
    public ItemStack getItemStack() {
        return this.is;
    }

    @Override
    public IInWorldGridNodeHost getGridHost() {
        return this.host;
    }

    private static class NetworkToolInventoryFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return IUpgradeModule.getTypeFromStack(stack) != null;
        }
    }

    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public InternalInventory getInventory() {
        return this.inv;
    }
}
