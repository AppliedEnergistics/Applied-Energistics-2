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

package appeng.blockentity.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.core.definitions.AEBlocks;

public class CraftingStorageBlockEntity extends CraftingBlockEntity {
    private static final int KILO_SCALAR = 1024;

    public CraftingStorageBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    protected Item getItemFromBlockEntity() {
        var storage = getStorageBytes() / KILO_SCALAR;

        return switch (storage) {
            case 1 -> AEBlocks.CRAFTING_STORAGE_1K.asItem();
            case 4 -> AEBlocks.CRAFTING_STORAGE_4K.asItem();
            case 16 -> AEBlocks.CRAFTING_STORAGE_16K.asItem();
            case 64 -> AEBlocks.CRAFTING_STORAGE_64K.asItem();
            default -> super.getItemFromBlockEntity();
        };
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

        final AbstractCraftingUnitBlock<?> unit = (AbstractCraftingUnitBlock<?>) this.level
                .getBlockState(this.worldPosition)
                .getBlock();
        return switch (unit.type) {
            case STORAGE_1K -> 1024;
            case STORAGE_4K -> 4 * 1024;
            case STORAGE_16K -> 16 * 1024;
            case STORAGE_64K -> 64 * 1024;
            default -> 0;
        };
    }
}
