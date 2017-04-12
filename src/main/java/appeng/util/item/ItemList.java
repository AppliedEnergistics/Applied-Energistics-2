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


import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;


public final class ItemList implements IItemList<IAEItemStack>
{

	private final NavigableMap<IAEItemStack, IAEItemStack> records = new ConcurrentSkipListMap<IAEItemStack, IAEItemStack>();

	@Override
	public void add( final IAEItemStack option )
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

		this.putItemRecord( opt );
	}

	@Override
	public IAEItemStack findPrecise( final IAEItemStack itemStack )
	{
		if( itemStack == null )
		{
			return null;
		}

		return this.records.get( itemStack );
	}

	@Override
	public Collection<IAEItemStack> findFuzzy( final IAEItemStack filter, final FuzzyMode fuzzy )
	{
		if( filter == null )
		{
			return Collections.emptyList();
		}

		final AEItemStack ais = (AEItemStack) filter;

		if( ais.isOre() )
		{
			final OreReference or = ais.getDefinition().getIsOre();

			if( or.getAEEquivalents().size() == 1 )
			{
				final IAEItemStack is = or.getAEEquivalents().get( 0 );

				return this.findFuzzyDamage( (AEItemStack) is, fuzzy, is.getItemDamage() == OreDictionary.WILDCARD_VALUE );
			}
			else
			{
				final Collection<IAEItemStack> output = new LinkedList<IAEItemStack>();

				for( final IAEItemStack is : or.getAEEquivalents() )
				{
					output.addAll( this.findFuzzyDamage( (AEItemStack) is, fuzzy, is.getItemDamage() == OreDictionary.WILDCARD_VALUE ) );
				}

				return output;
			}
		}

		return this.findFuzzyDamage( ais, fuzzy, false );
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

		final IAEItemStack st = this.records.get( option );

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

		final IAEItemStack st = this.records.get( option );

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

		final IAEItemStack st = this.records.get( option );

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
		return new MeaningfulItemIterator<IAEItemStack>( this.records.values().iterator() );
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
		return this.records.put( itemStack, itemStack );
	}

	private Collection<IAEItemStack> findFuzzyDamage( final AEItemStack filter, final FuzzyMode fuzzy, final boolean ignoreMeta )
	{
		final IAEItemStack low = filter.getLow( fuzzy, ignoreMeta );
		final IAEItemStack high = filter.getHigh( fuzzy, ignoreMeta );

		return this.records.subMap( low, true, high, true ).descendingMap().values();
	}
}
