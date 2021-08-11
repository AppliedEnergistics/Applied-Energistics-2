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

package appeng.items.storage;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.items.IItemHandler;

import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.definitions.AEItems;
import appeng.helpers.FluidCellConfig;
import appeng.util.InventoryAdaptor;

/**
 * @author DrummerMC
 * @version rv6 - 2018-01-17
 * @since rv6 2018-01-17
 */
public final class BasicFluidStorageCell extends AbstractStorageCell<IAEFluidStack> {

    private final int bytesPerType;
    private final double idleDrain;

    public BasicFluidStorageCell(Item.Properties props, ItemLike coreItem, int kilobytes, float idleDrain,
            int bytesPerType) {
        super(props, coreItem, kilobytes);
        this.idleDrain = idleDrain;
        this.bytesPerType = bytesPerType;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return this.bytesPerType;
    }

    @Override
    public double getIdleDrain() {
        return this.idleDrain;
    }

    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return StorageChannels.fluids();
    }

    @Override
    public int getTotalTypes(final ItemStack cellItem) {
        return 5;
    }

    @Override
    public IItemHandler getConfigInventory(final ItemStack is) {
        return new FluidCellConfig(is);
    }

    @Override
    protected void dropEmptyStorageCellCase(final InventoryAdaptor ia, final Player player) {
        final ItemStack extraA = ia.addItems(AEItems.EMPTY_STORAGE_CELL.stack());
        if (!extraA.isEmpty()) {
            player.drop(extraA, false);
        }
    }
}
