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

package appeng.me;


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.events.MENetworkEvent;
import appeng.api.networking.events.MENetworkPostCacheConstruction;
import appeng.api.util.IReadOnlyCollection;
import appeng.core.WorldSettings;
import appeng.hooks.TickHandler;
import appeng.util.ReadOnlyCollection;


public class Grid implements IGrid
{
	private final NetworkEventBus eventBus = new NetworkEventBus();
	private final Map<Class<? extends IGridHost>, MachineSet> machines = new HashMap<Class<? extends IGridHost>, MachineSet>();
	private final Map<Class<? extends IGridCache>, GridCacheWrapper> caches = new HashMap<Class<? extends IGridCache>, GridCacheWrapper>();
	private GridNode pivot;
	private int priority; // how import is this network?
	private GridStorage myStorage;

	public Grid( GridNode center )
	{
		this.pivot = center;

		Map<Class<? extends IGridCache>, IGridCache> myCaches = AEApi.instance().registries().gridCache().createCacheInstance( this );
		for ( Entry<Class<? extends IGridCache>, IGridCache> c : myCaches.entrySet() )
		{
			Class<? extends IGridCache> key = c.getKey();
			IGridCache value = c.getValue();
			Class<? extends IGridCache> valueClass = value.getClass();

			this.eventBus.readClass( key, valueClass );
			this.caches.put( key, new GridCacheWrapper( value ) );
		}

		this.postEvent( new MENetworkPostCacheConstruction() );

		TickHandler.instance.addNetwork( this );
		center.setGrid( this );
	}

	public int getPriority()
	{
		return this.priority;
	}

	public IGridStorage getMyStorage()
	{
		return this.myStorage;
	}

	public Map<Class<? extends IGridCache>, GridCacheWrapper> getCaches()
	{
		return this.caches;
	}

	public Iterable<Class<? extends IGridHost>> getMachineClasses()
	{
		return this.machines.keySet();
	}

	public int size()
	{
		int out = 0;
		for ( Collection<?> x : this.machines.values() )
			out += x.size();
		return out;
	}

	public void remove( GridNode gridNode )
	{
		for ( IGridCache c : this.caches.values() )
		{
			IGridHost machine = gridNode.getMachine();
			c.removeNode( gridNode, machine );
		}

		Class<? extends IGridHost> machineClass = gridNode.getMachineClass();
		Set<IGridNode> nodes = this.machines.get( machineClass );
		if ( nodes != null )
			nodes.remove( gridNode );

		gridNode.setGridStorage( null );

		if ( this.pivot == gridNode )
		{
			Iterator<IGridNode> n = this.getNodes().iterator();
			if ( n.hasNext() )
				this.pivot = ( GridNode ) n.next();
			else
			{
				this.pivot = null;
				TickHandler.instance.removeNetwork( this );
				this.myStorage.remove();
			}
		}
	}

	public void add( GridNode gridNode )
	{
		Class<? extends IGridHost> mClass = gridNode.getMachineClass();

		MachineSet nodes = this.machines.get( mClass );
		if ( nodes == null )
		{
			nodes = new MachineSet( mClass );
			this.machines.put( mClass, nodes );
			this.eventBus.readClass( mClass, mClass );
		}

		// handle loading grid storages.
		if ( gridNode.getGridStorage() != null )
		{
			GridStorage gs = gridNode.getGridStorage();
			IGrid grid = gs.getGrid();

			if ( grid == null )
			{
				this.myStorage = gs;
				this.myStorage.setGrid( this );

				for ( IGridCache gc : this.caches.values() )
					gc.onJoin( this.myStorage );
			}
			else if ( grid != this )
			{
				if ( this.myStorage == null )
				{
					this.myStorage = WorldSettings.getInstance().getNewGridStorage();
					this.myStorage.setGrid( this );
				}

				IGridStorage tmp = new GridStorage();
				if ( !gs.hasDivided( this.myStorage ) )
				{
					gs.addDivided( this.myStorage );

					for ( IGridCache gc : ( ( Grid ) grid ).caches.values() )
						gc.onSplit( tmp );

					for ( IGridCache gc : this.caches.values() )
						gc.onJoin( tmp );
				}
			}
		}
		else if ( this.myStorage == null )
		{
			this.myStorage = WorldSettings.getInstance().getNewGridStorage();
			this.myStorage.setGrid( this );
		}

		// update grid node...
		gridNode.setGridStorage( this.myStorage );

		// track node.
		nodes.add( gridNode );

		for ( IGridCache cache : this.caches.values() )
		{
			IGridHost machine = gridNode.getMachine();
			cache.addNode( gridNode, machine );
		}

		gridNode.getGridProxy().gridChanged();
		// postEventTo( gridNode, networkChanged );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public <C extends IGridCache> C getCache( Class<? extends IGridCache> iface )
	{
		return ( C ) this.caches.get( iface ).myCache;
	}

	@Override
	public MENetworkEvent postEvent( MENetworkEvent ev )
	{
		return this.eventBus.postEvent( this, ev );
	}

	@Override
	public MENetworkEvent postEventTo( IGridNode node, MENetworkEvent ev )
	{
		return this.eventBus.postEventTo( this, ( GridNode ) node, ev );
	}

	@Override
	public IReadOnlyCollection<Class<? extends IGridHost>> getMachinesClasses()
	{
		Set<Class<? extends IGridHost>> machineKeys = this.machines.keySet();

		return new ReadOnlyCollection<Class<? extends IGridHost>>( machineKeys );
	}

	@Override
	public IMachineSet getMachines( Class<? extends IGridHost> c )
	{
		MachineSet s = this.machines.get( c );
		if ( s == null )
			return new MachineSet( c );
		return s;
	}

	@Override
	public IReadOnlyCollection<IGridNode> getNodes()
	{
		return new GridNodeCollection( this.machines );
	}

	@Override
	public boolean isEmpty()
	{
		return this.pivot == null;
	}

	@Override
	public IGridNode getPivot()
	{
		return this.pivot;
	}

	public void setPivot( GridNode pivot )
	{
		this.pivot = pivot;
	}

	public void update()
	{
		for ( IGridCache gc : this.caches.values() )
		{
			// are there any nodes left?
			if ( this.pivot != null )
				gc.onUpdateTick();
		}
	}

	public void saveState()
	{
		for ( IGridCache c : this.caches.values() )
		{
			c.populateGridStorage( this.myStorage );
		}
	}

	public void setImportantFlag( int i, boolean publicHasPower )
	{
		int flag = 1 << i;
		this.priority = ( this.priority & ~flag ) | ( publicHasPower ? flag : 0 );
	}
}
