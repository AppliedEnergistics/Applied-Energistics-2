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

package appeng.items.storage;


import appeng.api.AEApi;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.items.materials.MaterialType;
import appeng.util.InventoryAdaptor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;


public final class BasicItemStorageCell extends AbstractStorageCell<IAEItemStack> {

    protected final int perType;
    protected final double idleDrain;

    public BasicItemStorageCell(final MaterialType whichCell, final int kilobytes) {
        super(whichCell, kilobytes);
        switch (whichCell) {
            case CELL1K_PART:
                this.idleDrain = 0.5;
                this.perType = 8;
                break;
            case CELL4K_PART:
                this.idleDrain = 1.0;
                this.perType = 32;
                break;
            case CELL16K_PART:
                this.idleDrain = 1.5;
                this.perType = 128;
                break;
            case CELL64K_PART:
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
    public IStorageChannel<IAEItemStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    @Override
    protected void dropEmptyStorageCellCase(final InventoryAdaptor ia, final EntityPlayer player) {
        AEApi.instance().definitions().materials().emptyStorageCell().maybeStack(1).ifPresent(is ->
        {
            final ItemStack extraA = ia.addItems(is);
            if (!extraA.isEmpty()) {
                player.dropItem(extraA, false);
            }
        });
    }
}
