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


import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.block.crafting.BlockCraftingUnit;
import net.minecraft.item.ItemStack;

import java.util.Optional;


public class TileCraftingStorageTile extends TileCraftingTile {
    private static final int KILO_SCALAR = 1024;

    @Override
    protected ItemStack getItemFromTile(final Object obj) {
        final IBlocks blocks = AEApi.instance().definitions().blocks();
        final int storage = ((TileCraftingTile) obj).getStorageBytes() / KILO_SCALAR;

        Optional<ItemStack> is;

        switch (storage) {
            case 1:
                is = blocks.craftingStorage1k().maybeStack(1);
                break;
            case 4:
                is = blocks.craftingStorage4k().maybeStack(1);
                break;
            case 16:
                is = blocks.craftingStorage16k().maybeStack(1);
                break;
            case 64:
                is = blocks.craftingStorage64k().maybeStack(1);
                break;
            default:
                is = Optional.empty();
                break;
        }

        return is.orElseGet(() -> super.getItemFromTile(obj));
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
        if (this.world == null || this.notLoaded() || this.isInvalid()) {
            return 0;
        }

        final BlockCraftingUnit unit = (BlockCraftingUnit) this.world.getBlockState(this.pos).getBlock();
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
