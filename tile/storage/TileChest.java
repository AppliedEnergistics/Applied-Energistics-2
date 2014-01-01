package appeng.tile.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.IMEChest;
import appeng.api.networking.GridFlags;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkPowerStorage;
import appeng.api.networking.events.MENetworkPowerStorage.PowerEventType;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReciever;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.core.AELog;
import appeng.helpers.AENoHandler;
import appeng.me.GridAccessException;
import appeng.me.storage.MEInventoryHandler;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkPowerTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;

public class TileChest extends AENetworkPowerTile implements IMEChest, IFluidHandler
{

	static final AENoHandler noHandler = new AENoHandler();

	static final int sides[] = new int[] { 0 };
	static final int front[] = new int[] { 1 };

	AppEngInternalInventory inv = new AppEngInternalInventory( this, 2 );

	ItemStack storageType;
	long lastStateChange = 0;
	int priority = 0;
	int state = 0;

	private void recalculateDisplay()
	{
		int oldState = state;

		for (int x = 0; x < getCellCount(); x++)
			state |= (getCellStatus( x ) << (3 * x));

		if ( getAECurrentPower() > 64 || gridProxy.isActive() )
			state |= 0x40;
		else
			state &= ~0x40;

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
			super( EnumSet.of( TileEventType.TICK, TileEventType.NETWORK, TileEventType.WORLD_NBT ) );
		}

		@Override
		public void Tick()
		{
			if ( Platform.isClient() )
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
		public void writeToStream(DataOutputStream data) throws IOException
		{
			if ( worldObj.getTotalWorldTime() - lastStateChange > 8 )
				state = 0;
			else
				state &= 0x24924924; // just keep the blinks...

			for (int x = 0; x < getCellCount(); x++)
				state |= (getCellStatus( x ) << (3 * x));

			if ( getAECurrentPower() > 64 || gridProxy.isActive() )
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
				data.writeInt( (is.getItemDamage() << Platform.DEF_OFFSET) | is.itemID );
			}
		}

		@Override
		public boolean readFromStream(DataInputStream data) throws IOException
		{
			int oldState = state;
			ItemStack oldType = storageType;

			state = data.readByte();

			int item = data.readInt();

			if ( item == 0 )
				storageType = null;
			else
				storageType = new ItemStack( item & 0xffff, 1, item >> Platform.DEF_OFFSET );

			lastStateChange = worldObj.getTotalWorldTime();

			AELog.info( "" + (state & 0x40) );
			return (state & 0xDB6DB6DB) != (oldState & 0xDB6DB6DB) || oldType != storageType;
		}

		@Override
		public void readFromNBT(NBTTagCompound data)
		{
			priority = data.getInteger( "priority" );
		}

		@Override
		public void writeToNBT(NBTTagCompound data)
		{
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
		gridProxy.setFlags( GridFlags.REQURE_CHANNEL );
		addNewHandler( new invManger() );

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

	class ChestNetNotifier<T extends IAEStack<T>> implements IMEMonitorHandlerReciever<T>
	{

		final StorageChannel chan;

		public ChestNetNotifier(StorageChannel chan) {
			this.chan = chan;
		}

		@Override
		public void postChange(T change)
		{
			try
			{
				gridProxy.getStorage().postAlterationOfStoredItems( chan, change );
			}
			catch (GridAccessException e)
			{
				// :(
			}
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

	};

	private <StackType extends IAEStack> MEMonitorHandler<StackType> wrap(IMEInventoryHandler h)
	{
		if ( h == null )
			return null;

		MEInventoryHandler ih = new MEInventoryHandler( h );
		ih.myPriority = priority;

		MEMonitorHandler<StackType> g = new MEMonitorHandler<StackType>( ih );
		g.addListener( new ChestNetNotifier( h.getChannel() ), g );

		return g;
	}

	private IMEInventoryHandler getHandler(StorageChannel channel) throws AENoHandler
	{
		if ( !isCached )
		{
			ItemStack is = inv.getStackInSlot( 1 );
			if ( is != null )
			{
				isCached = true;
				cellHandler = AEApi.instance().registries().cell().getHander( is );
				icell = wrap( cellHandler.getCellInventory( is, StorageChannel.ITEMS ) );
				fcell = wrap( cellHandler.getCellInventory( is, StorageChannel.FLUIDS ) );
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
				Platform.postChanges( gs, removed, added );

				gridProxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );
			}
			catch (GridAccessException e)
			{

			}

			// update the neighbors
			if ( worldObj != null )
			{
				worldObj.notifyBlocksOfNeighborChange( xCoord, yCoord, zCoord, 0 );
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

				IAEItemStack returns = Platform.poweredInsert( this, cell, AEApi.instance().storage().createItemStack( inv.getStackInSlot( 0 ) ) );

				if ( returns == null )
					inv.setInventorySlotContents( 0, null );
				else
					inv.setInventorySlotContents( 0, returns.getItemStack() );
			}
		}
		catch (AENoHandler t)
		{
		}
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return false;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		try
		{
			IMEInventory<IAEItemStack> cell = getHandler( StorageChannel.ITEMS );
			IAEItemStack returns = cell.injectItems( AEApi.instance().storage().createItemStack( inv.getStackInSlot( 0 ) ), Actionable.SIMULATE );
			return returns == null || returns.getStackSize() != itemstack.stackSize;
		}
		catch (AENoHandler t)
		{
		}

		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if ( side == getForward().ordinal() )
			return front;
		return sides;
	}

	@Override
	public List<IMEInventoryHandler> getCellArray(StorageChannel channel)
	{
		try
		{
			return Arrays.asList( new IMEInventoryHandler[] { getHandler( channel ) } );
		}
		catch (AENoHandler e)
		{
			return new ArrayList();
		}
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
		ICellHandler ch = AEApi.instance().registries().cell().getHander( cell );

		if ( ch != null )
		{
			try
			{
				IMEInventoryHandler handler = getHandler( StorageChannel.ITEMS );
				if ( ch != null )
					return ch.getStatusForCell( cell, handler );
			}
			catch (AENoHandler e)
			{
			}

			try
			{
				IMEInventoryHandler handler = getHandler( StorageChannel.FLUIDS );
				if ( ch != null )
					return ch.getStatusForCell( cell, handler );
			}
			catch (AENoHandler e)
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
				IAEStack results = h.injectItems( AEFluidStack.create( resource ), doFill ? Actionable.MODULATE : Actionable.SIMULATE );

				if ( results == null )
					return resource.amount;

				return resource.amount - (int) results.getStackSize();
			}
			catch (AENoHandler e)
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
		catch (AENoHandler e)
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
				return new FluidTankInfo[] { new FluidTankInfo( null ) }; // eh?
		}
		catch (AENoHandler e)
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

	public ItemStack getStorageType()
	{
		if ( isPowered() )
			return storageType;
		return null;
	}

}
