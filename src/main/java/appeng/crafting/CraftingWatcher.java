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

package appeng.crafting;


import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import javax.annotation.Nonnull;

import appeng.api.networking.crafting.ICraftingWatcher;
import appeng.api.networking.crafting.ICraftingWatcherHost;
import appeng.api.storage.data.IAEStack;
import appeng.me.cache.CraftingGridCache;


/**
 * Maintain my interests, and a global watch list, they should always be fully synchronized.
 */
public final class CraftingWatcher implements ICraftingWatcher
{

	final CraftingGridCache gsc;
	final ICraftingWatcherHost host;
	final HashSet<IAEStack> myInterests = new HashSet<IAEStack>();

	public CraftingWatcher( CraftingGridCache cache, ICraftingWatcherHost host )
	{
		this.gsc = cache;
		this.host = host;
	}

	public final ICraftingWatcherHost getHost()
	{
		return this.host;
	}

	@Override
	public final int size()
	{
		return this.myInterests.size();
	}

	@Override
	public final boolean isEmpty()
	{
		return this.myInterests.isEmpty();
	}

	@Override
	public final boolean contains( Object o )
	{
		return this.myInterests.contains( o );
	}

	@Nonnull
	@Override
	public final Iterator<IAEStack> iterator()
	{
		return new ItemWatcherIterator( this, this.myInterests.iterator() );
	}

	@Nonnull
	@Override
	public final Object[] toArray()
	{
		return this.myInterests.toArray();
	}

	@Nonnull
	@Override
	public final <T> T[] toArray( @Nonnull T[] a )
	{
		return this.myInterests.toArray( a );
	}

	@Override
	public final boolean add( IAEStack e )
	{
		if( this.myInterests.contains( e ) )
		{
			return false;
		}

		return this.myInterests.add( e.copy() ) && this.gsc.interestManager.put( e, this );
	}

	@Override
	public final boolean remove( Object o )
	{
		return this.myInterests.remove( o ) && this.gsc.interestManager.remove( (IAEStack) o, this );
	}

	@Override
	public final boolean containsAll( @Nonnull Collection<?> c )
	{
		return this.myInterests.containsAll( c );
	}

	@Override
	public final boolean addAll( @Nonnull Collection<? extends IAEStack> c )
	{
		boolean didChange = false;

		for( IAEStack o : c )
		{
			didChange = this.add( o ) || didChange;
		}

		return didChange;
	}

	@Override
	public final boolean removeAll( @Nonnull Collection<?> c )
	{
		boolean didSomething = false;
		for( Object o : c )
		{
			didSomething = this.remove( o ) || didSomething;
		}
		return didSomething;
	}

	@Override
	public final boolean retainAll( @Nonnull Collection<?> c )
	{
		boolean changed = false;
		Iterator<IAEStack> i = this.iterator();

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
	public final void clear()
	{
		Iterator<IAEStack> i = this.myInterests.iterator();
		while( i.hasNext() )
		{
			this.gsc.interestManager.remove( i.next(), this );
			i.remove();
		}
	}

	final class ItemWatcherIterator implements Iterator<IAEStack>
	{

		final CraftingWatcher watcher;
		final Iterator<IAEStack> interestIterator;
		IAEStack myLast;

		public ItemWatcherIterator( CraftingWatcher parent, Iterator<IAEStack> i )
		{
			this.watcher = parent;
			this.interestIterator = i;
		}

		@Override
		public final boolean hasNext()
		{
			return this.interestIterator.hasNext();
		}

		@Override
		public final IAEStack next()
		{
			return this.myLast = this.interestIterator.next();
		}

		@Override
		public final void remove()
		{
			CraftingWatcher.this.gsc.interestManager.remove( this.myLast, this.watcher );
			this.interestIterator.remove();
		}
	}
}
