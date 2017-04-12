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


import appeng.api.definitions.ITileDefinition;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;


public final class WrappedDamageItemDefinition implements ITileDefinition
{
	private final ITileDefinition definition;
	private final int damage;

	public WrappedDamageItemDefinition( final ITileDefinition definition, final int damage )
	{
		Preconditions.checkNotNull( definition );
		Preconditions.checkArgument( damage >= 0 );

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
		return this.definition.maybeBlock().transform( new BlockTransformFunction( stackSize, this.damage ) );
	}

	@Override
	public boolean isEnabled()
	{
		return this.definition.isEnabled();
	}

	@Override
	public boolean isSameAs( final ItemStack comparableStack )
	{
		if( comparableStack == null )
		{
			return false;
		}

		final boolean sameItem = this.definition.isSameAs( new ItemStack( comparableStack.getItem() ) );
		final boolean sameDamage = comparableStack.getItemDamage() == this.damage;

		return sameItem && sameDamage;
	}

	@Override
	public boolean isSameAs( final IBlockAccess world, final int x, final int y, final int z )
	{
		return this.definition.isSameAs( world, x, y, z ) && world.getBlockMetadata( x, y, z ) == this.damage;
	}

	private static final class BlockTransformFunction implements Function<Block, ItemStack>
	{
		private final int stackSize;
		private final int damage;

		public BlockTransformFunction( final int stackSize, final int damage )
		{
			Preconditions.checkArgument( stackSize > 0 );
			Preconditions.checkArgument( damage >= 0 );

			this.stackSize = stackSize;
			this.damage = damage;
		}

		@Override
		public ItemStack apply( final Block input )
		{
			Preconditions.checkNotNull( input );

			return new ItemStack( input, this.stackSize, this.damage );
		}
	}
}
