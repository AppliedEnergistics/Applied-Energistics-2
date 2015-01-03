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

package appeng.core.features;


import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

import com.google.common.base.Optional;

import appeng.api.definitions.IBlockDefinition;


public class BlockDefinition extends ItemDefinition implements IBlockDefinition
{
	private final Block block;
	private final boolean enabled;

	public BlockDefinition( Block block, ActivityState state )
	{
		super( Item.getItemFromBlock( block ), state );
		this.block = block;
		this.enabled = state == ActivityState.Enabled;
	}

	@Override
	public Optional<Block> maybeBlock()
	{
		return Optional.of( this.block );
	}

	@Override
	public Optional<ItemBlock> maybeItemBlock()
	{
		if ( this.enabled )
		{
			return Optional.of( new ItemBlock( this.block ) );
		}
		else
		{
			return Optional.absent();
		}
	}

	@Override
	public final Optional<ItemStack> maybeStack( int stackSize )
	{
		if ( this.enabled )
		{
			return Optional.of( new ItemStack( this.block ) );
		}
		else
		{
			return Optional.absent();
		}
	}

	@Override
	public final boolean isSameAs( IBlockAccess world, int x, int y, int z )
	{
		return this.enabled && world.getBlock( x, y, z ) == this.block;
	}
}
