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


import javax.annotation.Nonnull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import appeng.api.definitions.IItemDefinition;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;


public final class DamagedItemDefinition implements IItemDefinition
{
	private final IStackSrc source;

	public DamagedItemDefinition( @Nonnull IStackSrc source )
	{
		this.source = Preconditions.checkNotNull( source );
	}

	@Override
	public Optional<Item> maybeItem()
	{
		final Item item = this.source.getItem();

		return Optional.fromNullable( item );
	}

	@Override
	public Optional<ItemStack> maybeStack( int stackSize )
	{
		final ItemStack stack = this.source.stack( stackSize );

		return Optional.fromNullable( stack );
	}

	@Override
	public boolean isSameAs( ItemStack comparableStack )
	{
		if( comparableStack == null )
		{
			return false;
		}

		return comparableStack.getItem() == this.source.getItem() && comparableStack.getItemDamage() == this.source.getDamage();
	}
}
