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


import appeng.api.definitions.IBlockDefinition;
import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Optional;


public class BlockDefinition extends ItemDefinition implements IBlockDefinition {
    private final Optional<Block> block;

    public BlockDefinition(String registryName, Block block, ItemBlock item) {
        super(registryName, item);
        this.block = Optional.ofNullable(block);
    }

    @Override
    public final Optional<Block> maybeBlock() {
        return this.block;
    }

    @Override
    public final Optional<ItemBlock> maybeItemBlock() {
        return this.block.map(ItemBlock::new);
    }

    @Override
    public final Optional<ItemStack> maybeStack(int stackSize) {
        Preconditions.checkArgument(stackSize > 0);

        return this.block.map(b -> new ItemStack(b, stackSize));
    }

    @Override
    public final boolean isSameAs(final IBlockAccess world, final BlockPos pos) {
        return this.block.isPresent() && world.getBlockState(pos).getBlock() == this.block.get();
    }
}
