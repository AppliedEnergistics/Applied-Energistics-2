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


import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;


public final class GridCacheWrapper implements IGridCache
{

	final IGridCache myCache;
	final String name;

	public GridCacheWrapper( final IGridCache gc )
	{
		this.myCache = gc;
		this.name = this.myCache.getClass().getName();
	}

	@Override
	public final void onUpdateTick()
	{
		this.myCache.onUpdateTick();
	}

	@Override
	public final void removeNode( final IGridNode gridNode, final IGridHost machine )
	{
		this.myCache.removeNode( gridNode, machine );
	}

	@Override
	public final void addNode( final IGridNode gridNode, final IGridHost machine )
	{
		this.myCache.addNode( gridNode, machine );
	}

	@Override
	public final void onSplit( final IGridStorage storageB )
	{
		this.myCache.onSplit( storageB );
	}

	@Override
	public final void onJoin( final IGridStorage storageB )
	{
		this.myCache.onJoin( storageB );
	}

	@Override
	public final void populateGridStorage( final IGridStorage storage )
	{
		this.myCache.populateGridStorage( storage );
	}

	public String getName()
	{
		return this.name;
	}
}
