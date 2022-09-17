/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.core.features.registries.cell;


import appeng.api.AEApi;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.items.storage.ItemCreativeStorageCell;
import appeng.me.storage.CreativeCellInventory;
import net.minecraft.item.ItemStack;


public final class CreativeCellHandler implements ICellHandler {

    @Override
    public boolean isCell(final ItemStack is) {
        return !is.isEmpty() && is.getItem() instanceof ItemCreativeStorageCell;
    }

    @Override
    public ICellInventoryHandler getCellInventory(final ItemStack is, final ISaveProvider container, final IStorageChannel channel) {
        if (channel == AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class) && !is.isEmpty() && is
                .getItem() instanceof ItemCreativeStorageCell) {
            return CreativeCellInventory.getCell(is);
        }
        return null;
    }

    @Override
    public int getStatusForCell(final ItemStack is, final ICellInventoryHandler handler) {
        return 2;
    }

    @Override
    public double cellIdleDrain(final ItemStack is, final ICellInventoryHandler handler) {
        return 0;
    }
}
