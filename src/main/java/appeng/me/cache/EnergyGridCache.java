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

package appeng.me.cache;


import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NavigableSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergyGridProvider;
import appeng.api.networking.energy.IEnergyWatcher;
import appeng.api.networking.energy.IEnergyWatcherHost;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPostCacheConstruction;
import appeng.api.networking.events.MENetworkPowerIdleChange;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkPowerStorage;
import appeng.api.networking.events.MENetworkPowerStorage.PowerEventType;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.me.Grid;
import appeng.me.GridNode;
import appeng.me.energy.EnergyThreshold;
import appeng.me.energy.EnergyWatcher;


public class EnergyGridCache implements IEnergyGrid
{

	private static final double MAX_BUFFER_STORAGE = 200;
	private static final Comparator<IEnergyGridProvider> COMPARATOR_HIGHEST_AMOUNT_STORED_FIRST = ( o1, o2 ) -> Double.compare( o2.getProviderStoredEnergy(),
			o1.getProviderStoredEnergy() );

	private static final Comparator<IEnergyGridProvider> COMPARATOR_LOWEST_PERCENTAGE_FIRST = ( o1, o2 ) ->
	{
		final double percent1 = ( o1.getProviderStoredEnergy() + 1 ) / ( o1.getProviderMaxEnergy() + 1 );
		final double percent2 = ( o2.getProviderStoredEnergy() + 1 ) / ( o2.getProviderMaxEnergy() + 1 );

		return Double.compare( percent1, percent2 );
	};

	private final NavigableSet<EnergyThreshold> interests = Sets.newTreeSet();
	private final double averageLength = 40.0;
	private final Set<IAEPowerStorage> providers = new LinkedHashSet<>();
	private final Set<IAEPowerStorage> requesters = new LinkedHashSet<>();
	private final Multiset<IEnergyGridProvider> energyGridProviders = HashMultiset.create();
	private final IGrid myGrid;
	private final HashMap<IGridNode, IEnergyWatcher> watchers = new HashMap<>();

	/**
	 * estimated power available.
	 */
	private int availableTicksSinceUpdate = 0;
	private double globalAvailablePower = 0;
	private double globalMaxPower = MAX_BUFFER_STORAGE;

	/**
	 * idle draw.
	 */
	private double drainPerTick = 0;
	private double avgDrainPerTick = 0;
	private double avgInjectionPerTick = 0;
	private double tickDrainPerTick = 0;
	private double tickInjectionPerTick = 0;

	/**
	 * power status
	 */
	private boolean publicHasPower = false;
	private boolean hasPower = true;
	private long ticksSinceHasPowerChange = 900;

	private PathGridCache pgc;
	private double lastStoredPower = -1;

	private final GridPowerStorage localStorage = new GridPowerStorage();

	public EnergyGridCache( final IGrid g )
	{
		this.myGrid = g;
		this.requesters.add( this.localStorage );
		this.providers.add( this.localStorage );
	}

	@MENetworkEventSubscribe
	public void postInit( final MENetworkPostCacheConstruction pcc )
	{
		this.pgc = this.myGrid.getCache( IPathingGrid.class );
	}

	@MENetworkEventSubscribe
	public void nodeIdlePowerChangeHandler( final MENetworkPowerIdleChange ev )
	{
		// update power usage based on event.
		final GridNode node = (GridNode) ev.node;
		final IGridBlock gb = node.getGridBlock();

		final double newDraw = gb.getIdlePowerUsage();
		final double diffDraw = newDraw - node.getPreviousDraw();
		node.setPreviousDraw( newDraw );

		this.drainPerTick += diffDraw;
	}

	@MENetworkEventSubscribe
	public void storagePowerChangeHandler( final MENetworkPowerStorage ev )
	{
		if( ev.storage.isAEPublicPowerStorage() )
		{
			switch( ev.type )
			{
				case PROVIDE_POWER:
					if( ev.storage.getPowerFlow() != AccessRestriction.WRITE )
					{
						this.providers.add( ev.storage );
					}
					break;
				case REQUEST_POWER:
					if( ev.storage.getPowerFlow() != AccessRestriction.READ )
					{
						this.requesters.add( ev.storage );
					}
					break;
			}
		}
		else
		{
			( new RuntimeException( "Attempt to ask the IEnergyGrid to charge a non public energy store." ) ).printStackTrace();
		}
	}

	@Override
	public void onUpdateTick()
	{
		if( !this.interests.isEmpty() )
		{
			final double oldPower = this.lastStoredPower;
			this.lastStoredPower = this.getStoredPower();

			final EnergyThreshold low = new EnergyThreshold( Math.min( oldPower, this.lastStoredPower ), Integer.MIN_VALUE );
			final EnergyThreshold high = new EnergyThreshold( Math.max( oldPower, this.lastStoredPower ), Integer.MAX_VALUE );

			for( final EnergyThreshold th : this.interests.subSet( low, true, high, true ) )
			{
				( (EnergyWatcher) th.getEnergyWatcher() ).post( this );
			}
		}

		this.avgDrainPerTick *= ( this.averageLength - 1 ) / this.averageLength;
		this.avgInjectionPerTick *= ( this.averageLength - 1 ) / this.averageLength;

		this.avgDrainPerTick += this.tickDrainPerTick / this.averageLength;
		this.avgInjectionPerTick += this.tickInjectionPerTick / this.averageLength;

		this.tickDrainPerTick = 0;
		this.tickInjectionPerTick = 0;

		// power information.
		boolean currentlyHasPower = false;

		if( this.drainPerTick > 0.0001 )
		{
			final double drained = this.extractAEPower( this.getIdlePowerUsage(), Actionable.MODULATE, PowerMultiplier.CONFIG );
			currentlyHasPower = drained >= this.drainPerTick - 0.001;
		}
		else
		{
			currentlyHasPower = this.extractAEPower( 0.1, Actionable.SIMULATE, PowerMultiplier.CONFIG ) > 0;
		}

		// ticks since change..
		if( currentlyHasPower == this.hasPower )
		{
			this.ticksSinceHasPowerChange++;
		}
		else
		{
			this.ticksSinceHasPowerChange = 0;
		}

		// update status..
		this.hasPower = currentlyHasPower;

		// update public status, this buffers power ups for 30 ticks.
		if( this.hasPower && this.ticksSinceHasPowerChange > 30 )
		{
			this.publicPowerState( true, this.myGrid );
		}
		else if( !this.hasPower )
		{
			this.publicPowerState( false, this.myGrid );
		}

		this.availableTicksSinceUpdate++;
	}

	@Override
	public double extractAEPower( final double amt, final Actionable mode, final PowerMultiplier pm )
	{
		final double toExtract = pm.multiply( amt );
		final Queue<IEnergyGridProvider> toVisit = new PriorityQueue<>( COMPARATOR_HIGHEST_AMOUNT_STORED_FIRST );
		final Set<IEnergyGridProvider> visited = new HashSet<>();

		double extracted = 0;
		toVisit.add( this );

		while( !toVisit.isEmpty() && extracted < toExtract )
		{
			final IEnergyGridProvider next = toVisit.poll();
			visited.add( next );

			extracted += next.extractProviderPower( toExtract - extracted, mode );

			for( IEnergyGridProvider iEnergyGridProvider : next.providers() )
			{
				if( !visited.contains( iEnergyGridProvider ) )
				{
					toVisit.add( iEnergyGridProvider );
				}
			}
		}

		return pm.divide( extracted );
	}

	@Override
	public double getIdlePowerUsage()
	{
		return this.drainPerTick + this.pgc.getChannelPowerUsage();
	}

	private void publicPowerState( final boolean newState, final IGrid grid )
	{
		if( this.publicHasPower == newState )
		{
			return;
		}

		this.publicHasPower = newState;
		( (Grid) this.myGrid ).setImportantFlag( 0, this.publicHasPower );
		grid.postEvent( new MENetworkPowerStatusChange() );
	}

	/**
	 * refresh current stored power.
	 */
	private void refreshPower()
	{
		this.availableTicksSinceUpdate = 0;
		this.globalAvailablePower = 0;
		for( final IAEPowerStorage p : this.providers )
		{
			this.globalAvailablePower += p.getAECurrentPower();
		}
	}

	@Override
	public Collection<IEnergyGridProvider> providers()
	{
		return this.energyGridProviders;
	}

	@Override
	public double extractProviderPower( final double amt, final Actionable mode )
	{
		double extractedPower = 0;

		final Iterator<IAEPowerStorage> it = this.providers.iterator();

		while( extractedPower < amt && it.hasNext() )
		{
			final IAEPowerStorage node = it.next();

			final double req = amt - extractedPower;
			final double newPower = node.extractAEPower( req, mode, PowerMultiplier.ONE );
			extractedPower += newPower;

			if( newPower < req && mode == Actionable.MODULATE )
			{
				it.remove();
			}
		}

		final double result = Math.min( extractedPower, amt );

		if( mode == Actionable.MODULATE )
		{
			if( extractedPower > amt )
			{
				this.localStorage.addCurrentAEPower( extractedPower - amt );
			}

			this.globalAvailablePower -= result;
			this.tickDrainPerTick += result;
		}

		return result;
	}

	@Override
	public double injectProviderPower( double amt, final Actionable mode )
	{
		final double originalAmount = amt;

		final Iterator<IAEPowerStorage> it = this.requesters.iterator();

		while( amt > 0 && it.hasNext() )
		{
			final IAEPowerStorage node = it.next();
			amt = node.injectAEPower( amt, mode );

			if( amt > 0 && mode == Actionable.MODULATE )
			{
				it.remove();
			}
		}

		final double overflow = Math.max( 0.0, amt );

		if( mode == Actionable.MODULATE )
		{
			this.tickInjectionPerTick += originalAmount - overflow;
		}

		return overflow;
	}

	@Override
	public double getProviderEnergyDemand( final double maxRequired )
	{
		double required = 0;

		final Iterator<IAEPowerStorage> it = this.requesters.iterator();
		while( required < maxRequired && it.hasNext() )
		{
			final IAEPowerStorage node = it.next();
			if( node.getPowerFlow() != AccessRestriction.READ )
			{
				required += Math.max( 0.0, node.getAEMaxPower() - node.getAECurrentPower() );
			}
		}

		return required;
	}

	@Override
	public double getAvgPowerUsage()
	{
		return this.avgDrainPerTick;
	}

	@Override
	public double getAvgPowerInjection()
	{
		return this.avgInjectionPerTick;
	}

	@Override
	public boolean isNetworkPowered()
	{
		return this.publicHasPower;
	}

	@Override
	public double injectPower( final double amt, final Actionable mode )
	{
		final Queue<IEnergyGridProvider> toVisit = new PriorityQueue<>( COMPARATOR_LOWEST_PERCENTAGE_FIRST );
		final Set<IEnergyGridProvider> visited = new HashSet<>();
		toVisit.add( this );

		double leftover = amt;

		while( !toVisit.isEmpty() && leftover > 0 )
		{
			final IEnergyGridProvider next = toVisit.poll();
			visited.add( next );

			leftover = next.injectProviderPower( leftover, mode );

			for( IEnergyGridProvider iEnergyGridProvider : next.providers() )
			{
				if( !visited.contains( iEnergyGridProvider ) )
				{
					toVisit.add( iEnergyGridProvider );
				}
			}
		}

		return leftover;
	}

	@Override
	public double getStoredPower()
	{
		if( this.availableTicksSinceUpdate > 90 )
		{
			this.refreshPower();
		}

		return Math.max( 0.0, this.globalAvailablePower );
	}

	@Override
	public double getMaxStoredPower()
	{
		return this.globalMaxPower;
	}

	@Override
	public double getEnergyDemand( final double maxRequired )
	{
		final Queue<IEnergyGridProvider> toVisit = new PriorityQueue<>( COMPARATOR_LOWEST_PERCENTAGE_FIRST );
		final Set<IEnergyGridProvider> visited = new HashSet<>();
		toVisit.add( this );

		double required = 0;

		while( !toVisit.isEmpty() && required < maxRequired )
		{
			final IEnergyGridProvider next = toVisit.poll();
			visited.add( next );

			required += next.getProviderEnergyDemand( maxRequired - required );

			for( IEnergyGridProvider iEnergyGridProvider : next.providers() )
			{
				if( !visited.contains( iEnergyGridProvider ) )
				{
					toVisit.add( iEnergyGridProvider );
				}
			}
		}

		return required;
	}

	@Override
	public double getProviderStoredEnergy()
	{
		return this.getStoredPower();
	}

	@Override
	public double getProviderMaxEnergy()
	{
		return this.getMaxStoredPower();
	}

	@Override
	public void removeNode( final IGridNode node, final IGridHost machine )
	{
		if( machine instanceof IEnergyGridProvider )
		{
			this.energyGridProviders.remove( machine );
		}

		// idle draw.
		final GridNode gridNode = (GridNode) node;
		this.drainPerTick -= gridNode.getPreviousDraw();

		// power storage.
		if( machine instanceof IAEPowerStorage )
		{
			final IAEPowerStorage ps = (IAEPowerStorage) machine;
			if( ps.isAEPublicPowerStorage() )
			{
				if( ps.getPowerFlow() != AccessRestriction.WRITE )
				{
					this.globalMaxPower -= ps.getAEMaxPower();
					this.globalAvailablePower -= ps.getAECurrentPower();
				}

				this.providers.remove( ps );
				this.requesters.remove( ps );
			}
		}

		if( machine instanceof IEnergyWatcherHost )
		{
			final IEnergyWatcher watcher = this.watchers.get( node );

			if( watcher != null )
			{
				watcher.reset();
				this.watchers.remove( node );
			}
		}
	}

	@Override
	public void addNode( final IGridNode node, final IGridHost machine )
	{
		if( machine instanceof IEnergyGridProvider )
		{
			this.energyGridProviders.add( (IEnergyGridProvider) machine );
		}

		// idle draw...
		final GridNode gridNode = (GridNode) node;
		final IGridBlock gb = gridNode.getGridBlock();
		gridNode.setPreviousDraw( gb.getIdlePowerUsage() );
		this.drainPerTick += gridNode.getPreviousDraw();

		// power storage
		if( machine instanceof IAEPowerStorage )
		{
			final IAEPowerStorage ps = (IAEPowerStorage) machine;
			if( ps.isAEPublicPowerStorage() )
			{
				final double max = ps.getAEMaxPower();
				final double current = ps.getAECurrentPower();

				if( ps.getPowerFlow() != AccessRestriction.WRITE )
				{
					this.globalMaxPower += ps.getAEMaxPower();
				}

				if( current > 0 && ps.getPowerFlow() != AccessRestriction.WRITE )
				{
					this.globalAvailablePower += current;
					this.providers.add( ps );
				}

				if( current < max && ps.getPowerFlow() != AccessRestriction.READ )
				{
					this.requesters.add( ps );
				}
			}
		}

		if( machine instanceof IEnergyWatcherHost )
		{
			final IEnergyWatcherHost swh = (IEnergyWatcherHost) machine;
			final EnergyWatcher iw = new EnergyWatcher( this, swh );

			this.watchers.put( node, iw );
			swh.updateWatcher( iw );
		}

		this.myGrid.postEventTo( node, new MENetworkPowerStatusChange() );
	}

	@Override
	public void onSplit( final IGridStorage storageB )
	{
		final double newBuffer = this.localStorage.getAECurrentPower() / 2;
		this.localStorage.removeCurrentAEPower( newBuffer );
		storageB.dataObject().setDouble( "buffer", newBuffer );
	}

	@Override
	public void onJoin( final IGridStorage storageB )
	{
		this.localStorage.addCurrentAEPower( storageB.dataObject().getDouble( "buffer" ) );
	}

	@Override
	public void populateGridStorage( final IGridStorage storage )
	{
		storage.dataObject().setDouble( "buffer", this.localStorage.getAECurrentPower() );
	}

	public boolean registerEnergyInterest( final EnergyThreshold threshold )
	{
		return this.interests.add( threshold );
	}

	public boolean unregisterEnergyInterest( final EnergyThreshold threshold )
	{
		return this.interests.remove( threshold );
	}

	private class GridPowerStorage implements IAEPowerStorage
	{
		private double stored = 0;

		@Override
		public double extractAEPower( double amt, Actionable mode, PowerMultiplier usePowerMultiplier )
		{
			double extracted = Math.min( amt, this.stored );

			if( mode == Actionable.MODULATE )
			{
				this.removeCurrentAEPower( extracted );
			}

			return extracted;
		}

		@Override
		public boolean isAEPublicPowerStorage()
		{
			return true;
		}

		@Override
		public double injectAEPower( double amt, Actionable mode )
		{
			double toStore = Math.min( amt, MAX_BUFFER_STORAGE - this.stored );

			if( mode == Actionable.MODULATE )
			{
				this.addCurrentAEPower( toStore );
			}

			return amt - toStore;
		}

		@Override
		public AccessRestriction getPowerFlow()
		{
			return AccessRestriction.READ_WRITE;
		}

		@Override
		public double getAEMaxPower()
		{
			return MAX_BUFFER_STORAGE;
		}

		@Override
		public double getAECurrentPower()
		{
			return this.stored;
		}

		private void addCurrentAEPower( double amount )
		{
			this.stored += amount;

			if( this.stored > 0.01 )
			{
				EnergyGridCache.this.myGrid.postEvent( new MENetworkPowerStorage( this, PowerEventType.PROVIDE_POWER ) );
			}
		}

		private void removeCurrentAEPower( double amount )
		{
			this.stored -= amount;

			if( this.stored < MAX_BUFFER_STORAGE - 0.001 )
			{
				EnergyGridCache.this.myGrid.postEvent( new MENetworkPowerStorage( this, PowerEventType.REQUEST_POWER ) );
			}

			if( this.stored < 0.01 )
			{
				EnergyGridCache.this.ticksSinceHasPowerChange = 0;
				EnergyGridCache.this.publicPowerState( false, EnergyGridCache.this.myGrid );
			}
		}
	}
}
