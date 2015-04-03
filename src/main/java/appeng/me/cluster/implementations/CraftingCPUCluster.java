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

package appeng.me.cluster.implementations;


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.common.FMLCommonHandler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CraftingItemList;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCraftingCpuChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.WorldCoord;
import appeng.container.ContainerNull;
import appeng.core.AELog;
import appeng.crafting.CraftBranchFailure;
import appeng.crafting.CraftingJob;
import appeng.crafting.CraftingLink;
import appeng.crafting.CraftingWatcher;
import appeng.crafting.MECraftingInventory;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.IAECluster;
import appeng.tile.crafting.TileCraftingMonitorTile;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;


public class CraftingCPUCluster implements IAECluster, ICraftingCPU
{

	public final WorldCoord min;
	public final WorldCoord max;
	final int[] usedOps = new int[3];
	final Map<ICraftingPatternDetails, TaskProgress> tasks = new HashMap<ICraftingPatternDetails, TaskProgress>();
	// INSTANCE sate
	final private LinkedList<TileCraftingTile> tiles = new LinkedList<TileCraftingTile>();
	final private LinkedList<TileCraftingTile> storage = new LinkedList<TileCraftingTile>();
	final private LinkedList<TileCraftingMonitorTile> status = new LinkedList<TileCraftingMonitorTile>();
	private final HashMap<IMEMonitorHandlerReceiver<IAEItemStack>, Object> listeners = new HashMap<IMEMonitorHandlerReceiver<IAEItemStack>, Object>();
	public ICraftingLink myLastLink;
	public String myName = "";
	public boolean isDestroyed = false;
	/**
	 * crafting job info
	 */
	MECraftingInventory inventory = new MECraftingInventory();
	IAEItemStack finalOutput;
	boolean waiting = false;
	IItemList<IAEItemStack> waitingFor = AEApi.instance().storage().createItemList();
	long availableStorage = 0;
	MachineSource machineSrc = null;
	int accelerator = 0;
	private boolean isComplete = true;
	private int remainingOperations;
	private boolean somethingChanged;

	public CraftingCPUCluster( WorldCoord _min, WorldCoord _max )
	{
		this.min = _min;
		this.max = _max;
	}

	/**
	 * add a new Listener to the monitor, be sure to properly remove yourself when your done.
	 */
	@Override
	public void addListener( IMEMonitorHandlerReceiver<IAEItemStack> l, Object verificationToken )
	{
		this.listeners.put( l, verificationToken );
	}

	/**
	 * remove a Listener to the monitor.
	 */
	@Override
	public void removeListener( IMEMonitorHandlerReceiver<IAEItemStack> l )
	{
		this.listeners.remove( l );
	}

	public IMEInventory<IAEItemStack> getInventory()
	{
		return this.inventory;
	}

	@Override
	public void updateStatus( boolean updateGrid )
	{
		for( TileCraftingTile r : this.tiles )
			r.updateMeta( true );
	}

	@Override
	public void destroy()
	{
		if( this.isDestroyed )
			return;
		this.isDestroyed = true;

		boolean posted = false;

		for( TileCraftingTile r : this.tiles )
		{
			IGridNode n = r.getActionableNode();
			if( n != null && !posted )
			{
				IGrid g = n.getGrid();
				if( g != null )
				{
					g.postEvent( new MENetworkCraftingCpuChange( n ) );
					posted = true;
				}
			}

			r.updateStatus( null );
		}
	}

	@Override
	public Iterator<IGridHost> getTiles()
	{
		return (Iterator) this.tiles.iterator();
	}

	public void addTile( TileCraftingTile te )
	{
		if( this.machineSrc == null || te.isCoreBlock )
			this.machineSrc = new MachineSource( te );

		te.isCoreBlock = false;
		te.markDirty();
		this.tiles.push( te );

		if( te.isStorage() )
		{
			this.availableStorage += te.getStorageBytes();
			this.storage.add( te );
		}
		else if( te.isStatus() )
			this.status.add( (TileCraftingMonitorTile) te );
		else if( te.isAccelerator() )
			this.accelerator++;
	}

	public boolean canAccept( IAEStack input )
	{
		if( input instanceof IAEItemStack )
		{
			IAEItemStack is = this.waitingFor.findPrecise( (IAEItemStack) input );
			if( is != null && is.getStackSize() > 0 )
				return true;
		}
		return false;
	}

	public IAEStack injectItems( IAEStack input, Actionable type, BaseActionSource src )
	{
		if( input instanceof IAEItemStack && type == Actionable.SIMULATE )// causes crafting to lock up?
		{
			IAEItemStack what = (IAEItemStack) input.copy();
			IAEItemStack is = this.waitingFor.findPrecise( what );

			if( is != null && is.getStackSize() > 0 )
			{
				if( is.getStackSize() >= what.getStackSize() )
				{
					if( this.finalOutput.equals( what ) )
					{
						if( this.myLastLink != null )
							return ( (CraftingLink) this.myLastLink ).injectItems( what.copy(), type );

						return what; // ignore it.
					}

					return null;
				}

				IAEItemStack leftOver = what.copy();
				leftOver.decStackSize( is.getStackSize() );

				IAEItemStack used = what.copy();
				used.setStackSize( is.getStackSize() );

				if( this.finalOutput.equals( what ) )
				{
					if( this.myLastLink != null )
					{
						leftOver.add( ( (CraftingLink) this.myLastLink ).injectItems( used.copy(), type ) );
						return leftOver;
					}

					return what; // ignore it.
				}

				return leftOver;
			}
		}
		else if( input instanceof IAEItemStack && type == Actionable.MODULATE )
		{
			IAEItemStack what = (IAEItemStack) input;
			IAEItemStack is = this.waitingFor.findPrecise( what );

			if( is != null && is.getStackSize() > 0 )
			{
				this.waiting = false;
				this.postChange( (IAEItemStack) input, src );

				if( is.getStackSize() >= input.getStackSize() )
				{
					is.decStackSize( input.getStackSize() );
					this.markDirty();
					this.postCraftingStatusChange( is );

					if( this.finalOutput.equals( input ) )
					{
						this.finalOutput.decStackSize( input.getStackSize() );
						if( this.finalOutput.getStackSize() <= 0 )
							this.completeJob();

						this.updateCPU();

						if( this.myLastLink != null )
							return ( (CraftingLink) this.myLastLink ).injectItems( (IAEItemStack) input, type );

						return input; // ignore it.
					}

					// 2000
					return this.inventory.injectItems( what, type, src );
				}

				IAEItemStack insert = what.copy();
				insert.setStackSize( is.getStackSize() );
				what.decStackSize( is.getStackSize() );

				is.setStackSize( 0 );

				if( this.finalOutput.equals( insert ) )
				{
					this.finalOutput.decStackSize( insert.getStackSize() );
					if( this.finalOutput.getStackSize() <= 0 )
						this.completeJob();

					this.updateCPU();

					if( this.myLastLink != null )
					{
						what.add( ( (CraftingLink) this.myLastLink ).injectItems( insert.copy(), type ) );
						return what;
					}

					if( this.myLastLink != null )
						return ( (CraftingLink) this.myLastLink ).injectItems( (IAEItemStack) input, type );

					return input; // ignore it.
				}

				this.inventory.injectItems( insert, type, src );
				this.markDirty();

				return what;
			}
		}

		return input;
	}

	protected void postChange( IAEItemStack diff, BaseActionSource src )
	{
		Iterator<Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object>> i = this.getListeners();

		ImmutableList<IAEItemStack> single = null;

		// protect integrity
		if( i.hasNext() )
			single = ImmutableList.of( diff.copy() );

		while( i.hasNext() )
		{
			Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object> o = i.next();
			IMEMonitorHandlerReceiver<IAEItemStack> receiver = o.getKey();
			if( receiver.isValid( o.getValue() ) )
				receiver.postChange( null, single, src );
			else
				i.remove();
		}
	}

	private void markDirty()
	{
		this.getCore().markDirty();
	}

	public void postCraftingStatusChange( IAEItemStack diff )
	{
		if( this.getGrid() == null )
			return;

		CraftingGridCache sg = this.getGrid().getCache( ICraftingGrid.class );

		if( sg.interestManager.containsKey( diff ) )
		{
			Collection<CraftingWatcher> list = sg.interestManager.get( diff );

			if( !list.isEmpty() )
			{
				for( CraftingWatcher iw : list )

					iw.getHost().onRequestChange( sg, diff );
			}
		}
	}

	private void completeJob()
	{
		if( this.myLastLink != null )
			( (CraftingLink) this.myLastLink ).markDone();

		AELog.crafting( "marking job as complete" );
		this.isComplete = true;
	}

	private void updateCPU()
	{
		IAEItemStack send = this.finalOutput;

		if( this.finalOutput != null && this.finalOutput.getStackSize() <= 0 )
			send = null;

		for( TileCraftingMonitorTile t : this.status )
			t.setJob( send );
	}

	protected Iterator<Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object>> getListeners()
	{
		return this.listeners.entrySet().iterator();
	}

	private TileCraftingTile getCore()
	{
		return (TileCraftingTile) this.machineSrc.via;
	}

	public IGrid getGrid()
	{
		for( TileCraftingTile r : this.tiles )
		{
			IGridNode gn = r.getActionableNode();
			if( gn != null )
			{
				IGrid g = gn.getGrid();
				if( g != null )
					return r.getActionableNode().getGrid();
			}
		}

		return null;
	}

	private boolean canCraft( ICraftingPatternDetails details, IAEItemStack[] condensedInputs )
	{
		for( IAEItemStack g : condensedInputs )
		{

			if( details.isCraftable() )
			{
				boolean found = false;

				for( IAEItemStack fuzz : this.inventory.getItemList().findFuzzy( g, FuzzyMode.IGNORE_ALL ) )
				{
					fuzz = fuzz.copy();
					fuzz.setStackSize( g.getStackSize() );
					IAEItemStack ais = this.inventory.extractItems( fuzz, Actionable.SIMULATE, this.machineSrc );
					ItemStack is = ais == null ? null : ais.getItemStack();

					if( is != null && is.stackSize == g.getStackSize() )
					{
						found = true;
						break;
					}
					else if( is != null )
					{
						g = g.copy();
						g.decStackSize( is.stackSize );
					}
				}

				if( !found )
					return false;
			}
			else
			{
				IAEItemStack ais = this.inventory.extractItems( g.copy(), Actionable.SIMULATE, this.machineSrc );
				ItemStack is = ais == null ? null : ais.getItemStack();

				if( is == null || is.stackSize < g.getStackSize() )
					return false;
			}
		}

		return true;
	}

	public void cancel()
	{
		if( this.myLastLink != null )
			this.myLastLink.cancel();

		IItemList<IAEItemStack> list;
		this.getListOfItem( list = AEApi.instance().storage().createItemList(), CraftingItemList.ALL );
		for( IAEItemStack is : list )
			this.postChange( is, this.machineSrc );

		this.isComplete = true;
		this.myLastLink = null;
		this.tasks.clear();

		ImmutableSet<IAEItemStack> items = ImmutableSet.copyOf( this.waitingFor );

		this.waitingFor.resetStatus();

		for( IAEItemStack is : items )
			this.postCraftingStatusChange( is );

		this.finalOutput = null;
		this.updateCPU();

		this.storeItems(); // marks dirty
	}

	public void updateCraftingLogic( IGrid grid, IEnergyGrid eg, CraftingGridCache cc )
	{
		if( !this.getCore().isActive() )
			return;

		if( this.myLastLink != null )
		{
			if( this.myLastLink.isCanceled() )
			{
				this.myLastLink = null;
				this.cancel();
			}
		}

		if( this.isComplete )
		{
			if( this.inventory.getItemList().isEmpty() )
				return;

			this.storeItems();
			return;
		}

		this.waiting = false;
		if( this.waiting || this.tasks.isEmpty() ) // nothing to do here...
			return;

		this.remainingOperations = this.accelerator + 1 - ( this.usedOps[0] + this.usedOps[1] + this.usedOps[2] );
		int started = this.remainingOperations;

		if( this.remainingOperations > 0 )
		{
			do
			{
				this.somethingChanged = false;
				this.executeCrafting( eg, cc );
			}
			while( this.somethingChanged && this.remainingOperations > 0 );
		}
		this.usedOps[2] = this.usedOps[1];
		this.usedOps[1] = this.usedOps[0];
		this.usedOps[0] = started - this.remainingOperations;

		if( this.remainingOperations > 0 && !this.somethingChanged )
			this.waiting = true;
	}

	private void executeCrafting( IEnergyGrid eg, CraftingGridCache cc )
	{
		Iterator<Entry<ICraftingPatternDetails, TaskProgress>> i = this.tasks.entrySet().iterator();
		while( i.hasNext() )
		{
			Entry<ICraftingPatternDetails, TaskProgress> e = i.next();
			if( e.getValue().value <= 0 )
			{
				i.remove();
				continue;
			}

			ICraftingPatternDetails details = e.getKey();
			if( this.canCraft( details, details.getCondensedInputs() ) )
			{
				InventoryCrafting ic = null;

				for( ICraftingMedium m : cc.getMediums( e.getKey() ) )
				{
					if( e.getValue().value <= 0 )
						continue;

					if( !m.isBusy() )
					{
						if( ic == null )
						{
							IAEItemStack[] input = details.getInputs();

							double sum = 0;
							for( IAEItemStack anInput : input )
							{
								if( anInput != null )
								{
									sum += anInput.getStackSize();
								}
							}

							// power...
							if( eg.extractAEPower( sum, Actionable.MODULATE, PowerMultiplier.CONFIG ) < sum - 0.01 )
								continue;

							ic = new InventoryCrafting( new ContainerNull(), 3, 3 );
							boolean found = false;

							for( int x = 0; x < input.length; x++ )
							{

								if( input[x] != null )
								{
									found = false;
									if( details.isCraftable() )
									{
										for( IAEItemStack fuzz : this.inventory.getItemList().findFuzzy( input[x], FuzzyMode.IGNORE_ALL ) )
										{
											fuzz = fuzz.copy();
											fuzz.setStackSize( input[x].getStackSize() );

											if( details.isValidItemForSlot( x, fuzz.getItemStack(), this.getWorld() ) )
											{
												IAEItemStack ais = this.inventory.extractItems( fuzz, Actionable.MODULATE, this.machineSrc );
												ItemStack is = ais == null ? null : ais.getItemStack();

												if( is != null )
												{
													this.postChange( AEItemStack.create( is ), this.machineSrc );
													ic.setInventorySlotContents( x, is );
													found = true;
													break;
												}
											}
										}
									}
									else
									{
										IAEItemStack ais = this.inventory.extractItems( input[x].copy(), Actionable.MODULATE, this.machineSrc );
										ItemStack is = ais == null ? null : ais.getItemStack();

										if( is != null )
										{
											this.postChange( input[x], this.machineSrc );
											ic.setInventorySlotContents( x, is );
											if( is.stackSize == input[x].getStackSize() )
											{
												found = true;
												continue;
											}
										}
									}

									if( !found )
										break;
								}
							}

							if( !found )
							{
								// put stuff back..
								for( int x = 0; x < ic.getSizeInventory(); x++ )
								{
									ItemStack is = ic.getStackInSlot( x );
									if( is != null )
										this.inventory.injectItems( AEItemStack.create( is ), Actionable.MODULATE, this.machineSrc );
								}
								ic = null;
								break;
							}
						}

						if( m.pushPattern( details, ic ) )
						{
							this.somethingChanged = true;
							this.remainingOperations--;

							for( IAEItemStack out : details.getCondensedOutputs() )
							{
								this.postChange( out, this.machineSrc );
								this.waitingFor.add( out.copy() );
								this.postCraftingStatusChange( out.copy() );
							}

							if( details.isCraftable() )
							{
								FMLCommonHandler.instance().firePlayerCraftingEvent( Platform.getPlayer( (WorldServer) this.getWorld() ), details.getOutput( ic, this.getWorld() ), ic );

								for( int x = 0; x < ic.getSizeInventory(); x++ )
								{
									ItemStack output = Platform.getContainerItem( ic.getStackInSlot( x ) );
									if( output != null )
									{
										IAEItemStack cItem = AEItemStack.create( output );
										this.postChange( cItem, this.machineSrc );
										this.waitingFor.add( cItem );
										this.postCraftingStatusChange( cItem );
									}
								}
							}

							ic = null; // hand off complete!
							this.markDirty();

							e.getValue().value--;
							if( e.getValue().value <= 0 )
								continue;

							if( this.remainingOperations == 0 )
								return;
						}
					}
				}

				if( ic != null )
				{
					// put stuff back..
					for( int x = 0; x < ic.getSizeInventory(); x++ )
					{
						ItemStack is = ic.getStackInSlot( x );
						if( is != null )
						{
							this.inventory.injectItems( AEItemStack.create( is ), Actionable.MODULATE, this.machineSrc );
						}
					}
				}
			}
		}
	}

	private void storeItems()
	{
		IGrid g = this.getGrid();
		if( g == null )
			return;

		IStorageGrid sg = g.getCache( IStorageGrid.class );
		IMEInventory<IAEItemStack> ii = sg.getItemInventory();

		for( IAEItemStack is : this.inventory.getItemList() )
		{
			is = this.inventory.extractItems( is.copy(), Actionable.MODULATE, this.machineSrc );

			if( is != null )
			{
				this.postChange( is, this.machineSrc );
				is = ii.injectItems( is, Actionable.MODULATE, this.machineSrc );
			}

			if( is != null )
				this.inventory.injectItems( is, Actionable.MODULATE, this.machineSrc );
		}

		if( this.inventory.getItemList().isEmpty() )
			this.inventory = new MECraftingInventory();

		this.markDirty();
	}

	public ICraftingLink submitJob( IGrid g, ICraftingJob job, BaseActionSource src, ICraftingRequester requestingMachine )
	{
		if( !this.tasks.isEmpty() || !this.waitingFor.isEmpty() )
			return null;

		if( !( job instanceof CraftingJob ) )
			return null;

		if( this.isBusy() || !this.isActive() || this.availableStorage < job.getByteTotal() )
			return null;

		IStorageGrid sg = g.getCache( IStorageGrid.class );
		IMEInventory<IAEItemStack> storage = sg.getItemInventory();
		MECraftingInventory ci = new MECraftingInventory( storage, true, false, false );

		try
		{
			this.waitingFor.resetStatus();
			( (CraftingJob) job ).tree.setJob( ci, this, src );
			if( ci.commit( src ) )
			{
				this.finalOutput = job.getOutput();
				this.waiting = false;
				this.isComplete = false;
				this.markDirty();

				this.updateCPU();
				String craftID = this.generateCraftingID();

				this.myLastLink = new CraftingLink( this.generateLinkData( craftID, requestingMachine == null, false ), this );

				if( requestingMachine == null )
					return this.myLastLink;

				ICraftingLink whatLink = new CraftingLink( this.generateLinkData( craftID, false, true ), requestingMachine );

				this.submitLink( this.myLastLink );
				this.submitLink( whatLink );

				IItemList<IAEItemStack> list;
				this.getListOfItem( list = AEApi.instance().storage().createItemList(), CraftingItemList.ALL );

				for( IAEItemStack ge : list )
					this.postChange( ge, this.machineSrc );

				return whatLink;
			}
			else
			{
				this.tasks.clear();
				this.inventory.getItemList().resetStatus();
			}
		}
		catch( CraftBranchFailure e )
		{
			this.tasks.clear();
			this.inventory.getItemList().resetStatus();
			// AELog.error( e );
		}

		return null;
	}

	@Override
	public boolean isBusy()
	{
		Iterator<Entry<ICraftingPatternDetails, TaskProgress>> i = this.tasks.entrySet().iterator();
		while( i.hasNext() )
		{
			if( i.next().getValue().value <= 0 )
				i.remove();
		}

		return !this.tasks.isEmpty() || !this.waitingFor.isEmpty();
	}

	@Override
	public BaseActionSource getActionSource()
	{
		return this.machineSrc;
	}

	@Override
	public long getAvailableStorage()
	{
		return this.availableStorage;
	}

	@Override
	public int getCoProcessors()
	{
		return this.accelerator;
	}

	@Override
	public String getName()
	{
		return this.myName;
	}

	public boolean isActive()
	{
		TileCraftingTile core = this.getCore();
		if( core == null )
			return false;

		IGridNode node = core.getActionableNode();
		if( node == null )
			return false;

		return node.isActive();
	}

	private String generateCraftingID()
	{
		long now = System.currentTimeMillis();
		int hash = System.identityHashCode( this );
		int hmm = this.finalOutput == null ? 0 : this.finalOutput.hashCode();

		return Long.toString( now, Character.MAX_RADIX ) + '-' + Integer.toString( hash, Character.MAX_RADIX ) + '-' + Integer.toString( hmm, Character.MAX_RADIX );
	}

	private NBTTagCompound generateLinkData( String craftingID, boolean standalone, boolean req )
	{
		NBTTagCompound tag = new NBTTagCompound();

		tag.setString( "CraftID", craftingID );
		tag.setBoolean( "canceled", false );
		tag.setBoolean( "done", false );
		tag.setBoolean( "standalone", standalone );
		tag.setBoolean( "req", req );

		return tag;
	}

	private void submitLink( ICraftingLink myLastLink2 )
	{
		if( this.getGrid() != null )
		{
			CraftingGridCache cc = this.getGrid().getCache( ICraftingGrid.class );
			cc.addLink( (CraftingLink) myLastLink2 );
		}
	}

	public void getListOfItem( IItemList<IAEItemStack> list, CraftingItemList whichList )
	{
		switch( whichList )
		{
			case ACTIVE:
				for( IAEItemStack ais : this.waitingFor )
					list.add( ais );
				break;
			case PENDING:
				for( Entry<ICraftingPatternDetails, TaskProgress> t : this.tasks.entrySet() )
				{
					for( IAEItemStack ais : t.getKey().getCondensedOutputs() )
					{
						ais = ais.copy();
						ais.setStackSize( ais.getStackSize() * t.getValue().value );
						list.add( ais );
					}
				}
				break;
			case STORAGE:
				this.inventory.getAvailableItems( list );
				break;
			default:
			case ALL:
				this.inventory.getAvailableItems( list );

				for( IAEItemStack ais : this.waitingFor )
					list.add( ais );

				for( Entry<ICraftingPatternDetails, TaskProgress> t : this.tasks.entrySet() )
				{
					for( IAEItemStack ais : t.getKey().getCondensedOutputs() )
					{
						ais = ais.copy();
						ais.setStackSize( ais.getStackSize() * t.getValue().value );
						list.add( ais );
					}
				}
				break;
		}
	}

	public void addStorage( IAEItemStack extractItems )
	{
		this.inventory.injectItems( extractItems, Actionable.MODULATE, null );
	}

	public void addEmitable( IAEItemStack i )
	{
		this.waitingFor.add( i );
		this.postCraftingStatusChange( i );
	}

	public void addCrafting( ICraftingPatternDetails details, long crafts )
	{
		TaskProgress i = this.tasks.get( details );

		if( i == null )
			this.tasks.put( details, i = new TaskProgress() );

		i.value += crafts;
	}

	public IAEItemStack getItemStack( IAEItemStack what, CraftingItemList storage2 )
	{
		IAEItemStack is = null;
		switch( storage2 )
		{
			case STORAGE:
				is = this.inventory.getItemList().findPrecise( what );
				break;
			case ACTIVE:
				is = this.waitingFor.findPrecise( what );
				break;
			case PENDING:

				is = what.copy();
				is.setStackSize( 0 );

				for( Entry<ICraftingPatternDetails, TaskProgress> t : this.tasks.entrySet() )
				{
					for( IAEItemStack ais : t.getKey().getCondensedOutputs() )
					{
						if( ais.equals( is ) )
							is.setStackSize( is.getStackSize() + ais.getStackSize() * t.getValue().value );
					}
				}

				break;
			default:
			case ALL:
				throw new RuntimeException( "Invalid Operation" );
		}

		if( is != null )
			return is.copy();

		is = what.copy();
		is.setStackSize( 0 );
		return is;
	}

	public void writeToNBT( NBTTagCompound data )
	{
		data.setTag( "finalOutput", this.writeItem( this.finalOutput ) );
		data.setTag( "inventory", this.writeList( this.inventory.getItemList() ) );
		data.setBoolean( "waiting", this.waiting );
		data.setBoolean( "isComplete", this.isComplete );

		if( this.myLastLink != null )
		{
			NBTTagCompound link = new NBTTagCompound();
			this.myLastLink.writeToNBT( link );
			data.setTag( "link", link );
		}

		NBTTagList list = new NBTTagList();
		for( Entry<ICraftingPatternDetails, TaskProgress> e : this.tasks.entrySet() )
		{
			NBTTagCompound item = this.writeItem( AEItemStack.create( e.getKey().getPattern() ) );
			item.setLong( "craftingProgress", e.getValue().value );
			list.appendTag( item );
		}
		data.setTag( "tasks", list );

		data.setTag( "waitingFor", this.writeList( this.waitingFor ) );
	}

	private NBTTagCompound writeItem( IAEItemStack finalOutput2 )
	{
		NBTTagCompound out = new NBTTagCompound();

		if( finalOutput2 != null )
			finalOutput2.writeToNBT( out );

		return out;
	}

	private NBTTagList writeList( IItemList<IAEItemStack> myList )
	{
		NBTTagList out = new NBTTagList();

		for( IAEItemStack ais : myList )
			out.appendTag( this.writeItem( ais ) );

		return out;
	}

	public void done()
	{
		TileCraftingTile core = this.getCore();

		core.isCoreBlock = true;

		if( core.previousState != null )
		{
			this.readFromNBT( core.previousState );
			core.previousState = null;
		}

		this.updateCPU();
		this.updateName();
	}

	public void readFromNBT( NBTTagCompound data )
	{
		this.finalOutput = AEItemStack.loadItemStackFromNBT( (NBTTagCompound) data.getTag( "finalOutput" ) );
		for( IAEItemStack ais : this.readList( (NBTTagList) data.getTag( "inventory" ) ) )
			this.inventory.injectItems( ais, Actionable.MODULATE, this.machineSrc );

		this.waiting = data.getBoolean( "waiting" );
		this.isComplete = data.getBoolean( "isComplete" );

		if( data.hasKey( "link" ) )
		{
			NBTTagCompound link = data.getCompoundTag( "link" );
			this.myLastLink = new CraftingLink( link, this );
			this.submitLink( this.myLastLink );
		}

		NBTTagList list = data.getTagList( "tasks", 10 );
		for( int x = 0; x < list.tagCount(); x++ )
		{
			NBTTagCompound item = list.getCompoundTagAt( x );
			IAEItemStack pattern = AEItemStack.loadItemStackFromNBT( item );
			if( pattern != null && pattern.getItem() instanceof ICraftingPatternItem )
			{
				ICraftingPatternItem cpi = (ICraftingPatternItem) pattern.getItem();
				ICraftingPatternDetails details = cpi.getPatternForItem( pattern.getItemStack(), this.getWorld() );
				if( details != null )
				{
					TaskProgress tp = new TaskProgress();
					tp.value = item.getLong( "craftingProgress" );
					this.tasks.put( details, tp );
				}
			}
		}

		this.waitingFor = this.readList( (NBTTagList) data.getTag( "waitingFor" ) );
		for( IAEItemStack is : this.waitingFor )
			this.postCraftingStatusChange( is.copy() );
	}

	public void updateName()
	{
		this.myName = "";
		for( TileCraftingTile te : this.tiles )
		{

			if( te.hasCustomName() )
			{
				if( this.myName.length() > 0 )
					this.myName += ' ' + te.getCustomName();
				else
					this.myName = te.getCustomName();
			}
		}
	}

	private IItemList<IAEItemStack> readList( NBTTagList tag )
	{
		IItemList<IAEItemStack> out = AEApi.instance().storage().createItemList();
		if( tag == null )
			return out;

		for( int x = 0; x < tag.tagCount(); x++ )
		{
			IAEItemStack ais = AEItemStack.loadItemStackFromNBT( tag.getCompoundTagAt( x ) );
			if( ais != null )
				out.add( ais );
		}

		return out;
	}

	private World getWorld()
	{
		return this.getCore().getWorldObj();
	}

	public boolean isMaking( IAEItemStack what )
	{
		IAEItemStack wat = this.waitingFor.findPrecise( what );
		return wat != null && wat.getStackSize() > 0;
	}

	public void breakCluster()
	{
		TileCraftingTile t = this.getCore();
		if( t != null )
			t.breakCluster();
	}

	static class TaskProgress
	{

		long value;
	}
}
