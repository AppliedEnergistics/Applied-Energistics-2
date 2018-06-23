
package appeng.fluids.helper;


import java.util.Optional;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate;
import net.minecraftforge.items.IItemHandler;

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
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.MEMonitorIFluidHandler;
import appeng.me.storage.MEMonitorPassThrough;
import appeng.me.storage.NullInventory;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;


public class DualityFluidInterface implements IGridTickable, IStorageMonitorable, IAEFluidInventory, IAEAppEngInventory
{
	public static final int NUMBER_OF_TANKS = 9;
	public static final int TANK_CAPACITY = Fluid.BUCKET_VOLUME * 4;

	private final AENetworkProxy gridProxy;
	private final IFluidInterfaceHost iHost;
	private final IActionSource mySource;
	private final IActionSource interfaceRequestSource;
	private boolean hasConfig = false;
	private final IStorageMonitorableAccessor accessor = ( src ) -> getMonitorable( src );
	private final AEFluidTank[] tanks;
	private final IFluidHandler storage;
	private final AppEngInternalAEInventory config = new AppEngInternalAEInventory( this, NUMBER_OF_TANKS );
	private final IAEFluidStack[] requireWork;
	private int isWorking = -1;

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

	public void markDirty()
	{
		// rescan inventory
		for( IFluidHandler ih : this.tanks )
		{
			onFluidInventoryChanged( ih );
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

	private int getTankSlot( final IFluidHandler inventory )
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
		final FluidStack req = this.getConfiguredFluid( slot );
		final FluidStack stored = this.tanks[slot].drain( TANK_CAPACITY, false );

		if( ( req == null || req.amount == 0 ) && ( stored != null && stored.amount > 0 ) )
		{
			final IAEFluidStack work = AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ).createStack( stored );
			this.requireWork[slot] = work.setStackSize( -work.getStackSize() );
			return;
		}
		else if( req != null && req.amount > 0 )
		{
			if( stored == null || stored.amount == 0 ) // need to add stuff!
			{
				this.requireWork[slot] = AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ).createStack( req );
				return;
			}
			else if( req.getFluid().equals( stored.getFluid() ) ) // same type ( qty different? )!
			{
				if( req.amount != stored.amount )
				{
					this.requireWork[slot] = AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ).createStack( req );
					this.requireWork[slot].setStackSize( req.amount - stored.amount );
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

	private FluidStack getConfiguredFluid( int slot )
	{
		ItemStack is = this.config.getStackInSlot( slot );
		if( !is.isEmpty() )
		{
			IFluidHandlerItem fh = is.getCapability( CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null );
			if( fh == null )
			{
				throw new NullPointerException( "Item did not give IFluidHandlerItem: " + is.getDisplayName() );
			}
			return fh.drain( Fluid.BUCKET_VOLUME, false );
		}
		return null;
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
					toStore.setStackSize( toStore.getStackSize() - notStored.getStackSize() );

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
	public void onFluidInventoryChanged( final IFluidHandler inventory )
	{
		final int slot = getTankSlot( inventory );
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

	@Override
	public void onChangeInventory( IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack )
	{
		if( this.isWorking == slot )
		{
			return;
		}

		if( inv == this.config )
		{
			this.readConfig();
		}
	}

	public void writeToNBT( final NBTTagCompound data )
	{
		final NBTTagList tankContents = new NBTTagList();
		for( int i = 0; i < NUMBER_OF_TANKS; ++i )
		{
			tankContents.appendTag( this.tanks[i].writeToNBT( new NBTTagCompound() ) );
		}

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
		this.readConfig();
	}

	public IItemHandler getConfig()
	{
		return this.config;
	}

	private class InterfaceRequestSource extends MachineSource
	{
		private final InterfaceRequestContext context;

		public InterfaceRequestSource( IActionHost v )
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

	private class InterfaceRequestContext
	{
	}

	private class InterfaceInventory extends MEMonitorIFluidHandler
	{

		public InterfaceInventory( final DualityFluidInterface tileInterface )
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
			final boolean isInterface = context.isPresent();

			if( isInterface )
			{
				return null;
			}

			return super.extractItems( request, type, src );
		}
	}

	@Override
	public void saveChanges()
	{
		this.iHost.getTileEntity().markDirty();
	}

}
