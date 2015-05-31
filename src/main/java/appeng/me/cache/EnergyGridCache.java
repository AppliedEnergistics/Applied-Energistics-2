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

package appeng.me.cache;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

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
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.me.Grid;
import appeng.me.GridNode;
import appeng.me.energy.EnergyThreshold;
import appeng.me.energy.EnergyWatcher;


public final class EnergyGridCache implements IEnergyGrid
{

	public final TreeSet<EnergyThreshold> interests = new TreeSet<EnergyThreshold>();
	final double AvgLength = 40.0;
	final Set<IAEPowerStorage> providers = new LinkedHashSet<IAEPowerStorage>();
	final Set<IAEPowerStorage> requesters = new LinkedHashSet<IAEPowerStorage>();
	final Multiset<IEnergyGridProvider> energyGridProviders = HashMultiset.create();
	final IGrid myGrid;
	private final HashMap<IGridNode, IEnergyWatcher> watchers = new HashMap<IGridNode, IEnergyWatcher>();
	private final Set<IEnergyGrid> localSeen = new HashSet<IEnergyGrid>();
	/**
	 * estimated power available.
	 */
	int availableTicksSinceUpdate = 0;
	double globalAvailablePower = 0;
	double globalMaxPower = 0;
	/**
	 * idle draw.
	 */
	double drainPerTick = 0;
	double avgDrainPerTick = 0;
	double avgInjectionPerTick = 0;
	double tickDrainPerTick = 0;
	double tickInjectionPerTick = 0;
	/**
	 * power status
	 */
	boolean publicHasPower = false;
	boolean hasPower = true;
	long ticksSinceHasPowerChange = 900;
	/**
	 * excess power in the system.
	 */
	double extra = 0;
	IAEPowerStorage lastProvider;
	IAEPowerStorage lastRequester;
	PathGridCache pgc;
	double lastStoredPower = -1;

	public EnergyGridCache( IGrid g )
	{
		this.myGrid = g;
	}

	@MENetworkEventSubscribe
	public void postInit( MENetworkPostCacheConstruction pcc )
	{
		this.pgc = this.myGrid.getCache( IPathingGrid.class );
	}

	@MENetworkEventSubscribe
	public void EnergyNodeChanges( MENetworkPowerIdleChange ev )
	{
		// update power usage based on event.
		GridNode node = (GridNode) ev.node;
		IGridBlock gb = node.getGridBlock();

		double newDraw = gb.getIdlePowerUsage();
		double diffDraw = newDraw - node.previousDraw;
		node.previousDraw = newDraw;

		this.drainPerTick += diffDraw;
	}

	@MENetworkEventSubscribe
	public void EnergyNodeChanges( MENetworkPowerStorage ev )
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
	public final void onUpdateTick()
	{
		if( !this.interests.isEmpty() )
		{
			double oldPower = this.lastStoredPower;
			this.lastStoredPower = this.getStoredPower();

			EnergyThreshold low = new EnergyThreshold( Math.min( oldPower, this.lastStoredPower ), null );
			EnergyThreshold high = new EnergyThreshold( Math.max( oldPower, this.lastStoredPower ), null );
			for( EnergyThreshold th : this.interests.subSet( low, true, high, true ) )
			{
				( (EnergyWatcher) th.watcher ).post( this );
			}
		}

		this.avgDrainPerTick *= ( this.AvgLength - 1 ) / this.AvgLength;
		this.avgInjectionPerTick *= ( this.AvgLength - 1 ) / this.AvgLength;

		this.avgDrainPerTick += this.tickDrainPerTick / this.AvgLength;
		this.avgInjectionPerTick += this.tickInjectionPerTick / this.AvgLength;

		this.tickDrainPerTick = 0;
		this.tickInjectionPerTick = 0;

		// power information.
		boolean currentlyHasPower = false;

		if( this.drainPerTick > 0.0001 )
		{
			double drained = this.extractAEPower( this.getIdlePowerUsage(), Actionable.MODULATE, PowerMultiplier.CONFIG );
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
	public final double extractAEPower( double amt, Actionable mode, PowerMultiplier pm )
	{
		this.localSeen.clear();
		return pm.divide( this.extractAEPower( pm.multiply( amt ), mode, this.localSeen ) );
	}

	@Override
	public final double getIdlePowerUsage()
	{
		return this.drainPerTick + this.pgc.channelPowerUsage;
	}

	private void publicPowerState( boolean newState, IGrid grid )
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
	public final void refreshPower()
	{
		this.availableTicksSinceUpdate = 0;
		this.globalAvailablePower = 0;
		for( IAEPowerStorage p : this.providers )
		{
			this.globalAvailablePower += p.getAECurrentPower();
		}
	}

	@Override
	public final double extractAEPower( double amt, Actionable mode, Set<IEnergyGrid> seen )
	{
		if( !seen.add( this ) )
		{
			return 0;
		}

		double extractedPower = this.extra;

		if( mode == Actionable.SIMULATE )
		{
			extractedPower += this.simulateExtract( extractedPower, amt );

			if( extractedPower < amt )
			{
				Iterator<IEnergyGridProvider> i = this.energyGridProviders.iterator();
				while( extractedPower < amt && i.hasNext() )
				{
					extractedPower += i.next().extractAEPower( amt - extractedPower, mode, seen );
				}
			}

			return extractedPower;
		}
		else
		{
			this.extra = 0;
			extractedPower = this.doExtract( extractedPower, amt );
		}

		// got more then we wanted?
		if( extractedPower > amt )
		{
			this.extra = extractedPower - amt;
			this.globalAvailablePower -= amt;

			this.tickDrainPerTick += amt;
			return amt;
		}

		if( extractedPower < amt )
		{
			Iterator<IEnergyGridProvider> i = this.energyGridProviders.iterator();
			while( extractedPower < amt && i.hasNext() )
			{
				extractedPower += i.next().extractAEPower( amt - extractedPower, mode, seen );
			}
		}

		// go less or the correct amount?
		this.globalAvailablePower -= extractedPower;
		this.tickDrainPerTick += extractedPower;
		return extractedPower;
	}

	@Override
	public final double injectAEPower( double amt, Actionable mode, Set<IEnergyGrid> seen )
	{
		if( !seen.add( this ) )
		{
			return 0;
		}

		double ignore = this.extra;
		amt += this.extra;

		if( mode == Actionable.SIMULATE )
		{
			Iterator<IAEPowerStorage> it = this.requesters.iterator();
			while( amt > 0 && it.hasNext() )
			{
				IAEPowerStorage node = it.next();
				amt = node.injectAEPower( amt, Actionable.SIMULATE );
			}

			Iterator<IEnergyGridProvider> i = this.energyGridProviders.iterator();
			while( amt > 0 && i.hasNext() )
			{
				amt = i.next().injectAEPower( amt, mode, seen );
			}
		}
		else
		{
			this.tickInjectionPerTick += amt - ignore;
			// totalInjectionPastTicks[0] += i;

			while( amt > 0 && !this.requesters.isEmpty() )
			{
				IAEPowerStorage node = this.getFirstRequester();

				amt = node.injectAEPower( amt, Actionable.MODULATE );
				if( amt > 0 )
				{
					this.requesters.remove( node );
					this.lastRequester = null;
				}
			}

			Iterator<IEnergyGridProvider> i = this.energyGridProviders.iterator();
			while( amt > 0 && i.hasNext() )
			{
				IEnergyGridProvider what = i.next();
				Set<IEnergyGrid> listCopy = new HashSet<IEnergyGrid>();
				listCopy.addAll( seen );

				double cannotHold = what.injectAEPower( amt, Actionable.SIMULATE, listCopy );
				what.injectAEPower( amt - cannotHold, mode, seen );

				amt = cannotHold;
			}

			this.extra = amt;
		}

		return Math.max( 0.0, amt - this.buffer() );
	}

	@Override
	public final double getEnergyDemand( double maxRequired, Set<IEnergyGrid> seen )
	{
		if( !seen.add( this ) )
		{
			return 0;
		}

		double required = this.buffer() - this.extra;

		Iterator<IAEPowerStorage> it = this.requesters.iterator();
		while( required < maxRequired && it.hasNext() )
		{
			IAEPowerStorage node = it.next();
			if( node.getPowerFlow() != AccessRestriction.READ )
			{
				required += Math.max( 0.0, node.getAEMaxPower() - node.getAECurrentPower() );
			}
		}

		Iterator<IEnergyGridProvider> ix = this.energyGridProviders.iterator();
		while( required < maxRequired && ix.hasNext() )
		{
			IEnergyGridProvider node = ix.next();
			required += node.getEnergyDemand( maxRequired - required, seen );
		}

		return required;
	}

	private double simulateExtract( double extractedPower, double amt )
	{
		Iterator<IAEPowerStorage> it = this.providers.iterator();

		while( extractedPower < amt && it.hasNext() )
		{
			IAEPowerStorage node = it.next();

			double req = amt - extractedPower;
			double newPower = node.extractAEPower( req, Actionable.SIMULATE, PowerMultiplier.ONE );
			extractedPower += newPower;
		}

		return extractedPower;
	}

	private double doExtract( double extractedPower, double amt )
	{
		while( extractedPower < amt && !this.providers.isEmpty() )
		{
			IAEPowerStorage node = this.getFirstProvider();

			double req = amt - extractedPower;
			double newPower = node.extractAEPower( req, Actionable.MODULATE, PowerMultiplier.ONE );
			extractedPower += newPower;

			if( newPower < req )
			{
				this.providers.remove( node );
				this.lastProvider = null;
			}
		}

		// totalDrainPastTicks[0] += extractedPower;
		return extractedPower;
	}

	private IAEPowerStorage getFirstProvider()
	{
		if( this.lastProvider == null )
		{
			Iterator<IAEPowerStorage> i = this.providers.iterator();
			this.lastProvider = i.hasNext() ? i.next() : null;
		}

		return this.lastProvider;
	}

	@Override
	public final double getAvgPowerUsage()
	{
		return this.avgDrainPerTick;
	}

	@Override
	public final double getAvgPowerInjection()
	{
		return this.avgInjectionPerTick;
	}

	@Override
	public final boolean isNetworkPowered()
	{
		return this.publicHasPower;
	}

	@Override
	public final double injectPower( double amt, Actionable mode )
	{
		this.localSeen.clear();
		return this.injectAEPower( amt, mode, this.localSeen );
	}

	private IAEPowerStorage getFirstRequester()
	{
		if( this.lastRequester == null )
		{
			Iterator<IAEPowerStorage> i = this.requesters.iterator();
			this.lastRequester = i.hasNext() ? i.next() : null;
		}

		return this.lastRequester;
	}

	private double buffer()
	{
		return this.providers.isEmpty() ? 1000.0 : 0.0;
	}

	@Override
	public final double getStoredPower()
	{
		if( this.availableTicksSinceUpdate > 90 )
		{
			this.refreshPower();
		}

		return Math.max( 0.0, this.globalAvailablePower );
	}

	@Override
	public final double getMaxStoredPower()
	{
		return this.globalMaxPower;
	}

	@Override
	public final double getEnergyDemand( double maxRequired )
	{
		this.localSeen.clear();
		return this.getEnergyDemand( maxRequired, this.localSeen );
	}

	@Override
	public final void removeNode( IGridNode node, IGridHost machine )
	{
		if( machine instanceof IEnergyGridProvider )
		{
			this.energyGridProviders.remove( machine );
		}

		// idle draw.
		GridNode gridNode = (GridNode) node;
		this.drainPerTick -= gridNode.previousDraw;

		// power storage.
		if( machine instanceof IAEPowerStorage )
		{
			IAEPowerStorage ps = (IAEPowerStorage) machine;
			if( ps.isAEPublicPowerStorage() )
			{
				if( ps.getPowerFlow() != AccessRestriction.WRITE )
				{
					this.globalMaxPower -= ps.getAEMaxPower();
					this.globalAvailablePower -= ps.getAECurrentPower();
				}

				if( this.lastProvider == machine )
				{
					this.lastProvider = null;
				}

				if( this.lastRequester == machine )
				{
					this.lastRequester = null;
				}

				this.providers.remove( machine );
				this.requesters.remove( machine );
			}
		}

		if( machine instanceof IStackWatcherHost )
		{
			IEnergyWatcher myWatcher = this.watchers.get( machine );
			if( myWatcher != null )
			{
				myWatcher.clear();
				this.watchers.remove( machine );
			}
		}
	}

	@Override
	public final void addNode( IGridNode node, IGridHost machine )
	{
		if( machine instanceof IEnergyGridProvider )
		{
			this.energyGridProviders.add( (IEnergyGridProvider) machine );
		}

		// idle draw...
		GridNode gridNode = (GridNode) node;
		IGridBlock gb = gridNode.getGridBlock();
		gridNode.previousDraw = gb.getIdlePowerUsage();
		this.drainPerTick += gridNode.previousDraw;

		// power storage
		if( machine instanceof IAEPowerStorage )
		{
			IAEPowerStorage ps = (IAEPowerStorage) machine;
			if( ps.isAEPublicPowerStorage() )
			{
				double max = ps.getAEMaxPower();
				double current = ps.getAECurrentPower();

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
			IEnergyWatcherHost swh = (IEnergyWatcherHost) machine;
			EnergyWatcher iw = new EnergyWatcher( this, swh );
			this.watchers.put( node, iw );
			swh.updateWatcher( iw );
		}

		this.myGrid.postEventTo( node, new MENetworkPowerStatusChange() );
	}

	@Override
	public final void onSplit( IGridStorage storageB )
	{
		this.extra /= 2;
		storageB.dataObject().setDouble( "extraEnergy", this.extra );
	}

	@Override
	public final void onJoin( IGridStorage storageB )
	{
		this.extra += storageB.dataObject().getDouble( "extraEnergy" );
	}

	@Override
	public final void populateGridStorage( IGridStorage storage )
	{
		storage.dataObject().setDouble( "extraEnergy", this.extra );
	}
}
