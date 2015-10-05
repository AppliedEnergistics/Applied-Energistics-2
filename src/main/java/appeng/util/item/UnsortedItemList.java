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

package appeng.util.item;


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;


/**
 * An unsorted {@link IItemList} providing constant access time instead of logarithmic time.
 *
 * As tradeoff it will no longer support fuzzy operations.
 * Also no advanced features like storing craftable or requestable items is supported.
 *
 */
public final class UnsortedItemList implements IItemList<IAEItemStack>
{

	/**
	 * {@link Predicate} to filter all meaningful entries with {@link Iterators}
	 */
	private static final Predicate<IAEItemStack> MEANINGFUL_PREDICATE = new Predicate<IAEItemStack>()
	{

		@Override
		public boolean apply( @Nonnull IAEItemStack input )
		{
			return input.isMeaningful();
		}
	};

	private final Map<IAEItemStack, IAEItemStack> records = new HashMap<IAEItemStack, IAEItemStack>();

	@Override
	public void add( IAEItemStack option )
	{
		if( option == null )
		{
			return;
		}

		final IAEItemStack st = this.records.get( option );

		if( st != null )
		{
			st.add( option );
			return;
		}

		final IAEItemStack opt = option.copy();

		this.records.put( opt, opt );
	}

	@Override
	public IAEItemStack findPrecise( IAEItemStack itemStack )
	{
		if( itemStack == null )
		{
			return null;
		}

		return this.records.get( itemStack );
	}

	@Override
	public boolean isEmpty()
	{
		return this.records.isEmpty();
	}

	@Override
	public int size()
	{
		return this.records.size();
	}

	@Override
	public Iterator<IAEItemStack> iterator()
	{
		return Iterators.filter( this.records.values().iterator(), MEANINGFUL_PREDICATE );
	}

	@Override
	public IAEItemStack getFirstItem()
	{
		for( final IAEItemStack stackType : this )
		{
			return stackType;
		}

		return null;
	}

	@Override
	public void resetStatus()
	{
		for( final IAEItemStack i : this )
		{
			i.reset();
		}
	}

	/**
	 * Unsupported due to being a unsorted collection and thus only solvable in linear or worse time.
	 *
	 * @deprecated to indicate this method is unsupported.
	 */
	@Override
	@Deprecated
	public Collection<IAEItemStack> findFuzzy( IAEItemStack filter, FuzzyMode fuzzy )
	{
		throw new UnsupportedOperationException( "Unsupported on an unsorted collection" );
	}

	/**
	 * Unsupported to avoid being used as anything but a plain collection to store real item stacks.
	 *
	 * @deprecated to indicate this method is unsupported.
	 */
	@Override
	@Deprecated
	public void addStorage( IAEItemStack option )
	{
		throw new UnsupportedOperationException( "Purely designed for item storage" );
	}

	/**
	 * Unsupported to avoid being used as anything but a plain collection to store real item stacks.
	 *
	 * @deprecated to indicate this method is unsupported.
	 */
	@Override
	@Deprecated
	public void addCrafting( IAEItemStack option )
	{
		throw new UnsupportedOperationException( "Purely designed for item storage" );
	}

	/**
	 * Unsupported to avoid being used as anything but a plain collection to store real item stacks.
	 *
	 * @deprecated to indicate this method is unsupported.
	 */
	@Override
	@Deprecated
	public void addRequestable( IAEItemStack option )
	{
		throw new UnsupportedOperationException( "Purely designed for item storage" );
	}
}
