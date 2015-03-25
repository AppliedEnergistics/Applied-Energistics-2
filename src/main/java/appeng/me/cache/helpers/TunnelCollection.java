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

package appeng.me.cache.helpers;


import java.util.Collection;
import java.util.Iterator;

import appeng.parts.p2p.PartP2PTunnel;
import appeng.util.iterators.NullIterator;


public class TunnelCollection<T extends PartP2PTunnel> implements Iterable<T>
{

	final Class clz;
	Collection<T> tunnelSources;

	public TunnelCollection( Collection<T> src, Class c )
	{
		this.tunnelSources = src;
		this.clz = c;
	}

	public void setSource( Collection<T> c )
	{
		this.tunnelSources = c;
	}

	public boolean isEmpty()
	{
		return !this.iterator().hasNext();
	}

	@Override
	public Iterator<T> iterator()
	{
		if( this.tunnelSources == null )
			return new NullIterator<T>();
		return new TunnelIterator<T>( this.tunnelSources, this.clz );
	}

	public boolean matches( Class<? extends PartP2PTunnel> c )
	{
		return this.clz == c;
	}

	public Class<? extends PartP2PTunnel> getClz()
	{
		return this.clz;
	}
}
