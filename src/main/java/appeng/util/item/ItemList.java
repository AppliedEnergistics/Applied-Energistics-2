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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStackSearchKey;
import appeng.api.storage.data.IItemList;


public final class ItemList implements IItemList<IAEItemStack>
{

	private final NavigableMap<IAEStackSearchKey<ItemStack>, IAEItemStack> records = new ConcurrentSkipListMap<>();

	@Override
	public void add( final IAEItemStack option )
	{
		if( option == null )
		{
			return;
		}

		final IAEItemStack st = this.records.get( option.getSearchKey() );

		if( st != null )
		{
			st.add( option );
			return;
		}

		final IAEItemStack opt = option.copy();

		this.putItemRecord( opt );
	}

	@Override
	public IAEItemStack findPrecise( final IAEItemStack itemStack )
	{
		if( itemStack == null )
		{
			return null;
		}

		return this.records.get( itemStack.getSearchKey() );
	}

	@Override
	public Collection<IAEItemStack> findFuzzy( final IAEItemStack filter, final FuzzyMode fuzzy )
	{
		if( filter == null )
		{
			return Collections.emptyList();
		}

		final AEItemStack ais = (AEItemStack) filter;

		return ais.getOre().map( or ->
		{
			if( or.getAEEquivalents().size() == 1 )
			{
				final IAEItemStack is = or.getAEEquivalents().get( 0 );

				return this.findFuzzyDamage( is, fuzzy, is.getItemDamage() == OreDictionary.WILDCARD_VALUE );
			}
			else
			{
				final Collection<IAEItemStack> output = new LinkedList<>();

				for( final IAEItemStack is : or.getAEEquivalents() )
				{
					output.addAll( this.findFuzzyDamage( is, fuzzy, is.getItemDamage() == OreDictionary.WILDCARD_VALUE ) );
				}

				return output;
			}
		} )
				.orElse( this.findFuzzyDamage( ais, fuzzy, false ) );
	}

	@Override
	public boolean isEmpty()
	{
		return !this.iterator().hasNext();
	}

	@Override
	public void addStorage( final IAEItemStack option )
	{
		if( option == null )
		{
			return;
		}

		final IAEItemStack st = this.records.get( option.getSearchKey() );

		if( st != null )
		{
			st.incStackSize( option.getStackSize() );
			return;
		}

		final IAEItemStack opt = option.copy();

		this.putItemRecord( opt );
	}

	/*
	 * public void clean() { Iterator<StackType> i = iterator(); while (i.hasNext()) { StackType AEI =
	 * i.next(); if ( !AEI.isMeaningful() ) i.remove(); } }
	 */

	@Override
	public void addCrafting( final IAEItemStack option )
	{
		if( option == null )
		{
			return;
		}

		final IAEItemStack st = this.records.get( option.getSearchKey() );

		if( st != null )
		{
			st.setCraftable( true );
			return;
		}

		final IAEItemStack opt = option.copy();
		opt.setStackSize( 0 );
		opt.setCraftable( true );

		this.putItemRecord( opt );
	}

	@Override
	public void addRequestable( final IAEItemStack option )
	{
		if( option == null )
		{
			return;
		}

		final IAEItemStack st = this.records.get( option.getSearchKey() );

		if( st != null )
		{
			st.setCountRequestable( st.getCountRequestable() + option.getCountRequestable() );
			return;
		}

		final IAEItemStack opt = option.copy();
		opt.setStackSize( 0 );
		opt.setCraftable( false );
		opt.setCountRequestable( option.getCountRequestable() );

		this.putItemRecord( opt );
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
	public int size()
	{
		return this.records.size();
	}

	@Override
	public Iterator<IAEItemStack> iterator()
	{
		return new MeaningfulItemIterator<>( this.records.values().iterator() );
	}

	@Override
	public void resetStatus()
	{
		for( final IAEItemStack i : this )
		{
			i.reset();
		}
	}

	private IAEItemStack putItemRecord( final IAEItemStack itemStack )
	{
		return this.records.put( itemStack.getSearchKey(), itemStack );
	}

	private Collection<IAEItemStack> findFuzzyDamage( final IAEItemStack filter, final FuzzyMode fuzzy, final boolean ignoreMeta )
	{
		final IAEStackSearchKey<ItemStack> low = filter.getSearchKey().getLowerBound( fuzzy, ignoreMeta );
		final IAEStackSearchKey<ItemStack> high = filter.getSearchKey().getUpperBound( fuzzy, ignoreMeta );

		return this.records.subMap( low, true, high, true ).descendingMap().values();
	}
}
