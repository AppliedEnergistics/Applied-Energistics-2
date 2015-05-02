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


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.google.common.base.Stopwatch;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCallback;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.hooks.TickHandler;


public class CraftingJob implements Runnable, ICraftingJob
{

	final IItemList<IAEItemStack> storage;
	final Set<IAEItemStack> prophecies;
	final MECraftingInventory original;
	final World world;
	final IItemList<IAEItemStack> crafting = AEApi.instance().storage().createItemList();
	final IItemList<IAEItemStack> missing = AEApi.instance().storage().createItemList();
	final Map<String, TwoIntegers> opsAndMultiplier = new HashMap<String, TwoIntegers>();
	private final Object monitor = new Object();
	private final Stopwatch watch = Stopwatch.createUnstarted();
	public CraftingTreeNode tree;
	IAEItemStack output;
	boolean simulate = false;
	MECraftingInventory availableCheck;
	long bytes = 0;
	private BaseActionSource actionSrc;
	private ICraftingCallback callback;
	private boolean running = false;
	private boolean done = false;
	private int time = 5;
	private int incTime = Integer.MAX_VALUE;

	public CraftingJob( World w, NBTTagCompound data )
	{
		this.world = this.wrapWorld( w );
		this.storage = AEApi.instance().storage().createItemList();
		this.prophecies = new HashSet<IAEItemStack>();
		this.original = null;
		this.availableCheck = null;
	}

	private World wrapWorld( World w )
	{
		return w;
	}

	public CraftingJob( World w, IGrid grid, BaseActionSource actionSrc, IAEItemStack what, ICraftingCallback callback )
	{
		this.world = this.wrapWorld( w );
		this.output = what.copy();
		this.storage = AEApi.instance().storage().createItemList();
		this.prophecies = new HashSet<IAEItemStack>();
		this.actionSrc = actionSrc;

		this.callback = callback;
		ICraftingGrid cc = grid.getCache( ICraftingGrid.class );
		IStorageGrid sg = grid.getCache( IStorageGrid.class );
		this.original = new MECraftingInventory( sg.getItemInventory(), actionSrc, false, false, false );

		this.tree = this.getCraftingTree( cc, what );
		this.availableCheck = null;
	}

	private CraftingTreeNode getCraftingTree( ICraftingGrid cc, IAEItemStack what )
	{
		return new CraftingTreeNode( cc, this, what, null, -1, 0 );
	}

	public void refund( IAEItemStack o )
	{
		this.availableCheck.injectItems( o, Actionable.MODULATE, this.actionSrc );
	}

	public IAEItemStack checkUse( IAEItemStack available )
	{
		return this.availableCheck.extractItems( available, Actionable.MODULATE, this.actionSrc );
	}

	public void writeToNBT( NBTTagCompound out )
	{

	}

	public void addTask( IAEItemStack what, long crafts, ICraftingPatternDetails details, int depth )
	{
		if( crafts > 0 )
		{
			what = what.copy();
			what.setStackSize( what.getStackSize() * crafts );
			this.crafting.add( what );
		}
	}

	public void addMissing( IAEItemStack what )
	{
		what = what.copy();
		this.missing.add( what );
	}

	@Override
	public void run()
	{
		try
		{
			try
			{
				TickHandler.INSTANCE.registerCraftingSimulation( this.world, this );
				this.handlePausing();

				Stopwatch timer = Stopwatch.createStarted();

				MECraftingInventory craftingInventory = new MECraftingInventory( this.original, true, false, true );
				craftingInventory.ignore( this.output );

				this.availableCheck = new MECraftingInventory( this.original, false, false, false );
				this.tree.request( craftingInventory, this.output.getStackSize(), this.actionSrc );
				this.tree.dive( this );

				for( String s : this.opsAndMultiplier.keySet() )
				{
					TwoIntegers ti = this.opsAndMultiplier.get( s );
					AELog.crafting( s + " * " + ti.times + " = " + ( ti.perOp * ti.times ) );
				}

				AELog.crafting( "------------- " + this.bytes + "b real" + timer.elapsed( TimeUnit.MILLISECONDS ) + "ms" );
				// if ( mode == Actionable.MODULATE )
				// craftingInventory.moveItemsToStorage( storage );
			}
			catch( CraftBranchFailure e )
			{
				this.simulate = true;

				try
				{
					Stopwatch timer = Stopwatch.createStarted();
					MECraftingInventory craftingInventory = new MECraftingInventory( this.original, true, false, true );
					craftingInventory.ignore( this.output );

					this.availableCheck = new MECraftingInventory( this.original, false, false, false );

					this.tree.setSimulate();
					this.tree.request( craftingInventory, this.output.getStackSize(), this.actionSrc );
					this.tree.dive( this );

					for( String s : this.opsAndMultiplier.keySet() )
					{
						TwoIntegers ti = this.opsAndMultiplier.get( s );
						AELog.crafting( s + " * " + ti.times + " = " + ( ti.perOp * ti.times ) );
					}

					AELog.crafting( "------------- " + this.bytes + "b simulate" + timer.elapsed( TimeUnit.MILLISECONDS ) + "ms" );
				}
				catch( CraftBranchFailure e1 )
				{
					AELog.error( e1 );
				}
				catch( CraftingCalculationFailure f )
				{
					AELog.error( f );
				}
				catch( InterruptedException e1 )
				{
					AELog.crafting( "Crafting calculation canceled." );
					this.finish();
					return;
				}
			}
			catch( CraftingCalculationFailure f )
			{
				AELog.error( f );
			}
			catch( InterruptedException e1 )
			{
				AELog.crafting( "Crafting calculation canceled." );
				this.finish();
				return;
			}

			this.log( "crafting job now done" );
		}
		catch( Throwable t )
		{
			this.finish();
			throw new IllegalStateException( t );
		}

		this.finish();
	}

	public void handlePausing() throws InterruptedException
	{
		if( this.incTime > 100 )
		{
			this.incTime = 0;

			synchronized( this.monitor )
			{
				if( this.watch.elapsed( TimeUnit.MICROSECONDS ) > this.time )
				{
					this.running = false;
					this.watch.stop();
					this.monitor.notify();
				}

				if( !this.running )
				{
					this.log( "crafting job will now sleep" );

					while( !this.running )
					{
						this.monitor.wait();
					}

					this.log( "crafting job now active" );
				}
			}

			if( Thread.interrupted() )
			{
				throw new InterruptedException();
			}
		}
		this.incTime++;
	}

	public void finish()
	{
		if( this.callback != null )
		{
			this.callback.calculationComplete( this );
		}

		this.availableCheck = null;

		synchronized( this.monitor )
		{
			this.running = false;
			this.done = true;
			this.monitor.notify();
		}
	}

	private void log( String string )
	{
		// AELog.crafting( string );
	}

	@Override
	public boolean isSimulation()
	{
		return this.simulate;
	}

	@Override
	public long getByteTotal()
	{
		return this.bytes;
	}

	@Override
	public void populatePlan( IItemList<IAEItemStack> plan )
	{
		if( this.tree != null )
		{
			this.tree.getPlan( plan );
		}
	}

	@Override
	public IAEItemStack getOutput()
	{
		return this.output;
	}

	public boolean isDone()
	{
		return this.done;
	}

	public World getWorld()
	{
		return this.world;
	}

	/**
	 * returns true if this needs more simulation.
	 *
	 * @param milli milliseconds of simulation
	 *
	 * @return true if this needs more simulation
	 */
	public boolean simulateFor( int milli )
	{
		this.time = milli;

		synchronized( this.monitor )
		{
			if( this.done )
			{
				return false;
			}

			this.watch.reset();
			this.watch.start();
			this.running = true;

			this.log( "main thread is now going to sleep" );

			this.monitor.notify();

			while( this.running )
			{
				try
				{
					this.monitor.wait();
				}
				catch( InterruptedException ignored )
				{
				}
			}

			this.log( "main thread is now active" );
		}

		return true;
	}

	public void addBytes( long crafts )
	{
		this.bytes += crafts;
	}

	static class TwoIntegers
	{

		public final long perOp = 0;
		public final long times = 0;
	}
}
