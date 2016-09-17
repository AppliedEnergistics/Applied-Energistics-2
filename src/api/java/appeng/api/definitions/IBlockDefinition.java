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
import net.minecraft.item.ItemBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;


public interface IBlockDefinition extends IItemDefinition
{
	/**
	 * @return the {@link Block} implementation if applicable
	 */
	Optional<Block> maybeBlock();

	/**
	 * @return the {@link ItemBlock} implementation if applicable
	 */
	Optional<ItemBlock> maybeItemBlock();

	/**
	 * Compare Block with world.
	 *
	 * @param world world of block
	 * @param pos location
	 *
	 * @return if the block is placed in the world at the specific location.
	 */
	boolean isSameAs( IBlockAccess world, BlockPos pos );
}
