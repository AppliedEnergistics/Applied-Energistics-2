/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

import appeng.util.Platform;
import com.google.common.base.Preconditions;

import gregtech.api.items.IToolItem;
import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;


/**
 * This variant list is optimized for damageable items, and supports selecting durability ranges with
 * {@link #findFuzzy(IAEItemStack, FuzzyMode)}.
 */
class FuzzyItemVariantList extends ItemVariantList
{

	static final SharedStackComparator COMPARATOR = new SharedStackComparator();

	// NOTE: We only use Object as they key here so we can pass our special DamageBounds to the subMap method.
	// We NEVER put any keys in this map that are not AESharedItemStacks.
	private final Object2ObjectSortedMap<Object, IAEItemStack> records = new Object2ObjectAVLTreeMap<>( COMPARATOR );

	@Override
	public Collection<IAEItemStack> findFuzzy( final IAEItemStack filter, final FuzzyMode fuzzy )
	{
		ItemStack itemStack = filter.getDefinition();

		ItemDamageBound lowerBound = makeLowerBound( itemStack, fuzzy );
		ItemDamageBound upperBound = makeUpperBound( itemStack, fuzzy );
		Preconditions.checkState( lowerBound.itemDamage > upperBound.itemDamage );

		return this.records.subMap( lowerBound, upperBound ).values();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	Map<AESharedItemStack, IAEItemStack> getRecords()
	{
		// We ensure on our end that we NEVER use anything but AESharedItemStack as the key in this map
		return (Map<AESharedItemStack, IAEItemStack>) (Object) this.records;
	}

	static class ItemDamageBound
	{
		final int itemDamage;

		public ItemDamageBound( int itemDamage )
		{
			this.itemDamage = itemDamage;
		}
	}

	/**
	 * This comparator creates a strict and total ordering over all {@link AESharedItemStack} of the same item. To
	 * support selecting ranges of durability, it is defined for type {@link Object} and also accepts
	 * {@link ItemDamageBound} as an argument to compare against.
	 */
	static class SharedStackComparator implements Comparator<Object>
	{
		@Override
		public int compare( Object a, Object b )
		{
			// Either argument can either be a damage bound or a shared item stack
			// Since we never put damage bounds into the map as keys, only one
			// of the two arguments can possibly be a bound
			ItemDamageBound boundA = null;
			AESharedItemStack stackA = null;
			int itemDamageA;
			if( a instanceof ItemDamageBound )
			{
				boundA = (ItemDamageBound) a;
				itemDamageA = boundA.itemDamage;
			}
			else
			{
				stackA = (AESharedItemStack) a;
				itemDamageA = stackA.getItemDamage();
			}
			ItemDamageBound boundB = null;
			AESharedItemStack stackB = null;
			int itemDamageB;
			if( b instanceof ItemDamageBound )
			{
				boundB = (ItemDamageBound) b;
				itemDamageB = boundB.itemDamage;
			}
			else
			{
				stackB = (AESharedItemStack) b;
				itemDamageB = stackB.getItemDamage();
			}

			// When either argument is a damage bound, we just compare the damage values because it is used
			// only to get a certain damage range out of the map.
			if( boundA != null || boundB != null )
			{
				return Integer.compare( itemDamageB, itemDamageA );
			}

			ItemStack itemStackA = stackA.getDefinition();
			ItemStack itemStackB = stackB.getDefinition();
			Preconditions.checkState( itemStackA.getCount() == 1, "ItemStack#getCount() has to be 1" );
			Preconditions.checkArgument( itemStackB.getCount() == 1, "ItemStack#getCount() has to be 1" );

			if( itemStackA == itemStackB )
			{
				return 0;
			}

			// Damaged items are sorted before undamaged items
			final int damageValue = Integer.compare( itemDamageB, itemDamageA );
			if( damageValue != 0 )
			{
				return damageValue;
			}

			// As a final tie breaker, order by the object identity of the item stack
			// While this will order seemingly at random, we only need the order of
			// damage values to be predictable, while still having to satisfy the
			// complete order requirements of the sorted map
			return Long.compare( System.identityHashCode( itemStackA ), System.identityHashCode( itemStackB ) );
		}
	}

	/**
	 * Minecraft reverses the damage values. So anything with a damage of 0 is undamaged and increases the more damaged
	 * the item is.
	 * <p>
	 * Further the used subMap follows [MAX_DAMAGE, MIN_DAMAGE), so to include undamaged items, we have to start with a
	 * lower damage value than 0, while it is fine to use {@link ItemStack#getMaxDamage()} for the upper bound.
	 */
	private static final int MIN_DAMAGE_VALUE = -1;

	/*
	 * Keep in mind that the stack order is from most damaged to least damaged, so this lower bound will actually be a
	 * higher number than the upper bound.
	 */
	static ItemDamageBound makeLowerBound( final ItemStack stack, final FuzzyMode fuzzy )
	{
		Preconditions.checkState( stack.getItem().isDamageable() || ( Platform.isGTDamageableItem( stack.getItem() ) ), "Item#isDamageable() has to be true" );

		int damage;
		int maxDamage;
		if( Platform.isGTDamageableItem( stack.getItem() ) )
		{
			maxDamage = ( (IToolItem) stack.getItem() ).getMaxItemDamage( stack );
			damage = ( (IToolItem) stack.getItem() ).getItemDamage( stack );
		}
		else
		{
			maxDamage = stack.getMaxDamage();
			damage = stack.getItemDamage();
		}

		if( fuzzy == FuzzyMode.IGNORE_ALL )
		{
			if( maxDamage != 0 )
			{
				damage = maxDamage;
			}
		}
		else
		{
			final int breakpoint = fuzzy.calculateBreakPoint( maxDamage );
			damage = damage <= breakpoint ? breakpoint : maxDamage;
		}

		return new ItemDamageBound( damage );
	}

	/*
	 * Keep in mind that the stack order is from most damaged to least damaged, so this upper bound will actually be a
	 * lower number than the lower bound. It also is exclusive.
	 */
	static ItemDamageBound makeUpperBound( final ItemStack stack, final FuzzyMode fuzzy )
	{
		Preconditions.checkState( stack.getItem().isDamageable() || ( Platform.isGTDamageableItem( stack.getItem() ) ), "Item#isDamageable() has to be true" );

		int damage;
		if( fuzzy == FuzzyMode.IGNORE_ALL )
		{
			damage = MIN_DAMAGE_VALUE;
		}
		else
		{
			int maxDamage;
			if( Platform.isGTDamageableItem( stack.getItem() ) )
			{
				maxDamage = ( (IToolItem) stack.getItem() ).getMaxItemDamage( stack );
				damage = ( (IToolItem) stack.getItem() ).getItemDamage( stack );
			}
			else
			{
				maxDamage = stack.getMaxDamage();
				damage = stack.getItemDamage();
			}

			final int breakpoint = fuzzy.calculateBreakPoint( maxDamage );
			damage = damage <= breakpoint ? MIN_DAMAGE_VALUE : breakpoint;
		}

		return new ItemDamageBound( damage );
	}

}
