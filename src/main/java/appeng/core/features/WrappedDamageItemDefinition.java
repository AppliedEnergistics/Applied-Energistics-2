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


import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import appeng.api.definitions.ITileDefinition;


public final class WrappedDamageItemDefinition implements ITileDefinition
{
	private final ITileDefinition definition;
	private final int damage;

	public WrappedDamageItemDefinition( ITileDefinition definition, int damage )
	{
		this.definition = definition;
		this.damage = damage;
	}

	@Override
	public Optional<? extends Class<? extends TileEntity>> maybeEntity()
	{
		return this.definition.maybeEntity();
	}

	@Override
	public Optional<Block> maybeBlock()
	{
		return this.definition.maybeBlock();
	}

	@Override
	public Optional<ItemBlock> maybeItemBlock()
	{
		return this.definition.maybeItemBlock();
	}

	@Override
	public Optional<Item> maybeItem()
	{
		return this.definition.maybeItem();
	}

	@Override
	public Optional<ItemStack> maybeStack( final int stackSize )
	{
		return this.definition.maybeBlock().transform( new BlockTransformFunction( stackSize ) );
	}

	@Override
	public boolean isSameAs( ItemStack comparableStack )
	{
		if ( comparableStack == null )
			return false;

		return this.definition.isSameAs( comparableStack ) && comparableStack.getItemDamage() == this.damage;
	}

	@Override
	public boolean isSameAs( IBlockAccess world, int x, int y, int z )
	{
		return this.definition.isSameAs( world, x, y, z ) && world.getBlockMetadata( x, y, z ) == this.damage;
	}

	private class BlockTransformFunction implements Function<Block, ItemStack>
	{
		private final int stackSize;

		public BlockTransformFunction( int stackSize )
		{
			this.stackSize = stackSize;
		}

		@Nullable
		@Override
		public ItemStack apply( Block input )
		{
			return new ItemStack( input, this.stackSize, WrappedDamageItemDefinition.this.damage );
		}
	}
}
