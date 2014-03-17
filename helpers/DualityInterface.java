package appeng.helpers;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigureableObject;
import appeng.core.settings.TickRates;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.storage.MEMonitorIInventory;
import appeng.me.storage.MEMonitorPassthu;
import appeng.me.storage.NullInventory;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorIInventory;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.WrapperInvSlot;

public class DualityInterface implements IGridTickable, ISegmentedInventory, IStorageMonitorable, IInventoryDestination, IAEAppEngInventory,
		IConfigureableObject, IConfigManagerHost
{

	final int sides[] = new int[] { 0, 1, 2, 3, 4, 5, 6, 7 };
	final IAEItemStack requireWork[] = new IAEItemStack[] { null, null, null, null, null, null, null, null };

	boolean hasConfig = false;
	AENetworkProxy gridProxy;
	IInterfaceHost iHost;
	BaseActionSource mySrc;
	ConfigManager cm = new ConfigManager( this );

	public DualityInterface(AENetworkProxy prox, IInterfaceHost ih) {
		gridProxy = prox;
		gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL );

		iHost = ih;
		mySrc = fluids.changeSource = items.changeSource = new MachineSource( iHost );
	}

	@Override
	public void saveChanges()
	{
		iHost.saveChanges();
	}

	private void readConfig()
	{
		boolean hadConfig = hasConfig;

		hasConfig = false;

		for (ItemStack p : config)
		{
			if ( p != null )
			{
				hasConfig = true;
				break;
			}
		}

		boolean had = hasWorkToDo();

		for (int x = 0; x < 8; x++)
			updatePlan( x );

		boolean has = hasWorkToDo();

		if ( had != has )
		{
			try
			{
				if ( has )
					gridProxy.getTick().alertDevice( gridProxy.getNode() );
				else
					gridProxy.getTick().sleepDevice( gridProxy.getNode() );
			}
			catch (GridAccessException e)
			{
				// :P
			}
		}

		TileEntity te = iHost.getTileEntity();
		if ( hadConfig != hasConfig && te != null && te.getWorldObj() != null )
		{
			te.getWorldObj().notifyBlocksOfNeighborChange( te.xCoord, te.yCoord, te.zCoord, Platform.air );
		}
	}

	public void writeToNBT(NBTTagCompound data)
	{
		config.writeToNBT( data, "config" );
		patterns.writeToNBT( data, "patterns" );
		storage.writeToNBT( data, "storage" );
	}

	public void readFromNBT(NBTTagCompound data)
	{
		config.readFromNBT( data, "config" );
		patterns.readFromNBT( data, "patterns" );
		storage.readFromNBT( data, "storage" );
		readConfig();
	}

	AppEngInternalAEInventory config = new AppEngInternalAEInventory( this, 8 );
	AppEngInternalInventory storage = new AppEngInternalInventory( this, 8 );
	AppEngInternalInventory patterns = new AppEngInternalInventory( this, 9 );

	WrapperInvSlot slotInv = new WrapperInvSlot( storage );
	InventoryAdaptor adaptor = new AdaptorIInventory( slotInv );

	IMEInventory<IAEItemStack> destination;
	private boolean isWorking = false;

	@Override
	public boolean canInsert(ItemStack stack)
	{
		IAEItemStack out = destination.injectItems( AEApi.instance().storage().createItemStack( stack ), Actionable.SIMULATE, null );
		if ( out == null )
			return true;
		return out.getStackSize() != stack.stackSize;
		// ItemStack after = adaptor.simulateAdd( stack );
		// if ( after == null )
		// return true;
		// return after.stackSize != stack.stackSize;
	}

	private void updatePlan(int slot)
	{
		IAEItemStack req = config.getAEStackInSlot( slot );
		ItemStack Stored = storage.getStackInSlot( slot );

		if ( req == null && Stored != null )
		{
			IAEItemStack work = AEApi.instance().storage().createItemStack( Stored );
			requireWork[slot] = work.setStackSize( -work.getStackSize() );
			return;
		}
		else if ( req != null )
		{
			if ( Stored == null ) // need to add stuff!
			{
				requireWork[slot] = req.copy();
				return;
			}
			else if ( req.isSameType( Stored ) ) // same type ( qty diffrent? )!
			{
				if ( req.getStackSize() != Stored.stackSize )
				{
					requireWork[slot] = req.copy();
					requireWork[slot].setStackSize( req.getStackSize() - Stored.stackSize );
					return;
				}
			}
			else if ( Stored != null ) // dispose!
			{
				IAEItemStack work = AEApi.instance().storage().createItemStack( Stored );
				requireWork[slot] = work.setStackSize( -work.getStackSize() );
				return;
			}
		}

		// else

		requireWork[slot] = null;
	}

	static private boolean interfaceRequest = false;

	class InterfaceInventory extends MEMonitorIInventory
	{

		public InterfaceInventory(DualityInterface tileInterface) {
			super( tileInterface.storage, ForgeDirection.UP );
			mySource = new MachineSource( iHost );
		}

		@Override
		public IAEItemStack injectItems(IAEItemStack input, Actionable type, BaseActionSource src)
		{
			if ( interfaceRequest )
				return input;

			return super.injectItems( input, type, src );
		}

		@Override
		public IAEItemStack extractItems(IAEItemStack request, Actionable type, BaseActionSource src)
		{
			if ( interfaceRequest )
				return null;

			return super.extractItems( request, type, src );
		}

	};

	private boolean usePlan(int x, IAEItemStack itemStack)
	{
		boolean changed = false;
		slotInv.setSlot( x );
		interfaceRequest = isWorking = true;

		try
		{
			destination = gridProxy.getStorage().getItemInventory();
			IEnergySource src = gridProxy.getEnergy();

			if ( itemStack.getStackSize() > 0 )
			{
				IAEItemStack aquired = Platform.poweredExtraction( src, destination, itemStack, mySrc );
				if ( aquired != null )
				{
					changed = true;
					ItemStack issue = adaptor.addItems( aquired.getItemStack() );
					if ( issue != null )
						throw new RuntimeException( "bad attempt at managining inventory. ( addItems )" );
				}
			}
			else if ( itemStack.getStackSize() < 0 )
			{
				IAEItemStack toStore = itemStack.copy();
				toStore.setStackSize( -toStore.getStackSize() );

				long diff = toStore.getStackSize();

				toStore = Platform.poweredInsert( src, destination, toStore, mySrc );

				if ( toStore != null )
					diff -= toStore.getStackSize();

				if ( diff != 0 )
				{
					// extract items!
					changed = true;
					ItemStack removed = adaptor.removeItems( (int) diff, null, null );
					if ( removed == null )
						throw new RuntimeException( "bad attempt at managining inventory. ( addItems )" );
					else if ( removed.stackSize != diff )
						throw new RuntimeException( "bad attempt at managining inventory. ( addItems )" );
				}
			}
			// else wtf?
		}
		catch (GridAccessException e)
		{
			// :P
		}

		if ( changed )
			updatePlan( x );

		interfaceRequest = isWorking = false;
		return changed;
	}

	public IInventory getConfig()
	{
		return config;
	}

	public IInventory getPatterns()
	{
		return patterns;
	}

	MEMonitorPassthu<IAEItemStack> items = new MEMonitorPassthu<IAEItemStack>( new NullInventory(), IAEItemStack.class );
	MEMonitorPassthu<IAEFluidStack> fluids = new MEMonitorPassthu<IAEFluidStack>( new NullInventory(), IAEFluidStack.class );

	public void gridChanged()
	{
		try
		{
			items.setInternal( gridProxy.getStorage().getItemInventory() );
			fluids.setInternal( gridProxy.getStorage().getFluidInventory() );
		}
		catch (GridAccessException gae)
		{
			items.setInternal( new NullInventory() );
			fluids.setInternal( new NullInventory() );
		}

		TileEntity te = iHost.getTileEntity();
		te.getWorldObj().notifyBlocksOfNeighborChange( te.xCoord, te.yCoord, te.zCoord, Platform.air );
	}

	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.SMART;
	}

	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( iHost.getTileEntity() );
	}

	public IInventory getInternalInventory()
	{
		return storage;
	}

	public void markDirty()
	{
		for (int slot = 0; slot < storage.getSizeInventory(); slot++)
			onChangeInventory( storage, slot, InvOperation.markDirty, null, null );
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		if ( isWorking )
			return;

		if ( inv == config )
			readConfig();
		else if ( inv == patterns )
		{

		}
		else if ( inv == storage && slot >= 0 )
		{
			boolean had = hasWorkToDo();

			updatePlan( slot );

			boolean now = hasWorkToDo();

			if ( had != now )
			{
				try
				{
					if ( now )
						gridProxy.getTick().alertDevice( gridProxy.getNode() );
					else
						gridProxy.getTick().sleepDevice( gridProxy.getNode() );
				}
				catch (GridAccessException e)
				{
					// :P
				}
			}
		}
	}

	public boolean hasWorkToDo()
	{
		return requireWork[0] != null || requireWork[1] != null || requireWork[2] != null || requireWork[3] != null || requireWork[4] != null
				|| requireWork[5] != null || requireWork[6] != null || requireWork[7] != null;
	}

	private boolean updateStorage()
	{
		boolean didSomething = false;

		for (int x = 0; x < 8; x++)
		{
			if ( requireWork[x] != null )
			{
				didSomething = usePlan( x, requireWork[x] ) || didSomething;
			}
		}

		return didSomething;
	}

	public boolean hasConfig()
	{
		return hasConfig;
	}

	public int[] getAccessibleSlotsFromSide(int side)
	{
		return sides;
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( TickRates.Interface.min, TickRates.Interface.max, !hasWorkToDo(), true );
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		boolean couldDoWork = updateStorage();
		return hasWorkToDo() ? (couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER) : TickRateModulation.SLEEP;
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		if ( hasConfig() )
			return new InterfaceInventory( this );

		return items;
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		if ( hasConfig() )
			return null;

		return fluids;
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		if ( name.equals( "storage" ) )
			return storage;

		if ( name.equals( "patterns" ) )
			return patterns;

		if ( name.equals( "config" ) )
			return config;

		return null;
	}

	public IInventory getStorage()
	{
		return storage;
	}

	public TileEntity getTile()
	{
		return (TileEntity) (iHost instanceof TileEntity ? iHost : null);
	}

	public IPart getPart()
	{
		return (IPart) (iHost instanceof IPart ? iHost : null);
	}

	public appeng.api.util.IConfigManager getConfigManager()
	{
		return cm;
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
	{
		// TODO Auto-generated method stub

	};

}
