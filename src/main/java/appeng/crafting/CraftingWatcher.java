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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import appeng.api.networking.crafting.ICraftingWatcher;
import appeng.api.networking.crafting.ICraftingWatcherHost;
import appeng.api.storage.data.IAEStack;
import appeng.me.cache.CraftingGridCache;

/**
 * Maintain my interests, and a global watch list, they should always be fully synchronized.
 */
public class CraftingWatcher implements ICraftingWatcher
{

	class ItemWatcherIterator implements Iterator<IAEStack>
	{

		final CraftingWatcher watcher;
		final Iterator<IAEStack> interestIterator;
		IAEStack myLast;

		public ItemWatcherIterator(CraftingWatcher parent, Iterator<IAEStack> i) {
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
			CraftingWatcher.this.gsc.interestManager.remove( this.myLast, this.watcher );
			this.interestIterator.remove();
		}

	}

	final CraftingGridCache gsc;
	final ICraftingWatcherHost host;
	final HashSet<IAEStack> myInterests = new HashSet<IAEStack>();

	public CraftingWatcher(CraftingGridCache cache, ICraftingWatcherHost host) {
		this.gsc = cache;
		this.host = host;
	}

	public ICraftingWatcherHost getHost()
	{
		return this.host;
	}

	@Override
	public boolean add(IAEStack e)
	{
		if ( this.myInterests.contains( e ) )
			return false;

		return this.myInterests.add( e.copy() ) && this.gsc.interestManager.put( e, this );
	}

	@Override
	public boolean addAll(Collection<? extends IAEStack> c)
	{
		boolean didChange = false;

		for (IAEStack o : c)
			didChange = this.add( o ) || didChange;

		return didChange;
	}

	@Override
	public void clear()
	{
		Iterator<IAEStack> i = this.myInterests.iterator();
		while (i.hasNext())
		{
			this.gsc.interestManager.remove( i.next(), this );
			i.remove();
		}
	}

	@Override
	public boolean contains(Object o)
	{
		return this.myInterests.contains( o );
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return this.myInterests.containsAll( c );
	}

	@Override
	public boolean isEmpty()
	{
		return this.myInterests.isEmpty();
	}

	@Override
	public Iterator<IAEStack> iterator()
	{
		return new ItemWatcherIterator( this, this.myInterests.iterator() );
	}

	@Override
	public boolean remove(Object o)
	{
		return this.myInterests.remove( o ) && this.gsc.interestManager.remove( (IAEStack) o, this );
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		boolean didSomething = false;
		for (Object o : c)
			didSomething = this.remove( o ) || didSomething;
		return didSomething;
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		boolean changed = false;
		Iterator<IAEStack> i = this.iterator();

		while (i.hasNext())
		{
			if ( !c.contains( i.next() ) )
			{
				i.remove();
				changed = true;
			}
		}

		return changed;
	}

	@Override
	public int size()
	{
		return this.myInterests.size();
	}

	@Override
	public Object[] toArray()
	{
		return this.myInterests.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return this.myInterests.toArray( a );
	}

}
