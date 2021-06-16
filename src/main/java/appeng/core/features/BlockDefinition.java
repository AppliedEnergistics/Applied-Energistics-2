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

package appeng.core.features;

import com.google.common.base.Preconditions;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class BlockDefinition extends ItemDefinition {
    private final Block block;

    private final BlockItem blockItem;

    public BlockDefinition(ResourceLocation id, Block block, BlockItem item) {
        super(id, item);
        this.block = block;
        this.blockItem = item;
    }

    public final Block block() {
        return this.block;
    }

    public BlockItem blockItem() {
        return blockItem;
    }

    @Override
    public final ItemStack stack(int stackSize) {
        Preconditions.checkArgument(stackSize > 0);

        return new ItemStack(block, stackSize);
    }

}
