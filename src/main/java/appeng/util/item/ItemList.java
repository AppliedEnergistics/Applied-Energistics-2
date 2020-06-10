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
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;


public final class ItemList implements IItemList<IAEItemStack>
{

	private final NavigableMap<AESharedItemStack, IAEItemStack> records = new ConcurrentSkipListMap<>();

	@Override
	public void add( final IAEItemStack option )
	{
		if( option == null )
		{
			return;
		}

		final IAEItemStack st = this.records.get( ( (AEItemStack) option ).getSharedStack() );

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

		return this.records.get( ( (AEItemStack) itemStack ).getSharedStack() );
	}

	@Override
	public Collection<IAEItemStack> findFuzzy( final IAEItemStack filter )
	{
		if( filter == null )
		{
			return Collections.emptyList();
		}

		final AEItemStack ais = (AEItemStack) filter;
		final Item a = ais.getItem();

		Collection<ResourceLocation> atags = ais.getItemTags();

		return this.records.values().stream().filter( is ->
				a.equals( is.getItem() ) || ItemTagHelper.INSTANCE.isSimilarItem( atags, is.getItem() ) ).collect( Collectors.toSet());
	}

	@Override public Collection<IAEItemStack> find( IAEItemStack input, FuzzyMode fuzzyMode )
	{
		if(input == null)
		{
			return Collections.emptyList();
		}

		if( !fuzzyMode.isEnabled() )
		{
			IAEItemStack item = this.findPrecise( input );
			return item != null ? Collections.singleton( item ) : Collections.emptyList();
		}
		return this.findFuzzy( input );
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

		final IAEItemStack st = this.records.get( ( (AEItemStack) option ).getSharedStack() );

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

		final IAEItemStack st = this.records.get( ( (AEItemStack) option ).getSharedStack() );

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

		final IAEItemStack st = this.records.get( ( (AEItemStack) option ).getSharedStack() );

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
		return this.records.put( ( (AEItemStack) itemStack ).getSharedStack(), itemStack );
	}
}
