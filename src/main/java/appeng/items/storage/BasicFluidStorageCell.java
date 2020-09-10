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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.Api;
import appeng.items.contents.FluidCellConfig;
import appeng.items.materials.MaterialType;
import appeng.util.InventoryAdaptor;

/**
 * @author DrummerMC
 * @version rv6 - 2018-01-17
 * @since rv6 2018-01-17
 */
public final class BasicFluidStorageCell extends AbstractStorageCell<IAEFluidStack> {

    private final int perType;
    private final double idleDrain;

    public BasicFluidStorageCell(Properties props, final MaterialType whichCell, final int kilobytes) {
        super(props, whichCell, kilobytes);
        switch (whichCell) {
            case FLUID_1K_CELL_COMPONENT:
                this.idleDrain = 0.5;
                this.perType = 8;
                break;
            case FLUID_4K_CELL_COMPONENT:
                this.idleDrain = 1.0;
                this.perType = 32;
                break;
            case FLUID_16K_CELL_COMPONENT:
                this.idleDrain = 1.5;
                this.perType = 128;
                break;
            case FLUID_64K_CELL_COMPONENT:
                this.idleDrain = 2.0;
                this.perType = 512;
                break;
            default:
                this.idleDrain = 0.0;
                this.perType = 8;
        }

    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return this.perType;
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
        Api.instance().definitions().materials().emptyStorageCell().maybeStack(1).ifPresent(is -> {
            final ItemStack extraA = ia.addItems(is);
            if (!extraA.isEmpty()) {
                player.dropItem(extraA, false);
            }
        });
    }
}
