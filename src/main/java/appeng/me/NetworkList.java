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

package appeng.me;


import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public final class NetworkList implements Collection<Grid>
{

	private List<Grid> networks = new LinkedList<Grid>();

	@Override
	public final int size()
	{
		return this.networks.size();
	}

	@Override
	public final boolean isEmpty()
	{
		return this.networks.isEmpty();
	}

	@Override
	public final boolean contains( Object o )
	{
		return this.networks.contains( o );
	}

	@Override
	public final Iterator<Grid> iterator()
	{
		return this.networks.iterator();
	}

	@Override
	public final Object[] toArray()
	{
		return this.networks.toArray();
	}

	@Override
	public final <T> T[] toArray( T[] a )
	{
		return this.networks.toArray( a );
	}

	@Override
	public final boolean add( Grid e )
	{
		this.copy();
		return this.networks.add( e );
	}

	@Override
	public final boolean remove( Object o )
	{
		this.copy();
		return this.networks.remove( o );
	}

	@Override
	public final boolean containsAll( Collection<?> c )
	{
		return this.networks.containsAll( c );
	}

	@Override
	public final boolean addAll( Collection<? extends Grid> c )
	{
		this.copy();
		return this.networks.addAll( c );
	}

	@Override
	public final boolean removeAll( Collection<?> c )
	{
		this.copy();
		return this.networks.removeAll( c );
	}

	@Override
	public final boolean retainAll( Collection<?> c )
	{
		this.copy();
		return this.networks.retainAll( c );
	}

	@Override
	public final void clear()
	{
		this.networks = new LinkedList<Grid>();
	}

	private void copy()
	{
		List<Grid> old = this.networks;
		this.networks = new LinkedList<Grid>();
		this.networks.addAll( old );
	}
}
