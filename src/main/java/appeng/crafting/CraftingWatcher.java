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

package appeng.crafting;


import appeng.api.networking.crafting.ICraftingWatcher;
import appeng.api.networking.crafting.ICraftingWatcherHost;
import appeng.api.storage.data.IAEStack;
import appeng.me.cache.CraftingGridCache;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;


/**
 * Maintain my interests, and a global watch list, they should always be fully synchronized.
 */
public class CraftingWatcher implements ICraftingWatcher
{

	private final CraftingGridCache gsc;
	private final ICraftingWatcherHost host;
	private final HashSet<IAEStack> myInterests = new HashSet<IAEStack>();

	public CraftingWatcher( final CraftingGridCache cache, final ICraftingWatcherHost host )
	{
		this.gsc = cache;
		this.host = host;
	}

	public ICraftingWatcherHost getHost()
	{
		return this.host;
	}

	@Override
	public int size()
	{
		return this.myInterests.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.myInterests.isEmpty();
	}

	@Override
	public boolean contains( final Object o )
	{
		return this.myInterests.contains( o );
	}

	@Nonnull
	@Override
	public Iterator<IAEStack> iterator()
	{
		return new ItemWatcherIterator( this, this.myInterests.iterator() );
	}

	@Nonnull
	@Override
	public Object[] toArray()
	{
		return this.myInterests.toArray();
	}

	@Nonnull
	@Override
	public <T> T[] toArray( @Nonnull final T[] a )
	{
		return this.myInterests.toArray( a );
	}

	@Override
	public boolean add( final IAEStack e )
	{
		if( this.myInterests.contains( e ) )
		{
			return false;
		}

		return this.myInterests.add( e.copy() ) && this.gsc.getInterestManager().put( e, this );
	}

	@Override
	public boolean remove( final Object o )
	{
		return this.myInterests.remove( o ) && this.gsc.getInterestManager().remove( (IAEStack) o, this );
	}

	@Override
	public boolean containsAll( @Nonnull final Collection<?> c )
	{
		return this.myInterests.containsAll( c );
	}

	@Override
	public boolean addAll( @Nonnull final Collection<? extends IAEStack> c )
	{
		boolean didChange = false;

		for( final IAEStack o : c )
		{
			didChange = this.add( o ) || didChange;
		}

		return didChange;
	}

	@Override
	public boolean removeAll( @Nonnull final Collection<?> c )
	{
		boolean didSomething = false;
		for( final Object o : c )
		{
			didSomething = this.remove( o ) || didSomething;
		}
		return didSomething;
	}

	@Override
	public boolean retainAll( @Nonnull final Collection<?> c )
	{
		boolean changed = false;
		final Iterator<IAEStack> i = this.iterator();

		while( i.hasNext() )
		{
			if( !c.contains( i.next() ) )
			{
				i.remove();
				changed = true;
			}
		}

		return changed;
	}

	@Override
	public void clear()
	{
		final Iterator<IAEStack> i = this.myInterests.iterator();
		while( i.hasNext() )
		{
			this.gsc.getInterestManager().remove( i.next(), this );
			i.remove();
		}
	}

	private class ItemWatcherIterator implements Iterator<IAEStack>
	{

		private final CraftingWatcher watcher;
		private final Iterator<IAEStack> interestIterator;
		private IAEStack myLast;

		public ItemWatcherIterator( final CraftingWatcher parent, final Iterator<IAEStack> i )
		{
			this.watcher = parent;
			this.interestIterator = i;
		}

		@Override
		public boolean hasNext()
		{
			return this.interestIterator.hasNext();
		}

		@Override
		public IAEStack next()
		{
			return this.myLast = this.interestIterator.next();
		}

		@Override
		public void remove()
		{
			CraftingWatcher.this.gsc.getInterestManager().remove( this.myLast, this.watcher );
			this.interestIterator.remove();
		}
	}
}
