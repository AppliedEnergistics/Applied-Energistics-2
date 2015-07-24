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


import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

import appeng.api.definitions.IItemDefinition;
import appeng.util.Platform;


public class ItemDefinition implements IItemDefinition
{
	private final Optional<Item> item;

	public ItemDefinition( Item item, ActivityState state )
	{
		Preconditions.checkNotNull( item );
		Preconditions.checkNotNull( state );

		if( state == ActivityState.Enabled )
		{
			this.item = Optional.of( item );
		}
		else
		{
			this.item = Optional.absent();
		}
	}

	@Override
	public final Optional<Item> maybeItem()
	{
		return this.item;
	}

	@Override
	public Optional<ItemStack> maybeStack( int stackSize )
	{
		return this.item.transform( new ItemStackTransformer( stackSize ) );
	}

	@Override
	public boolean isEnabled()
	{
		return this.item.isPresent();
	}

	@Override
	public final boolean isSameAs( ItemStack comparableStack )
	{
		return this.isEnabled() && Platform.isSameItemType( comparableStack, this.maybeStack( 1 ).get() );
	}

	@Override
	public boolean isSameAs( IBlockAccess world, int x, int y, int z )
	{
		return false;
	}

	private static class ItemStackTransformer implements Function<Item, ItemStack>
	{
		private final int stackSize;

		public ItemStackTransformer( int stackSize )
		{
			Preconditions.checkArgument( stackSize > 0 );

			this.stackSize = stackSize;
		}

		@Override
		public ItemStack apply( Item input )
		{
			return new ItemStack( input, this.stackSize );
		}
	}
}
