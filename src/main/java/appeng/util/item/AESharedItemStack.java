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

package appeng.util.item;


import java.util.Objects;

import com.google.common.base.Preconditions;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import appeng.api.config.FuzzyMode;


final class AESharedItemStack implements Comparable<AESharedItemStack>
{
	private static final CompoundNBT LOW_TAG = new CompoundNBT();
	private static final CompoundNBT HIGH_TAG = new CompoundNBT();

	private final ItemStack itemStack;
	private final int itemId;
	private final int itemDamage;
	private final int hashCode;

	public AESharedItemStack( final ItemStack itemStack )
	{
		this.itemStack = itemStack;
		this.itemId = Item.getIdFromItem( itemStack.getItem() );
		this.itemDamage = itemStack.getDamage();
		this.hashCode = this.makeHashCode();
	}

	ItemStack getDefinition()
	{
		return this.itemStack;
	}

	int getDamage()
	{
		return this.itemDamage;
	}

	int getItemID()
	{
		return this.itemId;
	}

	@Override
	public int hashCode()
	{
		return this.hashCode;
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj instanceof AESharedItemStack )
		{
			final AESharedItemStack other = (AESharedItemStack) obj;

			Preconditions.checkState( this.itemStack.getCount() == 1, "ItemStack#getCount() has to be 1" );
			Preconditions.checkArgument( other.getDefinition().getCount() == 1, "ItemStack#getCount() has to be 1" );

			if( this.itemStack == other.itemStack )
			{
				return true;
			}
			return ItemStack.areItemStacksEqual( this.itemStack, other.itemStack );
		}
		return false;
	}

	//TODO check where this is used - just bcs this doesnt return 0 does not mean the stacks are different items
	@Override
	public int compareTo( final AESharedItemStack b )
	{
		Preconditions.checkState( this.itemStack.getCount() == 1, "ItemStack#getCount() has to be 1" );
		Preconditions.checkArgument( b.getDefinition().getCount() == 1, "ItemStack#getCount() has to be 1" );

		if( this.itemStack == b.getDefinition() )
		{
			return 0;
		}

		final int id = this.itemId - b.itemId;
		if( id != 0 )
		{
			return id;
		}

		final int damageValue = this.itemDamage - b.itemDamage;
		if( damageValue != 0 )
		{
			return damageValue;
		}

		final int nbt = this.compareNBT( b.getDefinition() );
		if( nbt != 0 )
		{
			return nbt;
		}

		if( !this.itemStack.areCapsCompatible( b.getDefinition() ) )
		{
			return System.identityHashCode( this.itemStack ) - System.identityHashCode( b.getDefinition() );
		}
		return 0;
	}

	private int compareNBT( final ItemStack b )
	{
		if( this.itemStack.getTag() == b.getTag() )
		{
			return 0;
		}
		if( this.itemStack.getTag() == LOW_TAG || b.getTag() == HIGH_TAG )
		{
			return -1;
		}
		if( this.itemStack.getTag() == HIGH_TAG || b.getTag() == LOW_TAG )
		{
			return 1;
		}
		return System.identityHashCode( this.itemStack.getTag() ) - System.identityHashCode( b.getTag() );
	}

	private int makeHashCode()
	{
		return Objects.hash( this.itemId, this.itemDamage, this.itemStack.hasTag() ? this.itemStack.getTag() : 0 );
	}
}
