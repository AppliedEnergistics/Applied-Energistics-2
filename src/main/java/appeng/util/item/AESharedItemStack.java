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

	public AESharedItemStack( final ItemStack itemStack, final int itemDamage )
	{
		this.itemStack = itemStack;
		this.itemId = Item.getIdFromItem( itemStack.getItem() );
		this.itemDamage = itemDamage; // setItemDamage may fail, so don't read from itemStack.
	}


	Bounds getBounds( final FuzzyMode fuzzy, final boolean ignoreMeta )
	{
		return new Bounds( this.itemStack, fuzzy, ignoreMeta );
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

	/**
	 * Creates the lower and upper bounds for a specific shared itemstack.
	 */
	public static final class Bounds
	{
		/**
		 * Bounds enforced by {@link ItemStack#isEmpty()}
		 */
		private static final int MIN_DAMAGE_VALUE = 0;
		private static final int MAX_DAMAGE_VALUE = 65535;

		private final AESharedItemStack lower;
		private final AESharedItemStack upper;

		public Bounds( final ItemStack stack, final FuzzyMode fuzzy, final boolean ignoreMeta )
		{
			Preconditions.checkState( !stack.isEmpty(), "ItemStack#isEmpty() has to be false" );
			Preconditions.checkState( stack.getCount() == 1, "ItemStack#getCount() has to be 1" );

			final NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : null;

			this.lower = this.makeLowerBound( stack, tag, fuzzy, ignoreMeta );
			this.upper = this.makeUpperBound( stack, tag, fuzzy, ignoreMeta );
		}

		public AESharedItemStack lower()
		{
			return this.lower;
		}

		public AESharedItemStack upper()
		{
			return this.upper;
		}

		private AESharedItemStack makeLowerBound( final ItemStack itemStack, final NBTTagCompound tag, final FuzzyMode fuzzy, final boolean ignoreMeta )
		{
			NBTTagCompound newTag = tag;
			ItemStack newDef = itemStack.copy();
			int newItemDamage = itemStack.getItemDamage();

			if( ignoreMeta )
			{
				newTag = tag;
				newItemDamage = MIN_DAMAGE_VALUE;
			}
			else
			{
				if( newDef.getItem().isDamageable() )
				{
					if( fuzzy == FuzzyMode.IGNORE_ALL )
					{
						newItemDamage = MIN_DAMAGE_VALUE;
					}
					else if( fuzzy == FuzzyMode.PERCENT_99 )
					{
						if( itemStack.getItemDamage() == MIN_DAMAGE_VALUE )
						{
							newItemDamage = MIN_DAMAGE_VALUE;
						}
						else
						{
							newItemDamage = MIN_DAMAGE_VALUE + 1;
						}
					}
					else
					{
						final int breakpoint = fuzzy.calculateBreakPoint( itemStack.getMaxDamage() );
						final int damage = breakpoint <= itemStack.getItemDamage() ? breakpoint : 0;
						newItemDamage = damage;
					}
				}
				newTag = LOW_TAG;
			}

			newDef.setItemDamage( newItemDamage );
			newDef.setTagCompound( newTag );

			return new AESharedItemStack( newDef, newItemDamage );
		}

		private AESharedItemStack makeUpperBound( final ItemStack itemStack, final NBTTagCompound tag, final FuzzyMode fuzzy, final boolean ignoreMeta )
		{
			NBTTagCompound newTag = tag;
			ItemStack newDef = itemStack.copy();
			int newItemDamage = itemStack.getItemDamage();

			if( ignoreMeta )
			{
				newItemDamage = MAX_DAMAGE_VALUE;
				newTag = tag;
			}
			else
			{
				if( newDef.getItem().isDamageable() )
				{
					if( fuzzy == FuzzyMode.IGNORE_ALL )
					{
						newItemDamage = itemStack.getMaxDamage() + 1;
					}
					else if( fuzzy == FuzzyMode.PERCENT_99 )
					{
						if( itemStack.getItemDamage() == MIN_DAMAGE_VALUE )
						{
							newItemDamage = MIN_DAMAGE_VALUE;
						}
						else
						{
							newItemDamage = itemStack.getMaxDamage() + 1;
						}
					}
					else
					{
						final int breakpoint = fuzzy.calculateBreakPoint( itemStack.getMaxDamage() );
						final int damage = itemStack.getItemDamage() < breakpoint ? breakpoint - 1 : itemStack.getMaxDamage() + 1;
						newItemDamage = damage;
					}
				}
				newTag = HIGH_TAG;
			}

			newDef.setItemDamage( newItemDamage );
			newDef.setTagCompound( newTag );

			return new AESharedItemStack( newDef, newItemDamage );
		}

	}
}
