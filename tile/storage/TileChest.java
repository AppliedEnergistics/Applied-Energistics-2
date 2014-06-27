package appeng.tile.storage;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.networking.GridFlags;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkPowerStorage;
import appeng.api.networking.events.MENetworkPowerStorage.PowerEventType;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.IConfigManager;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.me.storage.MEInventoryHandler;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkPowerTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;

public class TileChest extends AENetworkPowerTile implements IMEChest, IFluidHandler, ITerminalHost, IPriorityHost, IConfigManagerHost
{

	static private class ChestNoHandler extends Exception
	{

		private static final long serialVersionUID = 7995805326136526631L;

	}

	static final ChestNoHandler noHandler = new ChestNoHandler();

	static final int sides[] = new int[] { 0 };
	static final int front[] = new int[] { 1 };
	static final int noslots[] = new int[] {};

	AppEngInternalInventory inv = new AppEngInternalInventory( this, 2 );
	BaseActionSource mySrc = new MachineSource( this );
	IConfigManager config = new ConfigManager( this );

	ItemStack storageType;
	long lastStateChange = 0;
	int priority = 0;
	int state = 0;
	boolean wasActive = false;

	private void recalculateDisplay()
	{
		int oldState = state;

		for (int x = 0; x < getCellCount(); x++)
			state |= (getCellStatus( x ) << (3 * x));

		if ( isPowered() )
			state |= 0x40;
		else
			state &= ~0x40;

		boolean currentActive = gridProxy.isActive();
		if ( wasActive != currentActive )
		{
			wasActive = currentActive;
			try
			{
				gridProxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );
			}
			catch (GridAccessException e)
			{
				// :P
			}
		}

		if ( oldState != state )
			markForUpdate();
	}

	@Override
	protected void PowerEvent(PowerEventType x)
	{
		if ( x == PowerEventType.REQUEST_POWER )
		{
			try
			{
				gridProxy.getGrid().postEvent( new MENetworkPowerStorage( this, PowerEventType.REQUEST_POWER ) );
			}
			catch (GridAccessException e)
			{
				// :(
			}
		}
		else
			recalculateDisplay();
	}

	private class invManger extends AETileEventHandler
	{

		public invManger() {
			super( TileEventType.TICK, TileEventType.NETWORK, TileEventType.WORLD_NBT );
		}

		@Override
		public void Tick()
		{
			if ( worldObj.isRemote )
				return;

			double idleUsage = gridProxy.getIdlePowerUsage();

			try
			{
				if ( !gridProxy.getEnergy().isNetworkPowered() )
				{
					double powerUsed = extractAEPower( idleUsage, Actionable.MODULATE, PowerMultiplier.CONFIG ); // drain
					if ( powerUsed + 0.1 >= idleUsage != (state & 0x40) > 0 )
						recalculateDisplay();
				}
			}
			catch (GridAccessException e)
			{
				double powerUsed = extractAEPower( gridProxy.getIdlePowerUsage(), Actionable.MODULATE, PowerMultiplier.CONFIG ); // drain
				if ( powerUsed + 0.1 >= idleUsage != (state & 0x40) > 0 )
					recalculateDisplay();
			}

			if ( inv.getStackInSlot( 0 ) != null )
			{
				tryToStoreContents();
			}
		}

		@Override
		public void writeToStream(ByteBuf data) throws IOException
		{
			if ( worldObj.getTotalWorldTime() - lastStateChange > 8 )
				state = 0;
			else
				state &= 0x24924924; // just keep the blinks...

			for (int x = 0; x < getCellCount(); x++)
				state |= (getCellStatus( x ) << (3 * x));

			if ( isPowered() )
				state |= 0x40;
			else
				state &= ~0x40;

			data.writeByte( state );
			ItemStack is = inv.getStackInSlot( 1 );

			if ( is == null )
			{
				data.writeInt( 0 );
			}
			else
			{
				data.writeInt( (is.getItemDamage() << Platform.DEF_OFFSET) | Item.getIdFromItem( is.getItem() ) );
			}
		}

		@Override
		public boolean readFromStream(ByteBuf data) throws IOException
		{
			int oldState = state;
			ItemStack oldType = storageType;

			state = data.readByte();

			int item = data.readInt();

			if ( item == 0 )
				storageType = null;
			else
				storageType = new ItemStack( Item.getItemById( item & 0xffff ), 1, item >> Platform.DEF_OFFSET );

			lastStateChange = worldObj.getTotalWorldTime();

			return (state & 0xDB6DB6DB) != (oldState & 0xDB6DB6DB) || oldType != storageType;
		}

		@Override
		public void readFromNBT(NBTTagCompound data)
		{
			config.readFromNBT( data );
			priority = data.getInteger( "priority" );
		}

		@Override
		public void writeToNBT(NBTTagCompound data)
		{
			config.writeToNBT( data );
			data.setInteger( "priority", priority );
		}

	};

	@MENetworkEventSubscribe
	public void powerRender(MENetworkPowerStatusChange c)
	{
		recalculateDisplay();
	}

	@MENetworkEventSubscribe
	public void channelRender(MENetworkChannelsChanged c)
	{
		recalculateDisplay();
	}

	public TileChest() {
		internalMaxPower = PowerMultiplier.CONFIG.multiply( 40 );
		gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL );
		addNewHandler( new invManger() );
		config.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		config.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
		config.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );

		internalPublicPowerStorage = true;
		internalPowerFlow = AccessRestriction.WRITE;
	}

	boolean isCached = false;

	private ICellHandler cellHandler;
	private MEMonitorHandler icell;
	private MEMonitorHandler fcell;

	@Override
	public IMEMonitor getItemInventory()
	{
		return icell;
	}

	@Override
	public IMEMonitor getFluidInventory()
	{
		return fcell;
	}

	class ChestNetNotifier<T extends IAEStack<T>> implements IMEMonitorHandlerReceiver<T>
	{

		final StorageChannel chan;

		public ChestNetNotifier(StorageChannel chan) {
			this.chan = chan;
		}

		@Override
		public void postChange(IBaseMonitor<T> monitor, T change, BaseActionSource source)
		{
			if ( source == mySrc || (source instanceof PlayerSource && ((PlayerSource) source).via == TileChest.this) )
			{
				try
				{
					if ( gridProxy.isActive() )
						gridProxy.getStorage().postAlterationOfStoredItems( chan, change, mySrc );
				}
				catch (GridAccessException e)
				{
					// :(
				}
			}

			blinkCell( 0 );
		}

		@Override
		public boolean isValid(Object verificationToken)
		{
			if ( chan == StorageChannel.ITEMS )
				return verificationToken == icell;
			if ( chan == StorageChannel.FLUIDS )
				return verificationToken == fcell;
			return false;
		}

		@Override
		public void onListUpdate()
		{
			// not used here
		}

	};

	class ChestMonitorHandler<T extends IAEStack> extends MEMonitorHandler<T>
	{

		public ChestMonitorHandler(IMEInventoryHandler<T> t) {
			super( t );
		}

		public IMEInventoryHandler<T> getInternalHandler()
		{
			IMEInventoryHandler<T> h = getHandler();
			if ( h instanceof MEInventoryHandler )
				return (IMEInventoryHandler<T>) ((MEInventoryHandler) h).getInternal();
			return this.getHandler();
		}

	};

	private <StackType extends IAEStack> MEMonitorHandler<StackType> wrap(IMEInventoryHandler h)
	{
		if ( h == null )
			return null;

		MEInventoryHandler ih = new MEInventoryHandler( h, h.getChannel().type );
		ih.myPriority = priority;

		MEMonitorHandler<StackType> g = new ChestMonitorHandler<StackType>( ih );
		g.addListener( new ChestNetNotifier( h.getChannel() ), g );

		return g;
	}

	public IMEInventoryHandler getHandler(StorageChannel channel) throws ChestNoHandler
	{
		if ( !isCached )
		{
			icell = null;
			fcell = null;

			ItemStack is = inv.getStackInSlot( 1 );
			if ( is != null )
			{
				isCached = true;
				cellHandler = AEApi.instance().registries().cell().getHandler( is );
				if ( cellHandler != null )
				{
					double power = 1.0;

					IMEInventoryHandler<IAEItemStack> itemCell = cellHandler.getCellInventory( is, StorageChannel.ITEMS );
					IMEInventoryHandler<IAEFluidStack> fluidCell = cellHandler.getCellInventory( is, StorageChannel.FLUIDS );

					if ( itemCell != null )
						power += cellHandler.cellIdleDrain( is, itemCell );
					else if ( fluidCell != null )
						power += cellHandler.cellIdleDrain( is, fluidCell );

					gridProxy.setIdlePowerUsage( power );

					icell = wrap( itemCell );
					fcell = wrap( fluidCell );
				}
			}
		}

		switch (channel)
		{
		case FLUIDS:
			if ( fcell == null )
				throw noHandler;
			return fcell;
		case ITEMS:
			if ( icell == null )
				throw noHandler;
			return icell;
		default:
		}

		return null;
	}

	@Override
	public IInventory getInternalInventory()
	{
		return inv;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		if ( slot == 1 )
		{
			icell = null;
			fcell = null;
			isCached = false; // recalculate the storage cell.

			try
			{
				IStorageGrid gs = gridProxy.getStorage();
				Platform.postChanges( gs, removed, added, mySrc );

				gridProxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );
			}
			catch (GridAccessException e)
			{

			}

			// update the neighbors
			if ( worldObj != null )
			{
				worldObj.notifyBlocksOfNeighborChange( xCoord, yCoord, zCoord, Platform.air );
				markForUpdate();
			}
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		inv.setInventorySlotContents( i, itemstack );
		tryToStoreContents();
	}

	private void tryToStoreContents()
	{
		try
		{
			if ( getStackInSlot( 0 ) != null )
			{
				IMEInventory<IAEItemStack> cell = getHandler( StorageChannel.ITEMS );

				IAEItemStack returns = Platform.poweredInsert( this, cell, AEApi.instance().storage().createItemStack( inv.getStackInSlot( 0 ) ), mySrc );

				if ( returns == null )
					inv.setInventorySlotContents( 0, null );
				else
					inv.setInventorySlotContents( 0, returns.getItemStack() );
			}
		}
		catch (ChestNoHandler t)
		{
		}
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return i == 1;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		if ( i == 1 )
		{
			if ( AEApi.instance().registries().cell().getCellInventory( itemstack, StorageChannel.ITEMS ) != null )
				return true;
			if ( AEApi.instance().registries().cell().getCellInventory( itemstack, StorageChannel.FLUIDS ) != null )
				return true;
		}
		else
		{
			try
			{
				IMEInventory<IAEItemStack> cell = getHandler( StorageChannel.ITEMS );
				IAEItemStack returns = cell.injectItems( AEApi.instance().storage().createItemStack( inv.getStackInSlot( 0 ) ), Actionable.SIMULATE, mySrc );
				return returns == null || returns.getStackSize() != itemstack.stackSize;
			}
			catch (ChestNoHandler t)
			{
			}
		}
		return false;
	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection side)
	{
		if ( ForgeDirection.SOUTH == side )
			return front;

		if ( gridProxy.isActive() )
		{
			try
			{
				if ( getHandler( StorageChannel.ITEMS ) != null )
					return sides;
			}
			catch (ChestNoHandler e)
			{
				// nope!
			}
		}
		return noslots;
	}

	@Override
	public List<IMEInventoryHandler> getCellArray(StorageChannel channel)
	{
		if ( gridProxy.isActive() )
		{
			try
			{
				return Arrays.asList( new IMEInventoryHandler[] { getHandler( channel ) } );
			}
			catch (ChestNoHandler e)
			{
				// :P
			}
		}
		return new ArrayList();
	}

	@Override
	public int getPriority()
	{
		return priority;
	}

	@Override
	public int getCellCount()
	{
		return 1;
	}

	@Override
	public void blinkCell(int slot)
	{
		long now = worldObj.getTotalWorldTime();
		if ( now - lastStateChange > 8 )
			state = 0;
		lastStateChange = now;

		state |= 1 << (slot * 3 + 2);

		recalculateDisplay();
	}

	@Override
	public boolean isCellBlinking(int slot)
	{
		long now = worldObj.getTotalWorldTime();
		if ( now - lastStateChange > 8 )
			return false;

		return ((state >> (slot * 3 + 2)) & 0x01) == 0x01;
	}

	@Override
	public int getCellStatus(int slot)
	{
		if ( Platform.isClient() )
			return (state >> (slot * 3)) & 3;

		ItemStack cell = inv.getStackInSlot( 1 );
		ICellHandler ch = AEApi.instance().registries().cell().getHandler( cell );

		if ( ch != null )
		{
			try
			{
				IMEInventoryHandler handler = getHandler( StorageChannel.ITEMS );
				if ( ch != null && handler instanceof ChestMonitorHandler )
					return ch.getStatusForCell( cell, ((ChestMonitorHandler) handler).getInternalHandler() );
			}
			catch (ChestNoHandler e)
			{
			}

			try
			{
				IMEInventoryHandler handler = getHandler( StorageChannel.FLUIDS );
				if ( ch != null && handler instanceof ChestMonitorHandler )
					return ch.getStatusForCell( cell, ((ChestMonitorHandler) handler).getInternalHandler() );
			}
			catch (ChestNoHandler e)
			{
			}
		}

		return 0;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		double req = resource.amount / 500.0;
		double available = extractAEPower( req, Actionable.SIMULATE, PowerMultiplier.CONFIG );
		if ( available >= req - 0.01 )
		{
			try
			{
				IMEInventoryHandler h = getHandler( StorageChannel.FLUIDS );

				extractAEPower( req, Actionable.MODULATE, PowerMultiplier.CONFIG );
				IAEStack results = h.injectItems( AEFluidStack.create( resource ), doFill ? Actionable.MODULATE : Actionable.SIMULATE, mySrc );

				if ( results == null )
					return resource.amount;

				return resource.amount - (int) results.getStackSize();
			}
			catch (ChestNoHandler e)
			{
			}
		}
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		try
		{
			IMEInventoryHandler h = getHandler( StorageChannel.FLUIDS );
			return h.canAccept( AEFluidStack.create( new FluidStack( fluid, 1 ) ) );
		}
		catch (ChestNoHandler e)
		{
		}
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		try
		{
			IMEInventoryHandler h = getHandler( StorageChannel.FLUIDS );
			if ( h.getChannel() == StorageChannel.FLUIDS )
				return new FluidTankInfo[] { new FluidTankInfo( null, 1 ) }; // eh?
		}
		catch (ChestNoHandler e)
		{
		}

		return null;
	}

	@Override
	protected double extractAEPower(double amt, Actionable mode)
	{
		double stash = 0.0;

		IEnergyGrid eg;
		try
		{
			eg = gridProxy.getEnergy();
			stash = eg.extractAEPower( amt, mode, PowerMultiplier.ONE );
			if ( stash >= amt )
				return stash;
		}
		catch (GridAccessException e)
		{
			// no grid :(
		}

		// local battery!
		return super.extractAEPower( amt - stash, mode ) + stash;
	}

	@Override
	public boolean isPowered()
	{
		if ( Platform.isClient() )
			return (state & 0x40) == 0x40;

		boolean gridPowered = getAECurrentPower() > 64;

		if ( !gridPowered )
		{
			try
			{
				gridPowered = gridProxy.getEnergy().isNetworkPowered();
			}
			catch (GridAccessException e)
			{
			}
		}

		return super.getAECurrentPower() > 1 || gridPowered;
	}

	@Override
	public IStorageMonitorable getMonitorable(ForgeDirection side, BaseActionSource src)
	{
		if ( Platform.canAccess( gridProxy, src ) && side != getForward() )
			return this;
		return null;
	}

	public ItemStack getStorageType()
	{
		if ( isPowered() )
			return storageType;
		return null;
	}

	@Override
	public void setPriority(int newValue)
	{
		priority = newValue;

		icell = null;
		fcell = null;
		isCached = false; // recalculate the storage cell.

		try
		{
			gridProxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );
		}
		catch (GridAccessException e)
		{
			// :P
		}
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return config;
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
	{

	}

	public boolean openGui(EntityPlayer p, ICellHandler ch, ItemStack cell, int side)
	{
		try
		{
			IMEInventoryHandler ih = this.getHandler( StorageChannel.ITEMS );
			if ( ch != null && ih != null )
			{
				IMEInventoryHandler mine = ih;
				ch.openChestGui( p, this, ch, mine, cell, StorageChannel.ITEMS );
				return true;
			}

		}
		catch (ChestNoHandler e)
		{
			// :P
		}

		try
		{
			IMEInventoryHandler fh = this.getHandler( StorageChannel.FLUIDS );
			if ( ch != null && fh != null )
			{
				IMEInventoryHandler mine = fh;
				ch.openChestGui( p, this, ch, mine, cell, StorageChannel.FLUIDS );
				return true;
			}
		}
		catch (ChestNoHandler e)
		{
			// :P
		}

		return false;
	}

}
