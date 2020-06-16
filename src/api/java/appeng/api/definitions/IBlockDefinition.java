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

package appeng.api.definitions;

import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public interface IBlockDefinition extends IItemDefinition {
    /**
     * @return the {@link Block} implementation if applicable
     */
    default Optional<Block> maybeBlock() {
        return Optional.of(block());
    }

    Block block();

    /**
     * @return the {@link BlockItem} implementation if applicable
     */
    default Optional<BlockItem> maybeBlockItem() {
        return Optional.of(blockItem());
    }

    BlockItem blockItem();

    /**
     * Compare Block with world.
     *
     * @param world world of block
     * @param pos   location
     *
     * @return if the block is placed in the world at the specific location.
     */
    boolean isSameAs(IBlockReader world, BlockPos pos);
}
