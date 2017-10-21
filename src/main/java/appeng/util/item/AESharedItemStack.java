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
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.config.FuzzyMode;


final class AESharedItemStack implements Comparable<AESharedItemStack>
{
	private static final NBTTagCompound LOW_TAG = new NBTTagCompound();
	private static final NBTTagCompound HIGH_TAG = new NBTTagCompound();

	private final ItemStack itemStack;
	private final int itemId;
	private final int itemDamage;

	public AESharedItemStack( final ItemStack itemStack )
	{
		this.itemStack = itemStack;
		this.itemId = Item.getIdFromItem( itemStack.getItem() );
		this.itemDamage = itemStack.getItemDamage();
	}

	ItemStack getDefinition()
	{
		return this.itemStack;
	}

	int getItemDamage()
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
		return Objects.hash( this.itemId, this.itemDamage, this.itemStack.hasTagCompound() ? this.itemStack.getTagCompound() : 0 );
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

		return this.compareNBT( b.getDefinition() );
	}

	private int compareNBT( final ItemStack b )
	{
		if( this.itemStack.getTagCompound() == b.getTagCompound() )
		{
			return 0;
		}
		if( this.itemStack.getTagCompound() == LOW_TAG || b.getTagCompound() == HIGH_TAG )
		{
			return -1;
		}
		if( this.itemStack.getTagCompound() == HIGH_TAG || b.getTagCompound() == LOW_TAG )
		{
			return 1;
		}
		return System.identityHashCode( this.itemStack.getTagCompound() ) - System.identityHashCode( b.getTagCompound() );
	}

	public AESharedItemStack getLowerBound( final FuzzyMode fuzzy, final boolean ignoreMeta )
	{
		Preconditions.checkState( this.itemStack.getCount() == 1, "ItemStack#getCount() has to be 1" );
		final ItemStack newDef = this.itemStack.copy();

		if( ignoreMeta )
		{
			newDef.setItemDamage( 0 );
		}
		else
		{
			if( newDef.getItem().isDamageable() )
			{
				if( fuzzy == FuzzyMode.IGNORE_ALL )
				{
					newDef.setItemDamage( 0 );
				}
				else if( fuzzy == FuzzyMode.PERCENT_99 )
				{
					if( this.itemStack.getItemDamage() == 0 )
					{
						newDef.setItemDamage( 0 );
					}
					else
					{
						newDef.setItemDamage( 0 );
					}
				}
				else
				{
					final int breakpoint = fuzzy.calculateBreakPoint( this.itemStack.getMaxDamage() );
					newDef.setItemDamage( breakpoint <= this.itemDamage ? breakpoint : 0 );
				}
			}
			newDef.setTagCompound( LOW_TAG );
		}

		return new AESharedItemStack( newDef );
	}

	public AESharedItemStack getUpperBound( final FuzzyMode fuzzy, final boolean ignoreMeta )
	{
		Preconditions.checkState( this.itemStack.getCount() == 1, "ItemStack#getCount() has to be 1" );
		final ItemStack newDef = this.itemStack.copy();

		if( ignoreMeta )
		{
			newDef.setItemDamage( Integer.MAX_VALUE );
		}
		else
		{
			if( newDef.getItem().isDamageable() )
			{
				if( fuzzy == FuzzyMode.IGNORE_ALL )
				{
					newDef.setItemDamage( this.itemStack.getMaxDamage() + 1 );
				}
				else if( fuzzy == FuzzyMode.PERCENT_99 )
				{
					if( this.itemStack.getItemDamage() == 0 )
					{
						newDef.setItemDamage( 0 );
					}
					else
					{
						newDef.setItemDamage( this.itemStack.getMaxDamage() + 1 );
					}
				}
				else
				{
					final int breakpoint = fuzzy.calculateBreakPoint( this.itemStack.getMaxDamage() );
					newDef.setItemDamage( this.itemDamage < breakpoint ? breakpoint - 1 : this.itemStack.getMaxDamage() + 1 );
				}
			}
			newDef.setTagCompound( HIGH_TAG );
		}

		return new AESharedItemStack( newDef );
	}
}
