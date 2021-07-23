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

package appeng.core.definitions;

import java.util.Objects;

import com.google.common.base.Preconditions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class BlockDefinition<T extends Block> extends ItemDefinition<BlockItem> {
    private final T block;

    public BlockDefinition(ResourceLocation id, T block, BlockItem item) {
        super(id, item);
        this.block = Objects.requireNonNull(block, "block");
    }

    public final T block() {
        return this.block;
    }

    @Override
    public final ItemStack stack(int stackSize) {
        Preconditions.checkArgument(stackSize > 0);

        return new ItemStack(block, stackSize);
    }

}
