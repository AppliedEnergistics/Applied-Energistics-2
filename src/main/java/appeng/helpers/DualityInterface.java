package appeng.helpers;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
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
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.settings.TickRates;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.storage.MEMonitorIInventory;
import appeng.me.storage.MEMonitorPassthu;
import appeng.me.storage.NullInventory;
import appeng.parts.automation.UpgradeInventory;
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
import appeng.util.item.AEItemStack;

import com.google.common.collect.ImmutableSet;

public class DualityInterface implements IGridTickable, ISegmentedInventory, IStorageMonitorable, IInventoryDestination, IAEAppEngInventory,
		IConfigurableObject, IConfigManagerHost, ICraftingProvider, IUpgradeableHost, IPriorityHost
{

	final int sides[] = new int[] { 0, 1, 2, 3, 4, 5, 6, 7 };
	final IAEItemStack requireWork[] = new IAEItemStack[] { null, null, null, null, null, null, null, null };
	final MultiCraftingTracker craftingTracker;

	boolean hasConfig = false;
	AENetworkProxy gridProxy;
	IInterfaceHost iHost;
	BaseActionSource mySrc;
	ConfigManager cm = new ConfigManager( this );
	int priority;

	List<ICraftingPatternDetails> craftingList = null;
	List<ItemStack> waitingToSend = null;

	private UpgradeInventory upgrades;

	@Override
	public int getInstalledUpgrades(Upgrades u)
	{
		if ( upgrades == null )
			return 0;
		return upgrades.getInstalledUpgrades( u );
	}

	public boolean hasItemsToSend()
	{
		return waitingToSend != null && !waitingToSend.isEmpty();
	}

	public void updateCraftingList()
	{
		Boolean accountedFor[] = new Boolean[] { false, false, false, false, false, false, false, false, false }; // 9...

		assert (accountedFor.length == patterns.getSizeInventory());

		if ( !gridProxy.isReady() )
			return;

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

		upgrades = new UpgradeInventory( gridProxy.getMachineRepresentation(), this, 1 );
		cm.registerSetting( Settings.BLOCK, YesNo.NO );
		cm.registerSetting( Settings.INTERFACE_TERMINAL, YesNo.YES );

		iHost = ih;
		craftingTracker = new MultiCraftingTracker( iHost, 9 );
		mySrc = fluids.changeSource = items.changeSource = new MachineSource( iHost );
	}

	@Override
	public void saveChanges()
	{
		iHost.saveChanges();
	}

	private void readConfig()
	{
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

		notifyNeightbors();
	}

	public void writeToNBT(NBTTagCompound data)
	{
		config.writeToNBT( data, "config" );
		patterns.writeToNBT( data, "patterns" );
		storage.writeToNBT( data, "storage" );
		upgrades.writeToNBT( data, "upgrades" );
		cm.writeToNBT( data );
		craftingTracker.writeToNBT( data );
		data.setInteger( "priority", priority );

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

		craftingTracker.readFromNBT( data );
		upgrades.readFromNBT( data, "upgrades" );
		config.readFromNBT( data, "config" );
		patterns.readFromNBT( data, "patterns" );
		storage.readFromNBT( data, "storage" );
		priority = data.getInteger( "priority" );
		cm.readFromNBT( data );
		readConfig();
		updateCraftingList();
	}

	AppEngInternalAEInventory config = new AppEngInternalAEInventory( this, 8 );
	AppEngInternalInventory storage = new AppEngInternalInventory( this, 8 );
	AppEngInternalInventory patterns = new AppEngInternalInventory( this, 9 );

	WrapperInvSlot slotInv = new WrapperInvSlot( storage );

	private InventoryAdaptor getAdaptor(int slot)
	{
		return new AdaptorIInventory( slotInv.getWrapper( slot ) );
	}

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
			else if ( req.isSameType( Stored ) ) // same type ( qty different? )!
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
			super( new AdaptorIInventory( tileInterface.storage ) );
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

	}

	private boolean usePlan(int x, IAEItemStack itemStack)
	{
		boolean changed = false;
		InventoryAdaptor adaptor = getAdaptor( x );
		interfaceRequest = isWorking = true;

		try
		{
			destination = gridProxy.getStorage().getItemInventory();
			IEnergySource src = gridProxy.getEnergy();

			if ( craftingTracker.isBusy( x ) )
				changed = handleCrafting( x, adaptor, itemStack ) || changed;
			else if ( itemStack.getStackSize() > 0 )
			{
				// make sure strange things didn't happen...
				if ( adaptor.simulateAdd( itemStack.getItemStack() ) != null )
				{
					changed = true;
					throw new GridAccessException();
				}

				IAEItemStack acquired = Platform.poweredExtraction( src, destination, itemStack, mySrc );
				if ( acquired != null )
				{
					changed = true;
					ItemStack issue = adaptor.addItems( acquired.getItemStack() );
					if ( issue != null )
						throw new RuntimeException( "bad attempt at managing inventory. ( addItems )" );
				}
				else
					changed = handleCrafting( x, adaptor, itemStack ) || changed;
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

	private boolean handleCrafting(int x, InventoryAdaptor d, IAEItemStack itemStack)
	{
		try
		{
			if ( getInstalledUpgrades( Upgrades.CRAFTING ) > 0 && itemStack != null )
			{
				return craftingTracker.handleCrafting( x, itemStack.getStackSize(), itemStack, d, iHost.getTileEntity().getWorldObj(), gridProxy.getGrid(),
						gridProxy.getCrafting(), mySrc );
			}
		}
		catch (GridAccessException e)
		{
			// :P
		}

		return false;
	}

	public IInventory getConfig()
	{
		return config;
	}

	public IInventory getPatterns()
	{
		return patterns;
	}

	MEMonitorPassthu<IAEItemStack> items = new MEMonitorPassthu<IAEItemStack>( new NullInventory(), StorageChannel.ITEMS );
	MEMonitorPassthu<IAEFluidStack> fluids = new MEMonitorPassthu<IAEFluidStack>( new NullInventory(), StorageChannel.FLUIDS );

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

		notifyNeightbors();
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
		if ( !gridProxy.isActive() )
			return TickRateModulation.SLEEP;

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

		if ( name.equals( "upgrades" ) )
			return upgrades;

		return null;
	}

	public IInventory getStorage()
	{
		return storage;
	}

	@Override
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
		if ( getInstalledUpgrades( Upgrades.CRAFTING ) == 0 )
			cancelCrafting();

		markDirty();
	}

	private void cancelCrafting()
	{
		craftingTracker.cancel();
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

			boolean allAreBusy = true;

			for (ForgeDirection s : possibleDirections)
			{
				TileEntity te = w.getTileEntity( tile.xCoord + s.offsetX, tile.yCoord + s.offsetY, tile.zCoord + s.offsetZ );

				InventoryAdaptor ad = InventoryAdaptor.getAdaptor( te, s.getOpposite() );
				if ( ad != null )
				{
					if ( ad.simulateRemove( 1, null, null ) == null )
					{
						allAreBusy = false;
						break;
					}
				}
			}

			busy = allAreBusy;
		}

		return busy;
	}

	private boolean isBlocking()
	{
		return cm.getSetting( Settings.BLOCK ) == YesNo.YES;
	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table)
	{
		if ( hasItemsToSend() || !gridProxy.isActive() )
			return false;

		TileEntity tile = iHost.getTileEntity();
		World w = tile.getWorldObj();

		EnumSet<ForgeDirection> possibleDirections = iHost.getTargets();
		for (ForgeDirection s : possibleDirections)
		{
			TileEntity te = w.getTileEntity( tile.xCoord + s.offsetX, tile.yCoord + s.offsetY, tile.zCoord + s.offsetZ );
			if ( te instanceof IInterfaceHost )
			{
				try
				{
					if ( ((IInterfaceHost) te).getInterfaceDuality().sameGrid( gridProxy.getGrid() ) )
						continue;
				}
				catch (GridAccessException e)
				{
					continue;
				}
			}

			if ( te instanceof ICraftingMachine )
			{
				ICraftingMachine cm = (ICraftingMachine) te;
				if ( cm.acceptsPlans() )
				{
					if ( cm.pushPattern( patternDetails, table, s.getOpposite() ) )
						return true;
					continue;
				}
			}

			InventoryAdaptor ad = InventoryAdaptor.getAdaptor( te, s.getOpposite() );
			if ( ad != null )
			{
				if ( isBlocking() )
				{
					if ( ad.simulateRemove( 1, null, null ) != null )
						continue;
				}

				if ( acceptsItems( ad, table ) )
				{
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

	private boolean sameGrid(IGrid grid) throws GridAccessException
	{
		return grid == gridProxy.getGrid();
	}

	private boolean acceptsItems(InventoryAdaptor ad, InventoryCrafting table)
	{
		for (int x = 0; x < table.getSizeInventory(); x++)
		{
			ItemStack is = table.getStackInSlot( x );
			if ( is == null )
				continue;

			if ( ad.simulateAdd( is.copy() ) != null )
				return false;
		}

		return true;
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
					ItemStack Result = ad.addItems( whatToSend );

					if ( Result == null )
						whatToSend = null;
					else
						whatToSend.stackSize -= whatToSend.stackSize - Result.stackSize;

					if ( whatToSend == null )
						break;
				}
			}

			if ( whatToSend == null )
				i.remove();
		}

		if ( waitingToSend.isEmpty() )
			waitingToSend = null;
	}

	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker)
	{
		if ( gridProxy.isActive() && craftingList != null )
		{
			for (ICraftingPatternDetails details : craftingList)
			{
				details.setPriority( this.priority );
				craftingTracker.addCraftingOption( this, details );
			}
		}
	}

	public void addDrops(List<ItemStack> drops)
	{
		if ( waitingToSend != null )
		{
			for (ItemStack is : waitingToSend)
				if ( is != null )
					drops.add( is );
		}

		for (ItemStack is : upgrades)
			if ( is != null )
				drops.add( is );

		for (ItemStack is : storage)
			if ( is != null )
				drops.add( is );

		for (ItemStack is : patterns)
			if ( is != null )
				drops.add( is );
	}

	public void notifyNeightbors()
	{
		if ( gridProxy.isActive() )
		{
			try
			{
				gridProxy.getGrid().postEvent( new MENetworkCraftingPatternChange( this, gridProxy.getNode() ) );
				gridProxy.getTick().wakeDevice( gridProxy.getNode() );
			}
			catch (GridAccessException e)
			{
				// :P
			}
		}

		TileEntity te = iHost.getTileEntity();
		if ( te != null && te.getWorldObj() != null )
			Platform.notifyBlocksOfNeighbors( te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord );
	}

	public IUpgradeableHost getHost()
	{
		if ( getPart() instanceof IUpgradeableHost )
			return (IUpgradeableHost) getPart();
		if ( getTile() instanceof IUpgradeableHost )
			return (IUpgradeableHost) getTile();
		return null;
	}

	public ImmutableSet<ICraftingLink> getRequestedJobs()
	{
		return craftingTracker.getRequestedJobs();
	}

	public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack acquired, Actionable mode)
	{
		int slot = craftingTracker.getSlot( link );

		if ( acquired != null && slot >= 0 && slot <= requireWork.length )
		{
			InventoryAdaptor adaptor = getAdaptor( slot );

			if ( mode == Actionable.SIMULATE )
				return AEItemStack.create( adaptor.simulateAdd( acquired.getItemStack() ) );
			else
			{
				IAEItemStack is = AEItemStack.create( adaptor.addItems( acquired.getItemStack() ) );
				updatePlan( slot );
				return is;
			}
		}

		return acquired;
	}

	public void jobStateChange(ICraftingLink link)
	{
		craftingTracker.jobStateChange( link );
	}

	static final Set<Block> badBlocks = new HashSet();

	public String getTermName()
	{
		TileEntity tile = iHost.getTileEntity();
		World w = tile.getWorldObj();

		if ( ((ICustomNameObject) iHost).hasCustomName() )
			return ((ICustomNameObject) iHost).getCustomName();

		EnumSet<ForgeDirection> possibleDirections = iHost.getTargets();
		for (ForgeDirection s : possibleDirections)
		{
			Vec3 from = Vec3.createVectorHelper( (double) tile.xCoord + 0.5, (double) tile.yCoord + 0.5, (double) tile.zCoord + 0.5 );
			from = from.addVector( s.offsetX * 0.501, s.offsetY * 0.501, s.offsetZ * 0.501 );
			Vec3 to = from.addVector( s.offsetX, s.offsetY, s.offsetZ );

			Block blk = w.getBlock( tile.xCoord + s.offsetX, tile.yCoord + s.offsetY, tile.zCoord + s.offsetZ );
			MovingObjectPosition mop = w.rayTraceBlocks( from, to, true );

			TileEntity te = w.getTileEntity( tile.xCoord + s.offsetX, tile.yCoord + s.offsetY, tile.zCoord + s.offsetZ );

			if ( te == null )
				continue;

			if ( te instanceof IInterfaceHost )
			{
				try
				{
					if ( ((IInterfaceHost) te).getInterfaceDuality().sameGrid( gridProxy.getGrid() ) )
						continue;
				}
				catch (GridAccessException e)
				{
					continue;
				}
			}

			Item item = Item.getItemFromBlock( blk );

			if ( item == null )
			{
				return blk.getUnlocalizedName();
			}

			ItemStack what = new ItemStack( item, 1, blk.getDamageValue( w, tile.xCoord + s.offsetX, tile.yCoord + s.offsetY, tile.zCoord + s.offsetZ ) );

			if ( te instanceof ICraftingMachine || InventoryAdaptor.getAdaptor( te, s.getOpposite() ) != null )
			{
				if ( te instanceof IInventory && ((IInventory) te).getSizeInventory() == 0 )
					continue;

				if ( te instanceof ISidedInventory )
				{
					int[] sides = ((ISidedInventory) te).getAccessibleSlotsFromSide( s.getOpposite().ordinal() );

					if ( sides == null || sides.length == 0 )
						continue;
				}

				try
				{
					if ( mop != null && !badBlocks.contains( blk ) )
					{
						if ( mop.blockX == te.xCoord && mop.blockY == te.yCoord && mop.blockZ == te.zCoord )
						{
							ItemStack g = blk.getPickBlock( mop, w, te.xCoord, te.yCoord, te.zCoord );
							if ( g != null )
								what = g;
						}
					}
				}
				catch (Throwable t)
				{
					badBlocks.add( blk ); // nope!
				}

				if ( what.getItem() != null )
					return what.getUnlocalizedName();
			}

		}

		return "Nothing";
	}

	public long getSortValue()
	{
		TileEntity te = iHost.getTileEntity();
		return (te.zCoord << 24) ^ (te.xCoord << 8) ^ te.yCoord;
	}

	public void initialize()
	{
		updateCraftingList();
	}

	@Override
	public int getPriority()
	{
		return priority;
	}

	@Override
	public void setPriority(int newValue)
	{
		priority = newValue;
		markDirty();

		try
		{
			gridProxy.getGrid().postEvent( new MENetworkCraftingPatternChange( this, gridProxy.getNode() ) );
		}
		catch (GridAccessException e)
		{
			// :P
		}
	}
}
