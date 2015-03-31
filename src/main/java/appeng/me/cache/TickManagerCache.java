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


import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

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
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.me.cache.helpers.TickTracker;


public class TickManagerCache implements ITickManager
{

	private long currentTick = 0;

	final IGrid myGrid;

	private final Stopwatch stopWatch;

	public TickManagerCache( IGrid g )
	{
		this.myGrid = g;
		this.stopWatch = Stopwatch.createUnstarted();
	}

	final HashMap<IGridNode, TickTracker> alertable = new HashMap<IGridNode, TickTracker>();

	final HashMap<IGridNode, TickTracker> sleeping = new HashMap<IGridNode, TickTracker>();
	final HashMap<IGridNode, TickTracker> awake = new HashMap<IGridNode, TickTracker>();

	final PriorityQueue<TickTracker> upcomingTicks = new PriorityQueue<TickTracker>();

	public long getCurrentTick()
	{
		return this.currentTick;
	}

	public long getAvgNanoTime( IGridNode node )
	{
		TickTracker tt = this.awake.get( node );

		if ( tt == null )
			tt = this.sleeping.get( node );

		if ( tt == null )
			return -1;

		return tt.getAvgNanos();
	}

	@Override
	public void onUpdateTick()
	{
		TickTracker tt = null;
		try
		{
			this.currentTick++;

			final boolean isTiDi = AEConfig.instance.timeDilationEnabled;
			int tiDiFactor = 0;
			double timeLimitation = 1;

			if ( isTiDi )
			{
				final double baseTime = AEConfig.instance.timeDilationTimePerTick * 5000;
				final double networkModifier = this.myGrid.getNodes().size() / ( AEConfig.instance.timeDilationGridSizeMultiplier + 1 ) + 1;
				timeLimitation = baseTime * networkModifier / ( this.upcomingTicks.size() + 1 );
			}

			while ( !this.upcomingTicks.isEmpty() )
			{

				tt = this.upcomingTicks.peek();
				int diff = ( int ) ( this.currentTick - tt.lastTick );
				if ( diff >= tt.current_rate )
				{
					// remove tt..
					this.upcomingTicks.poll();

					if ( isTiDi )
					{
						this.stopWatch.reset();
						this.stopWatch.start();
					}

					TickRateModulation mod = tt.gt.tickingRequest( tt.node, diff );

					if ( isTiDi )
					{
						this.stopWatch.stop();
						final long elapsedTime = this.stopWatch.elapsed( TimeUnit.MICROSECONDS );
						tiDiFactor = ( int ) ( elapsedTime / timeLimitation );
					}

					switch ( mod )
					{
						case FASTER:
							tt.setRate( tt.current_rate - 2 + tiDiFactor );
							break;
						case IDLE:
							tt.setRate( tt.request.maxTickRate );
							break;
						case SAME:
							break;
						case SLEEP:
							this.sleepDevice( tt.node );
							break;
						case SLOWER:
							tt.setRate( tt.current_rate + 1 + tiDiFactor );
							break;
						case URGENT:
							tt.setRate( 0 + tiDiFactor );
							break;
						default:
							break;
					}

					if ( this.awake.containsKey( tt.node ) )
						this.addToQueue( tt );
				}
				else
					break; // done!
			}

		}
		catch ( Throwable t )
		{
			CrashReport crashreport = CrashReport.makeCrashReport( t, "Ticking GridNode" );
			CrashReportCategory crashreportcategory = crashreport.makeCategory( tt.gt.getClass().getSimpleName() + " being ticked." );
			tt.addEntityCrashInfo( crashreportcategory );
			throw new ReportedException( crashreport );
		}
	}

	private void addToQueue( TickTracker tt )
	{
		tt.lastTick = this.currentTick;
		this.upcomingTicks.add( tt );
	}

	@Override
	public boolean alertDevice( IGridNode node )
	{
		TickTracker tt = this.alertable.get( node );
		if ( tt == null )
			return false;
		// throw new RuntimeException(
		// "Invalid alerted device, this node is not marked as alertable, or part of this grid." );

		// set to awake, this is for sanity.
		this.sleeping.remove( node );
		this.awake.put( node, tt );

		// configure sort.
		tt.lastTick -= tt.request.maxTickRate;
		tt.current_rate = tt.request.minTickRate;

		// prevent dupes and tick build up.
		this.upcomingTicks.remove( tt );
		this.upcomingTicks.add( tt );

		return true;
	}

	@Override
	public boolean sleepDevice( IGridNode node )
	{
		if ( this.awake.containsKey( node ) )
		{
			TickTracker gt = this.awake.get( node );
			this.awake.remove( node );
			this.sleeping.put( node, gt );

			return true;
		}

		return false;
	}

	@Override
	public boolean wakeDevice( IGridNode node )
	{
		if ( this.sleeping.containsKey( node ) )
		{
			TickTracker gt = this.sleeping.get( node );
			this.sleeping.remove( node );
			this.awake.put( node, gt );
			this.addToQueue( gt );

			return true;
		}

		return false;
	}

	@Override
	public void removeNode( IGridNode gridNode, IGridHost machine )
	{
		if ( machine instanceof IGridTickable )
		{
			this.alertable.remove( gridNode );
			this.sleeping.remove( gridNode );
			this.awake.remove( gridNode );
		}
	}

	@Override
	public void addNode( IGridNode gridNode, IGridHost machine )
	{
		if ( machine instanceof IGridTickable )
		{
			TickingRequest tr = ( ( IGridTickable ) machine ).getTickingRequest( gridNode );
			if ( tr != null )
			{
				TickTracker tt = new TickTracker( tr, gridNode, ( IGridTickable ) machine, this.currentTick, this );

				if ( tr.canBeAlerted )
					this.alertable.put( gridNode, tt );

				if ( tr.isSleeping )
					this.sleeping.put( gridNode, tt );
				else
				{
					this.awake.put( gridNode, tt );
					this.addToQueue( tt );
				}

			}
		}
	}

	@Override
	public void onSplit( IGridStorage storageB )
	{

	}

	@Override
	public void onJoin( IGridStorage storageB )
	{

	}

	@Override
	public void populateGridStorage( IGridStorage storage )
	{

	}
}
