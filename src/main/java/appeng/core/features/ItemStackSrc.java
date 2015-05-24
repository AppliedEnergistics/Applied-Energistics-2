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


import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


public class ItemStackSrc implements IStackSrc
{

	private final Item item;
	public final Block block;
	public final int damage;
	private final boolean enabled;

	public ItemStackSrc( Item item, int damage, ActivityState state )
	{
		Preconditions.checkNotNull( item );
		Preconditions.checkArgument( damage >= 0 );
		Preconditions.checkNotNull( state );
		Preconditions.checkArgument( state == ActivityState.Enabled || state == ActivityState.Disabled );

		this.block = null;
		this.item = item;
		this.damage = damage;
		this.enabled = state == ActivityState.Enabled;
	}

	public ItemStackSrc( Block b, int dmg, ActivityState state )
	{
		this.item = null;
		this.block = b;
		this.damage = dmg;
		this.enabled = state == ActivityState.Enabled;
	}

	@Nullable
	@Override
	public ItemStack stack( int i )
	{
		if( this.block != null )
		{
			return new ItemStack( this.block, i, this.damage );
		}

		if( this.item != null )
		{
			return new ItemStack( this.item, i, this.damage );
		}

		return null;
	}

	@Override
	public Item getItem()
	{
		return this.item;
	}

	@Override
	public int getDamage()
	{
		return this.damage;
	}

	@Override
	public boolean isEnabled()
	{
		return this.enabled;
	}
}
