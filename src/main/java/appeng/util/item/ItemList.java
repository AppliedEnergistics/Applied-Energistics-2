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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import net.minecraftforge.oredict.OreDictionary;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

import com.google.common.collect.Lists;


public final class ItemList<StackType extends IAEStack> implements IItemList<StackType>
{

	private final NavigableMap<StackType, StackType> records = new ConcurrentSkipListMap<StackType, StackType>();
	private final Class<? extends IAEStack> clz;

	// private int currentPriority = Integer.MIN_VALUE;
	public Throwable stacktrace;
	int iteration = Integer.MIN_VALUE;

	public ItemList( Class<? extends IAEStack> cla )
	{
		this.clz = cla;
	}

	@Override
	public synchronized void add( StackType option )
	{
		if( this.checkStackType( option ) )
		{
			return;
		}

		StackType st = this.records.get( option );

		if( st != null )
		{
			// st.setPriority( currentPriority );
			st.add( option );
			return;
		}

		StackType opt = (StackType) option.copy();
		// opt.setPriority( currentPriority );
		this.records.put( opt, opt );
	}

	private boolean checkStackType( StackType st )
	{
		if( st == null )
		{
			return true;
		}

		if( !this.clz.isInstance( st ) )
		{
			throw new IllegalArgumentException( "WRONG TYPE - got " + st.getClass().getName() + " expected " + this.clz.getName() );
		}

		return false;
	}

	@Override
	public synchronized StackType findPrecise( StackType i )
	{
		if( this.checkStackType( i ) )
		{
			return null;
		}

		StackType is = this.records.get( i );
		if( is != null )
		{
			return is;
		}

		return null;
	}

	@Override
	public Collection<StackType> findFuzzy( StackType filter, FuzzyMode fuzzy )
	{
		if( this.checkStackType( filter ) )
		{
			return new ArrayList<StackType>();
		}

		if( filter instanceof IAEFluidStack )
		{
			List<StackType> result = Lists.newArrayList();

			if( filter.equals( this ) )
			{
				result.add( filter );
			}

			return result;
		}

		AEItemStack ais = (AEItemStack) filter;
		if( ais.isOre() )
		{
			OreReference or = ais.def.isOre;
			if( or.getAEEquivalents().size() == 1 )
			{
				IAEItemStack is = or.getAEEquivalents().get( 0 );
				return this.findFuzzyDamage( (AEItemStack) is, fuzzy, is.getItemDamage() == OreDictionary.WILDCARD_VALUE );
			}
			else
			{
				Collection<StackType> output = new LinkedList<StackType>();

				for( IAEItemStack is : or.getAEEquivalents() )
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

	public Collection<StackType> findFuzzyDamage( AEItemStack filter, FuzzyMode fuzzy, boolean ignoreMeta )
	{
		StackType low = (StackType) filter.getLow( fuzzy, ignoreMeta );
		StackType high = (StackType) filter.getHigh( fuzzy, ignoreMeta );
		return this.records.subMap( low, true, high, true ).descendingMap().values();
	}

	@Override
	public synchronized void addStorage( StackType option ) // adds a stack as
	// stored.
	{
		if( this.checkStackType( option ) )
		{
			return;
		}

		StackType st = this.records.get( option );

		if( st != null )
		{
			// st.setPriority( currentPriority );
			st.incStackSize( option.getStackSize() );
			return;
		}

		StackType opt = (StackType) option.copy();
		// opt.setPriority( currentPriority );
		this.records.put( opt, opt );
	}

	/*
	 * public synchronized void clean() { Iterator<StackType> i = iterator(); while (i.hasNext()) { StackType AEI =
	 * i.next(); if ( !AEI.isMeaningful() ) i.remove(); } }
	 */

	@Override
	public synchronized void addCrafting( StackType option ) // adds a stack as
	// craftable.
	{
		if( this.checkStackType( option ) )
		{
			return;
		}

		StackType st = this.records.get( option );

		if( st != null )
		{
			// st.setPriority( currentPriority );
			st.setCraftable( true );
			return;
		}

		StackType opt = (StackType) option.copy();
		// opt.setPriority( currentPriority );
		opt.setStackSize( 0 );
		opt.setCraftable( true );

		this.records.put( opt, opt );
	}

	@Override
	public synchronized void addRequestable( StackType option ) // adds a stack
	// as
	// requestable.
	{
		if( this.checkStackType( option ) )
		{
			return;
		}

		StackType st = this.records.get( option );

		if( st != null )
		{
			// st.setPriority( currentPriority );
			( (IAEItemStack) st ).setCountRequestable( st.getCountRequestable() + option.getCountRequestable() );
			return;
		}

		StackType opt = (StackType) option.copy();
		// opt.setPriority( currentPriority );
		opt.setStackSize( 0 );
		opt.setCraftable( false );
		opt.setCountRequestable( opt.getCountRequestable() );

		this.records.put( opt, opt );
	}

	@Override
	public synchronized StackType getFirstItem()
	{
		for( StackType stackType : this )
		{
			return stackType;
		}
		return null;
	}

	@Override
	public synchronized int size()
	{
		return this.records.values().size();
	}

	@Override
	public synchronized Iterator<StackType> iterator()
	{
		return new MeaningfulIterator<StackType>( this.records.values().iterator() );
	}

	@Override
	public synchronized void resetStatus()
	{
		for( StackType i : this )
		{
			i.reset();
		}
	}
}
