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


import java.util.*;

import appeng.parts.automation.PartLevelEmitter;
import com.google.common.base.Preconditions;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.me.cache.helpers.TickTracker;


public class TickManagerCache implements ITickManager
{

	private final IGrid myGrid;
	private final HashMap<IGridNode, TickTracker> alertable = new HashMap<>();
	private final HashMap<IGridNode, TickTracker> sleeping = new HashMap<>();
	private final HashMap<IGridNode, TickTracker> awake = new HashMap<>();
	private final HashMap<IGridNode, TickTracker> laterTicker = new HashMap<>();
	private final PriorityQueue<TickTracker> upcomingTicks = new PriorityQueue<>();

	private long currentTick = 0;

	public TickManagerCache( final IGrid g )
	{
		this.myGrid = g;
	}

	public long getCurrentTick()
	{
		return this.currentTick;
	}

	public long getAvgNanoTime( final IGridNode node )
	{
		TickTracker tt = this.awake.get( node );

		if( tt == null )
		{
			tt = this.sleeping.get( node );
		}

		if( tt == null )
		{
			return -1;
		}

		return tt.getAvgNanos();
	}

	@Override
	public void onUpdateTick()
	{
		TickTracker tt = null;

		try
		{
			this.currentTick++;

			while ( !this.upcomingTicks.isEmpty() )
			{
				tt = this.upcomingTicks.peek();

				// Stop once it reaches a TickTracker running at a later tick
				if( tt.getNextTick() > this.currentTick )
				{
					break;
				}

				this.upcomingTicks.poll();

				final int diff = (int) ( this.currentTick - tt.getLastTick() );

				if( tt.getGridTickable() instanceof PartLevelEmitter )
				{
					laterTicker.put( tt.getNode(), tt );
				} else
				{

					final TickRateModulation mod = tt.getGridTickable().tickingRequest( tt.getNode(), diff );

					switch ( mod )
					{
						case FASTER:
							tt.setCurrentRate( tt.getCurrentRate() - 2 );
							break;
						case IDLE:
							tt.setCurrentRate( tt.getRequest().maxTickRate );
							break;
						case SAME:
							break;
						case SLEEP:
							this.sleepDevice( tt.getNode() );
							break;
						case SLOWER:
							tt.setCurrentRate( tt.getCurrentRate() + 1 );
							break;
						case URGENT:
							tt.setCurrentRate( 0 );
							break;
						default:
							break;
					}
				}
				if( this.awake.containsKey( tt.getNode() ) )
				{
					this.addToQueue( tt );
				}
			}
			Iterator<Map.Entry<IGridNode, TickTracker>> ltIterator = laterTicker.entrySet().iterator();
			while ( ltIterator.hasNext() )
			{
				Map.Entry<IGridNode, TickTracker> tickTracker = ltIterator.next();
				tickTracker.getValue().getGridTickable().tickingRequest( tickTracker.getKey(), 1 );
				ltIterator.remove();
			}
		PartLevelEmitter.wipeCaches();
		}
		catch( final Throwable t )
		{
			final CrashReport crashreport = CrashReport.makeCrashReport( t, "Ticking GridNode" );
			final CrashReportCategory crashreportcategory = crashreport.makeCategory( tt.getGridTickable().getClass().getSimpleName() + " being ticked." );
			tt.addEntityCrashInfo( crashreportcategory );
			throw new ReportedException( crashreport );
		}
	}

	private void addToQueue( final TickTracker tt )
	{
		tt.setLastTick( this.currentTick );
		this.upcomingTicks.add( tt );
	}

	@Override
	public void removeNode( final IGridNode gridNode, final IGridHost machine )
	{
		if( machine instanceof IGridTickable )
		{
			this.alertable.remove( gridNode );
			this.sleeping.remove( gridNode );
			this.awake.remove( gridNode );
		}
	}

	@Override
	public void addNode( final IGridNode gridNode, final IGridHost machine )
	{
		if( machine instanceof IGridTickable )
		{
			final IGridTickable tickable = ( (IGridTickable) machine );
			final TickingRequest tr = tickable.getTickingRequest( gridNode );

			Preconditions.checkNotNull( tr );

			final TickTracker tt = new TickTracker( tr, gridNode, (IGridTickable) machine, this.currentTick, this );

			if( tr.canBeAlerted )
			{
				this.alertable.put( gridNode, tt );
			}

			if( tr.isSleeping )
			{
				this.sleeping.put( gridNode, tt );
			}
			else
			{
				this.awake.put( gridNode, tt );
				this.addToQueue( tt );
			}
		}
	}

	@Override
	public void onSplit( final IGridStorage storageB )
	{

	}

	@Override
	public void onJoin( final IGridStorage storageB )
	{

	}

	@Override
	public void populateGridStorage( final IGridStorage storage )
	{

	}

	@Override
	public boolean alertDevice( final IGridNode node )
	{
		Preconditions.checkNotNull( node );

		final TickTracker tt = this.alertable.get( node );
		if( tt == null )
		{
			return false;
		}
		// throw new RuntimeException(
		// "Invalid alerted device, this node is not marked as alertable, or part of this grid." );

		// set to awake, this is for sanity.
		this.sleeping.remove( node );
		this.awake.put( node, tt );

		// configure sort.
		tt.setLastTick( tt.getLastTick() - tt.getRequest().maxTickRate );
		tt.setCurrentRate( tt.getRequest().minTickRate );

		// prevent dupes and tick build up.
		this.upcomingTicks.remove( tt );
		this.upcomingTicks.add( tt );

		return true;
	}

	@Override
	public boolean sleepDevice( final IGridNode node )
	{
		Preconditions.checkNotNull( node );

		if( this.awake.containsKey( node ) )
		{
			final TickTracker gt = this.awake.get( node );
			this.awake.remove( node );
			this.sleeping.put( node, gt );

			return true;
		}

		return false;
	}

	@Override
	public boolean wakeDevice( final IGridNode node )
	{
		Preconditions.checkNotNull( node );

		if( this.sleeping.containsKey( node ) )
		{
			final TickTracker gt = this.sleeping.get( node );
			this.sleeping.remove( node );
			this.awake.put( node, gt );
			this.upcomingTicks.remove( gt );
			this.addToQueue( gt );

			return true;
		}

		return false;
	}
}
