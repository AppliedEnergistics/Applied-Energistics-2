package appeng.helpers;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
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
		IConfigureableObject, IConfigManagerHost, ICraftingProvider
{

	final int sides[] = new int[] { 0, 1, 2, 3, 4, 5, 6, 7 };
	final IAEItemStack requireWork[] = new IAEItemStack[] { null, null, null, null, null, null, null, null };

	boolean hasConfig = false;
	AENetworkProxy gridProxy;
	IInterfaceHost iHost;
	BaseActionSource mySrc;
	ConfigManager cm = new ConfigManager( this );

	List<ICraftingPatternDetails> craftingList = null;
	List<ItemStack> waitingToSend = null;

	public boolean hasItemsToSend()
	{
		return waitingToSend != null && !waitingToSend.isEmpty();
	}

	public void updateCraftingList()
	{
		Boolean accountedFor[] = new Boolean[] { false, false, false, false, false, false, false, false, false }; // 9...

		assert (accountedFor.length == patterns.getSizeInventory());

		if ( craftingList != null )
		{
			Iterator<ICraftingPatternDetails> i = craftingList.iterator();
			while (i.hasNext())
			{
				ICraftingPatternDetails details = i.next();
				boolean found = false;

				for (int x = 0; x < accountedFor.length; x++)
				{
					ItemStack is = patterns.getStackInSlot( x );
					if ( details.getPattern() == is )
					{
						accountedFor[x] = found = true;
					}
				}

				if ( !found )
					i.remove();
			}
		}

		for (int x = 0; x < accountedFor.length; x++)
		{
			if ( accountedFor[x] == false )
				addToCraftingList( patterns.getStackInSlot( x ) );
		}

		try
		{
			gridProxy.getGrid().postEvent( new MENetworkCraftingPatternChange( this, gridProxy.getNode() ) );
		}
		catch (GridAccessException e)
		{
			// :P
		}
	}

	public void addToCraftingList(ItemStack is)
	{
		if ( is == null )
			return;

		if ( is.getItem() instanceof ICraftingPatternItem )
		{
			ICraftingPatternItem cpi = (ICraftingPatternItem) is.getItem();
			ICraftingPatternDetails details = cpi.getPatternForItem( is, iHost.getTileEntity().getWorldObj() );

			if ( details != null )
			{
				if ( craftingList == null )
					craftingList = new LinkedList();

				craftingList.add( details );
			}
		}
	}

	public void addToSendList(ItemStack is)
	{
		if ( is == null )
			return;

		if ( waitingToSend == null )
			waitingToSend = new LinkedList();

		waitingToSend.add( is );

		try
		{
			gridProxy.getTick().wakeDevice( gridProxy.getNode() );
		}
		catch (GridAccessException e)
		{
			// :P
		}
	}

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

		NBTTagList waitingToSend = new NBTTagList();
		if ( this.waitingToSend != null )
		{
			for (ItemStack is : this.waitingToSend)
			{
				NBTTagCompound item = new NBTTagCompound();
				is.writeToNBT( item );
				waitingToSend.appendTag( item );
			}
		}
		data.setTag( "waitingToSend", waitingToSend );
	}

	public void readFromNBT(NBTTagCompound data)
	{
		this.waitingToSend = null;
		NBTTagList waitingList = data.getTagList( "waitingToSend", 10 );
		if ( waitingList != null )
		{
			for (int x = 0; x < waitingList.tagCount(); x++)
			{
				NBTTagCompound c = waitingList.getCompoundTagAt( x );
				if ( c != null )
				{
					ItemStack is = ItemStack.loadItemStackFromNBT( c );
					addToSendList( is );
				}
			}
		}

		config.readFromNBT( data, "config" );
		patterns.readFromNBT( data, "patterns" );
		storage.readFromNBT( data, "storage" );
		readConfig();
		updateCraftingList();
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
		if ( req != null && req.getStackSize() <= 0 )
		{
			config.setInventorySlotContents( slot, null );
			req = null;
		}

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
			super( InventoryAdaptor.getAdaptor( tileInterface.storage, ForgeDirection.UP ) );
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
				// make sure strange things didn't happen...
				if ( adaptor.simulateAdd( itemStack.getItemStack() ) != null )
				{
					changed = true;
					throw new GridAccessException();
				}

				IAEItemStack aquired = Platform.poweredExtraction( src, destination, itemStack, mySrc );
				if ( aquired != null )
				{
					changed = true;
					ItemStack issue = adaptor.addItems( aquired.getItemStack() );
					if ( issue != null )
						throw new RuntimeException( "bad attempt at managing inventory. ( addItems )" );
				}
			}
			else if ( itemStack.getStackSize() < 0 )
			{
				IAEItemStack toStore = itemStack.copy();
				toStore.setStackSize( -toStore.getStackSize() );

				long diff = toStore.getStackSize();

				// make sure strange things didn't happen...
				ItemStack canExtract = adaptor.simulateRemove( (int) diff, toStore.getItemStack(), null );
				if ( canExtract == null || canExtract.stackSize != diff )
				{
					changed = true;
					throw new GridAccessException();
				}

				toStore = Platform.poweredInsert( src, destination, toStore, mySrc );

				if ( toStore != null )
					diff -= toStore.getStackSize();

				if ( diff != 0 )
				{
					// extract items!
					changed = true;
					ItemStack removed = adaptor.removeItems( (int) diff, null, null );
					if ( removed == null )
						throw new RuntimeException( "bad attempt at managing inventory. ( removeItems )" );
					else if ( removed.stackSize != diff )
						throw new RuntimeException( "bad attempt at managing inventory. ( removeItems )" );
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
		else if ( inv == patterns && (removed != null || added != null) )
			updateCraftingList();
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
		return hasItemsToSend() || requireWork[0] != null || requireWork[1] != null || requireWork[2] != null || requireWork[3] != null
				|| requireWork[4] != null || requireWork[5] != null || requireWork[6] != null || requireWork[7] != null;
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
		if ( hasItemsToSend() )
			pushItemsOut( EnumSet.allOf( ForgeDirection.class ) );

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

	}

	public IStorageMonitorable getMonitorable(ForgeDirection side, BaseActionSource src, IStorageMonitorable myInterface)
	{
		if ( Platform.canAccess( gridProxy, src ) )
			return myInterface;

		final DualityInterface di = this;

		return new IStorageMonitorable() {

			@Override
			public IMEMonitor<IAEItemStack> getItemInventory()
			{
				return new InterfaceInventory( di );
			}

			@Override
			public IMEMonitor<IAEFluidStack> getFluidInventory()
			{
				return null;
			}
		};
	}

	@Override
	public boolean isBusy()
	{
		if ( hasItemsToSend() )
			return true;

		boolean busy = false;

		if ( isBlocking() )
		{
			EnumSet<ForgeDirection> possibleDirections = iHost.getTargets();
			TileEntity tile = iHost.getTileEntity();
			World w = tile.getWorldObj();

			for (ForgeDirection s : possibleDirections)
			{
				TileEntity te = w.getTileEntity( tile.xCoord + s.offsetX, tile.yCoord + s.offsetY, tile.zCoord + s.offsetZ );

				InventoryAdaptor ad = InventoryAdaptor.getAdaptor( te, s.getOpposite() );
				if ( ad != null )
				{
					if ( ad.simulateRemove( 1, null, null ) != null )
					{
						busy = true;
						break;
					}
				}
			}
		}

		return busy;
	}

	private boolean isBlocking()
	{
		return true;
	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table)
	{
		if ( hasItemsToSend() )
			return false;

		TileEntity tile = iHost.getTileEntity();
		World w = tile.getWorldObj();

		EnumSet<ForgeDirection> possibleDirections = iHost.getTargets();
		for (ForgeDirection s : possibleDirections)
		{
			TileEntity te = w.getTileEntity( tile.xCoord + s.offsetX, tile.yCoord + s.offsetY, tile.zCoord + s.offsetZ );
			if ( te instanceof ICraftingMachine )
			{
				if ( ((ICraftingMachine) te).pushPattern( patternDetails, table, s.getOpposite() ) )
					return true;
			}
			else
			{
				InventoryAdaptor ad = InventoryAdaptor.getAdaptor( te, s.getOpposite() );
				if ( ad != null )
				{
					possibleDirections.remove( s );

					for (int x = 0; x < table.getSizeInventory(); x++)
					{
						ItemStack is = table.getStackInSlot( x );
						if ( is != null )
						{
							addToSendList( ad.addItems( is ) );
						}
					}

					pushItemsOut( possibleDirections );
					return true;
				}
			}
		}

		return false;
	}

	private void pushItemsOut(EnumSet<ForgeDirection> possibleDirections)
	{
		if ( !hasItemsToSend() )
			return;

		TileEntity tile = iHost.getTileEntity();
		World w = tile.getWorldObj();

		Iterator<ItemStack> i = waitingToSend.iterator();
		while (i.hasNext())
		{
			ItemStack whatToSend = i.next();

			for (ForgeDirection s : possibleDirections)
			{
				TileEntity te = w.getTileEntity( tile.xCoord + s.offsetX, tile.yCoord + s.offsetY, tile.zCoord + s.offsetZ );
				if ( te == null )
					continue;

				InventoryAdaptor ad = InventoryAdaptor.getAdaptor( te, s.getOpposite() );
				if ( ad != null )
				{
					whatToSend = ad.addItems( whatToSend );
					if ( whatToSend == null )
						break;
				}
			}

			if ( whatToSend == null )
				i.remove();
			else
				whatToSend.stackSize = whatToSend.stackSize;
		}

		if ( waitingToSend.isEmpty() )
			waitingToSend = null;
	}

	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker)
	{
		if ( craftingList != null )
		{
			for (ICraftingPatternDetails details : craftingList)
				craftingTracker.addCraftingOption( this, details );
		}
	}

}
