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

package appeng.fluids.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.items.IItemHandler;

import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.Api;
import appeng.core.api.definitions.ApiItems;
import appeng.fluids.helper.FluidCellConfig;
import appeng.items.storage.AbstractStorageCell;
import appeng.util.InventoryAdaptor;

/**
 * @author DrummerMC
 * @version rv6 - 2018-01-17
 * @since rv6 2018-01-17
 */
public final class BasicFluidStorageCell extends AbstractStorageCell<IAEFluidStack> {

    private final int bytesPerType;
    private final double idleDrain;

    public BasicFluidStorageCell(Properties props, IItemProvider coreItem, int kilobytes, float idleDrain,
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
        return Api.instance().storage().getStorageChannel(IFluidStorageChannel.class);
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
    protected void dropEmptyStorageCellCase(final InventoryAdaptor ia, final PlayerEntity player) {
        final ItemStack extraA = ia.addItems(ApiItems.EMPTY_STORAGE_CELL.stack());
        if (!extraA.isEmpty()) {
            player.dropItem(extraA, false);
        }
    }
}
