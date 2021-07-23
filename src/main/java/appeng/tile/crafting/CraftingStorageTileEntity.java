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

package appeng.tile.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;

import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.core.definitions.AEBlocks;

public class CraftingStorageTileEntity extends CraftingTileEntity {
    private static final int KILO_SCALAR = 1024;

    public CraftingStorageTileEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    protected ItemStack getItemFromTile() {
        final int storage = getStorageBytes() / KILO_SCALAR;

        switch (storage) {
            case 1:
                return AEBlocks.CRAFTING_STORAGE_1K.stack();
            case 4:
                return AEBlocks.CRAFTING_STORAGE_4K.stack();
            case 16:
                return AEBlocks.CRAFTING_STORAGE_16K.stack();
            case 64:
                return AEBlocks.CRAFTING_STORAGE_64K.stack();
            default:
                return super.getItemFromTile();
        }
    }

    @Override
    public boolean isAccelerator() {
        return false;
    }

    @Override
    public boolean isStorage() {
        return true;
    }

    @Override
    public int getStorageBytes() {
        if (this.level == null || this.notLoaded() || this.isRemoved()) {
            return 0;
        }

        final AbstractCraftingUnitBlock<?> unit = (AbstractCraftingUnitBlock<?>) this.level.getBlockState(this.worldPosition)
                .getBlock();
        switch (unit.type) {
            default:
            case STORAGE_1K:
                return 1024;
            case STORAGE_4K:
                return 4 * 1024;
            case STORAGE_16K:
                return 16 * 1024;
            case STORAGE_64K:
                return 64 * 1024;
        }
    }
}
