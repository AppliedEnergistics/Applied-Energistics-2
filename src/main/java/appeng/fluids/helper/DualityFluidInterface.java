/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.helper;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.capabilities.Capabilities;
import appeng.core.settings.TickRates;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.AEFluidTank;
import appeng.fluids.util.IAEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.MEMonitorIFluidHandler;
import appeng.me.storage.MEMonitorPassThrough;
import appeng.me.storage.NullInventory;
import appeng.util.Platform;


public class DualityFluidInterface implements IGridTickable, IStorageMonitorable, IAEFluidInventory, IPriorityHost
{
	public static final int NUMBER_OF_TANKS = 6;
	public static final int TANK_CAPACITY = Fluid.BUCKET_VOLUME * 4;

	private final AENetworkProxy gridProxy;
	private final IFluidInterfaceHost iHost;
	private final IActionSource mySource;
	private final IActionSource interfaceRequestSource;
	private boolean hasConfig = false;
	private final IStorageMonitorableAccessor accessor = this::getMonitorable;
	private final AEFluidTank[] tanks;
	private final IFluidHandler storage;
	private final AEFluidInventory config = new AEFluidInventory( this, NUMBER_OF_TANKS );
	private final IAEFluidStack[] requireWork;
	private boolean fluidsChanged = false;
	private int isWorking = -1;
	private int priority;

	private final MEMonitorPassThrough<IAEItemStack> items = new MEMonitorPassThrough<>( new NullInventory<IAEItemStack>(), AEApi.instance()
			.storage()
			.getStorageChannel( IItemStorageChannel.class ) );
	private final MEMonitorPassThrough<IAEFluidStack> fluids = new MEMonitorPassThrough<>( new NullInventory<IAEFluidStack>(), AEApi.instance()
			.storage()
			.getStorageChannel( IFluidStorageChannel.class ) );

	public DualityFluidInterface( final AENetworkProxy networkProxy, final IFluidInterfaceHost ih )
	{
		this.gridProxy = networkProxy;
		this.gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL );
		this.iHost = ih;

		this.mySource = new MachineSource( this.iHost );
		this.interfaceRequestSource = new InterfaceRequestSource( this.iHost );

		this.fluids.setChangeSource( this.mySource );
		this.items.setChangeSource( this.mySource );

		this.tanks = new AEFluidTank[NUMBER_OF_TANKS];
		this.requireWork = new IAEFluidStack[NUMBER_OF_TANKS];
		for( int i = 0; i < NUMBER_OF_TANKS; ++i )
		{
			this.tanks[i] = new AEFluidTank( this, TANK_CAPACITY );
			this.requireWork[i] = null;
		}
		this.storage = new FluidHandlerConcatenate( this.tanks );
	}

	@Override
	public <T extends IAEStack<T>> IMEMonitor<T> getInventory( IStorageChannel<T> channel )
	{
		if( channel == AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) )
		{
			if( this.hasConfig() )
			{
				return null;
			}

			return (IMEMonitor<T>) this.items;
		}
		else if( channel == AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) )
		{
			if( this.hasConfig() )
			{
				return (IMEMonitor<T>) new InterfaceInventory( this );
			}

			return (IMEMonitor<T>) this.fluids;
		}

		return null;
	}

	public IStorageMonitorable getMonitorable( final IActionSource src )
	{
		if( Platform.canAccess( this.gridProxy, src ) )
		{
			return this;
		}

		return null;
	}

	@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		return new TickingRequest( TickRates.Interface.getMin(), TickRates.Interface.getMax(), !this.hasWorkToDo(), true );
	}

	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int ticksSinceLastCall )
	{
		if( !this.gridProxy.isActive() )
		{
			return TickRateModulation.SLEEP;
		}

		final boolean couldDoWork = this.updateStorage();
		return this.hasWorkToDo() ? ( couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER ) : TickRateModulation.SLEEP;
	}

	public void notifyNeighbors()
	{
		if( this.gridProxy.isActive() )
		{
			try
			{
				this.gridProxy.getTick().wakeDevice( this.gridProxy.getNode() );
			}
			catch( final GridAccessException e )
			{
				// :P
			}
		}

		final TileEntity te = this.iHost.getTileEntity();
		if( te != null && te.getWorld() != null )
		{
			Platform.notifyBlocksOfNeighbors( te.getWorld(), te.getPos() );
		}
	}

	public void gridChanged()
	{
		try
		{
			this.items.setInternal( this.gridProxy.getStorage().getInventory( AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) ) );
			this.fluids.setInternal( this.gridProxy.getStorage().getInventory( AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) ) );
		}
		catch( final GridAccessException gae )
		{
			this.items.setInternal( new NullInventory<IAEItemStack>() );
			this.fluids.setInternal( new NullInventory<IAEFluidStack>() );
		}

		this.notifyNeighbors();
	}

	public AECableType getCableConnectionType( final AEPartLocation dir )
	{
		return AECableType.SMART;
	}

	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this.iHost.getTileEntity() );
	}

	public boolean hasCapability( Capability<?> capabilityClass, EnumFacing facing )
	{
		return capabilityClass == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capabilityClass == Capabilities.STORAGE_MONITORABLE_ACCESSOR;
	}

	@SuppressWarnings( "unchecked" )
	public <T> T getCapability( Capability<T> capabilityClass, EnumFacing facing )
	{
		if( capabilityClass == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY )
		{
			return (T) this.storage;
		}
		else if( capabilityClass == Capabilities.STORAGE_MONITORABLE_ACCESSOR )
		{
			return (T) this.accessor;
		}
		return null;
	}

	private boolean hasConfig()
	{
		return this.hasConfig;
	}

	private void readConfig()
	{
		final boolean had = this.hasWorkToDo();

		for( int x = 0; x < NUMBER_OF_TANKS; x++ )
		{
			this.updatePlan( x );
		}

		final boolean has = this.hasWorkToDo();

		if( had != has )
		{
			try
			{
				if( has )
				{
					this.gridProxy.getTick().alertDevice( this.gridProxy.getNode() );
				}
				else
				{
					this.gridProxy.getTick().sleepDevice( this.gridProxy.getNode() );
				}
			}
			catch( final GridAccessException e )
			{
				// :P
			}
		}

		this.notifyNeighbors();
	}

	private boolean updateStorage()
	{
		boolean didSomething = false;
		for( int x = 0; x < NUMBER_OF_TANKS; x++ )
		{
			if( this.requireWork[x] != null )
			{
				didSomething = this.usePlan( x ) || didSomething;
			}
		}
		return didSomething;
	}

	private boolean hasWorkToDo()
	{
		for( final IAEFluidStack requiredWork : this.requireWork )
		{
			if( requiredWork != null )
			{
				return true;
			}
		}

		return false;
	}

	private int getTankSlot( final IAEFluidTank inventory )
	{
		for( int i = 0; i < NUMBER_OF_TANKS; ++i )
		{
			if( this.tanks[i] == inventory )
			{
				return i;
			}
		}
		throw new IndexOutOfBoundsException();
	}

	private void updatePlan( final int slot )
	{
		final IAEFluidStack req = this.config.getFluidInSlot( slot );
		final FluidStack stored = this.tanks[slot].drain( TANK_CAPACITY, false );

		if( req == null && ( stored != null && stored.amount > 0 ) )
		{
			final IAEFluidStack work = AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ).createStack( stored );
			this.requireWork[slot] = work.setStackSize( -work.getStackSize() );
			return;
		}
		else if( req != null )
		{
			if( stored == null || stored.amount == 0 ) // need to add stuff!
			{
				this.requireWork[slot] = req.copy();
				this.requireWork[slot].setStackSize( TANK_CAPACITY );
				return;
			}
			else if( req.equals( stored ) ) // same type ( qty different? )!
			{
				if( stored.amount < TANK_CAPACITY )
				{
					this.requireWork[slot] = req.copy();
					this.requireWork[slot].setStackSize( TANK_CAPACITY - stored.amount );
					return;
				}
			}
			else
			// Stored != null; dispose!
			{
				final IAEFluidStack work = AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ).createStack( stored );
				this.requireWork[slot] = work.setStackSize( -work.getStackSize() );
				return;
			}
		}

		this.requireWork[slot] = null;
	}

	private boolean usePlan( final int slot )
	{
		IFluidHandler tank = this.tanks[slot];
		IAEFluidStack work = this.requireWork[slot];
		this.isWorking = slot;

		boolean changed = false;
		try
		{
			final IMEInventory<IAEFluidStack> dest = this.gridProxy.getStorage()
					.getInventory( AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) );
			final IEnergySource src = this.gridProxy.getEnergy();

			if( work.getStackSize() > 0 )
			{
				// make sure strange things didn't happen...
				if( tank.fill( work.getFluidStack(), false ) != work.getStackSize() )
				{
					changed = true;
				}
				else
				{
					final IAEFluidStack acquired = Platform.poweredExtraction( src, dest, work, this.interfaceRequestSource );
					if( acquired != null )
					{
						changed = true;
						final int filled = tank.fill( acquired.getFluidStack(), true );
						if( filled != acquired.getStackSize() )
						{
							throw new IllegalStateException( "bad attempt at managing tanks. ( fill )" );
						}
					}
				}
			}
			else if( work.getStackSize() < 0 )
			{
				IAEFluidStack toStore = work.copy();
				toStore.setStackSize( -toStore.getStackSize() );

				// make sure strange things didn't happen...
				final FluidStack canExtract = tank.drain( toStore.getFluidStack(), false );
				if( canExtract == null || canExtract.amount != toStore.getStackSize() )
				{
					changed = true;
				}
				else
				{
					IAEFluidStack notStored = Platform.poweredInsert( src, dest, toStore, this.interfaceRequestSource );
					toStore.setStackSize( toStore.getStackSize() - ( notStored == null ? 0 : notStored.getStackSize() ) );

					if( toStore.getStackSize() > 0 )
					{
						// extract items!
						changed = true;
						final FluidStack removed = tank.drain( toStore.getFluidStack(), true );
						if( removed == null || toStore.getStackSize() != removed.amount )
						{
							throw new IllegalStateException( "bad attempt at managing tanks. ( drain )" );
						}
					}
				}
			}
		}
		catch( final GridAccessException e )
		{
			// :P
		}

		if( changed )
		{
			this.updatePlan( slot );
		}

		this.isWorking = -1;
		return changed;
	}

	@Override
	public void onFluidInventoryChanged( final IAEFluidTank inventory, final int slotIndex )
	{
		this.fluidsChanged = true;

		if( inventory == this.config )
		{
			if( this.isWorking == slotIndex )
			{
				return;
			}
			this.readConfig();
		}
		else
		{
			final int slot = this.getTankSlot( inventory );

			this.saveChanges();

			if( this.isWorking == slot )
			{
				return;
			}

			final boolean had = this.hasWorkToDo();

			this.updatePlan( slot );

			final boolean now = this.hasWorkToDo();

			if( had != now )
			{
				try
				{
					if( now )
					{
						this.gridProxy.getTick().alertDevice( this.gridProxy.getNode() );
					}
					else
					{
						this.gridProxy.getTick().sleepDevice( this.gridProxy.getNode() );
					}
				}
				catch( final GridAccessException e )
				{
					// :P
				}
			}
		}
	}

	@Override
	public int getPriority()
	{
		return this.priority;
	}

	@Override
	public void setPriority( final int newValue )
	{
		this.priority = newValue;
	}

	public void writeToNBT( final NBTTagCompound data )
	{
		final NBTTagList tankContents = new NBTTagList();
		for( int i = 0; i < NUMBER_OF_TANKS; ++i )
		{
			tankContents.appendTag( this.tanks[i].writeToNBT( new NBTTagCompound() ) );
		}

		data.setInteger( "priority", this.priority );
		data.setTag( "storage", tankContents );
		this.config.writeToNBT( data, "config" );
	}

	public void readFromNBT( final NBTTagCompound data )
	{
		this.config.readFromNBT( data, "config" );
		final NBTTagList tankContents = data.getTagList( "storage", 10 );
		if( tankContents != null )
		{
			for( int i = 0; i < Math.min( NUMBER_OF_TANKS, tankContents.tagCount() ); ++i )
			{
				this.tanks[i].readFromNBT( tankContents.getCompoundTagAt( i ) );
			}
		}
		this.priority = data.getInteger( "priority" );
		this.readConfig();
	}

	public IAEFluidTank getConfig()
	{
		return this.config;
	}

	public AEFluidTank getTank( final int i )
	{
		return this.tanks[i];
	}

	public final Map<Integer, IAEFluidStack> writeFluidInfo( final boolean force )
	{
		final Map<Integer, IAEFluidStack> ret = new HashMap<>();
		if( force || this.fluidsChanged )
		{
			for( int i = 0; i < NUMBER_OF_TANKS; ++i )
			{
				ret.put( i, this.tanks[i].getFluidInSlot( 0 ) );
			}
			for( int i = 0; i < NUMBER_OF_TANKS; ++i )
			{
				ret.put( i + NUMBER_OF_TANKS, this.config.getFluidInSlot( i ) );
			}
			if( !force )
			{
				this.fluidsChanged = false;
			}
		}
		return ret;
	}

	public void readFluidSlots( Map<Integer, IAEFluidStack> tagMap )
	{
		for( int i = 0; i < NUMBER_OF_TANKS; ++i )
		{
			if( tagMap.containsKey( i ) )
			{
				this.tanks[i].setFluidInSlot( 0, tagMap.get( i ) );
			}
			if( tagMap.containsKey( i + NUMBER_OF_TANKS ) )
			{
				this.config.setFluidInSlot( i, tagMap.get( i + NUMBER_OF_TANKS ) );
			}
		}
	}

	private class InterfaceRequestSource extends MachineSource
	{
		private final InterfaceRequestContext context;

		InterfaceRequestSource( IActionHost v )
		{
			super( v );
			this.context = new InterfaceRequestContext();
		}

		@Override
		public <T> Optional<T> context( Class<T> key )
		{
			if( key == InterfaceRequestContext.class )
			{
				return (Optional<T>) Optional.of( this.context );
			}

			return super.context( key );
		}
	}

	private class InterfaceRequestContext implements Comparable<Integer>
	{
		@Override
		public int compareTo( Integer o )
		{
			return Integer.compare( DualityFluidInterface.this.priority, o );
		}
	}

	private class InterfaceInventory extends MEMonitorIFluidHandler
	{

		InterfaceInventory( final DualityFluidInterface tileInterface )
		{
			super( tileInterface.storage );
			this.setActionSource( new MachineSource( tileInterface.iHost ) );
		}

		@Override
		public IAEFluidStack injectItems( final IAEFluidStack input, final Actionable type, final IActionSource src )
		{
			final Optional<InterfaceRequestContext> context = src.context( InterfaceRequestContext.class );
			final boolean isInterface = context.isPresent();

			if( isInterface )
			{
				return input;
			}

			return super.injectItems( input, type, src );
		}

		@Override
		public IAEFluidStack extractItems( final IAEFluidStack request, final Actionable type, final IActionSource src )
		{
			final Optional<InterfaceRequestContext> context = src.context( InterfaceRequestContext.class );
			final boolean hasLowerOrEqualPriority = context.map( c -> c.compareTo( DualityFluidInterface.this.priority ) <= 0 ).orElse( false );

			if( hasLowerOrEqualPriority )
			{
				return null;
			}

			return super.extractItems( request, type, src );
		}
	}

	public void saveChanges()
	{
		this.iHost.getTileEntity().markDirty();
	}

}
