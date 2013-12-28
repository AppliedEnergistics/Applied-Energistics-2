package appeng.tile.misc;

import java.util.EnumSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.IStorageMonitorable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.storage.MEMonitorPassthu;
import appeng.me.storage.NullInventory;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorIInventory;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.WrapperInvSlot;

public class TileInterface extends AENetworkInvTile implements IGridTickable, IStorageMonitorable, IInventoryDestination
{

	final int sides[] = new int[] { 0, 1, 2, 3, 4, 5, 6, 7 };
	final IAEItemStack requireWork[] = new IAEItemStack[] { null, null, null, null, null, null, null, null };

	boolean hasConfig = false;

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

		for (int x = 0; x < 8; x++)
			updatePlan( x );

		try
		{
			if ( hasWorkToDo() )
				gridProxy.getTick().wakeDevice( gridProxy.getNode() );
			else
				gridProxy.getTick().sleepDevice( gridProxy.getNode() );
		}
		catch (GridAccessException e)
		{
			// :P
		}

		if ( hadConfig != hasConfig && worldObj != null )
		{
			worldObj.notifyBlocksOfNeighborChange( xCoord, yCoord, zCoord, 0 );
		}
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
		IAEItemStack out = destination.injectItems( AEApi.instance().storage().createItemStack( stack ), Actionable.SIMULATE );
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
			if ( Stored == null )
			{
				requireWork[slot] = req.copy();
				return;
			}
			if ( req.isSameType( Stored ) )
			{
				if ( req.getStackSize() != Stored.stackSize )
				{
					requireWork[slot] = req.copy();
					requireWork[slot].setStackSize( req.getStackSize() - Stored.stackSize );
					return;
				}
			}
			else if ( Stored != null )
			{
				IAEItemStack work = AEApi.instance().storage().createItemStack( Stored );
				requireWork[slot] = work.setStackSize( -work.getStackSize() );
				return;
			}
		}

		// else

		requireWork[slot] = null;
	}

	private boolean usePlan(int x, IAEItemStack itemStack)
	{
		boolean changed = false;
		slotInv.setSlot( x );
		isWorking = true;

		try
		{
			destination = gridProxy.getStorage().getItemInventory();
			IEnergySource src = gridProxy.getEnergy();

			if ( itemStack.getStackSize() > 0 )
			{
				IAEItemStack aquired = Platform.poweredExtraction( src, destination, itemStack );
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

				toStore = Platform.poweredInsert( src, destination, toStore );

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

		isWorking = false;
		return changed;
	}

	class TileInterfaceHandler extends AETileEventHandler
	{

		public TileInterfaceHandler() {
			super( EnumSet.of( TileEventType.WORLD_NBT ) );
		}

		@Override
		public void writeToNBT(NBTTagCompound data)
		{
			config.writeToNBT( data, "config" );
			patterns.writeToNBT( data, "patterns" );
		}

		@Override
		public void readFromNBT(NBTTagCompound data)
		{
			config.readFromNBT( data, "config" );
			patterns.readFromNBT( data, "patterns" );
			readConfig();
		}

	};

	public TileInterface() {
		addNewHandler( new TileInterfaceHandler() );
	}

	public IInventory getConfig()
	{
		return config;
	}

	public IInventory getPatterns()
	{
		return patterns;
	}

	MEMonitorPassthu<IAEItemStack> items = new MEMonitorPassthu<IAEItemStack>( new NullInventory() );
	MEMonitorPassthu<IAEFluidStack> fluids = new MEMonitorPassthu<IAEFluidStack>( new NullInventory() );

	@Override
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

		worldObj.notifyBlocksOfNeighborChange( xCoord, yCoord, zCoord, 0 );
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.SMART;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	@Override
	public IInventory getInternalInventory()
	{
		return storage;
	}

	@Override
	public void onInventoryChanged()
	{
		readConfig();
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
			updatePlan( slot );

			try
			{
				if ( hasWorkToDo() )
					gridProxy.getTick().wakeDevice( gridProxy.getNode() );
				else
					gridProxy.getTick().sleepDevice( gridProxy.getNode() );
			}
			catch (GridAccessException e)
			{
				// :P
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

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return sides;
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( 5, 120, !hasWorkToDo(), false );
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
			return null;

		return items;
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		if ( hasConfig() )
			return null;

		return fluids;
	}

}
