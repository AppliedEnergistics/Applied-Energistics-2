package appeng.tile.storage;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.me.storage.DriveWatcher;
import appeng.me.storage.MEInventoryHandler;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;

public class TileDrive extends AENetworkInvTile implements IChestOrDrive, IPriorityHost
{

	final int sides[] = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	final AppEngInternalInventory inv = new AppEngInternalInventory( this, 10 );

	boolean isCached = false;
	final ICellHandler[] handlersBySlot = new ICellHandler[10];
	final DriveWatcher<IAEItemStack>[] invBySlot = new DriveWatcher[10];
	List<MEInventoryHandler> items = new LinkedList<MEInventoryHandler>();
	List<MEInventoryHandler> fluids = new LinkedList<MEInventoryHandler>();

	final BaseActionSource mySrc;
	long lastStateChange = 0;
	int state = 0;
	int priority = 0;
	boolean wasActive = false;

	private void recalculateDisplay()
	{
		int oldState = 0;

		boolean currentActive = gridProxy.isActive();
		if ( currentActive )
			state |= 0x80000000;
		else
			state &= ~0x80000000;

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

		for (int x = 0; x < getCellCount(); x++)
			state |= (getCellStatus( x ) << (3 * x));

		if ( oldState != state )
			markForUpdate();
	}

	@TileEvent(TileEventType.NETWORK_WRITE)
	public void writeToStream_TileDrive(ByteBuf data)
	{
		if ( worldObj.getTotalWorldTime() - lastStateChange > 8 )
			state = 0;
		else
			state &= 0x24924924; // just keep the blinks...

		if ( gridProxy.isActive() )
			state |= 0x80000000;
		else
			state &= ~0x80000000;

		for (int x = 0; x < getCellCount(); x++)
			state |= (getCellStatus( x ) << (3 * x));

		data.writeInt( state );
	}

	@TileEvent(TileEventType.NETWORK_READ)
	public boolean readFromStream_TileDrive(ByteBuf data)
	{
		int oldState = state;
		state = data.readInt();
		lastStateChange = worldObj.getTotalWorldTime();
		return (state & 0xDB6DB6DB) != (oldState & 0xDB6DB6DB);
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void readFromNBT_TileDrive(NBTTagCompound data)
	{
		isCached = false;
		priority = data.getInteger( "priority" );
	}

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void writeToNBT_TileDrive(NBTTagCompound data)
	{
		data.setInteger( "priority", priority );
	}

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

	public TileDrive() {
		mySrc = new MachineSource( this );
		gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL );
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
		return inv;
	}

	@Override
	public void onReady()
	{
		super.onReady();
		updateState();
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		if ( isCached )
		{
			isCached = false; // recalculate the storage cell.
			updateState();
		}

		try
		{
			gridProxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );

			IStorageGrid gs = gridProxy.getStorage();
			Platform.postChanges( gs, removed, added, mySrc );
		}
		catch (GridAccessException ignored)
		{
		}

		markForUpdate();
	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection side)
	{
		return sides;
	}

	public void updateState()
	{
		if ( !isCached )
		{
			items = new LinkedList();
			fluids = new LinkedList();

			double power = 2.0;

			for (int x = 0; x < inv.getSizeInventory(); x++)
			{
				ItemStack is = inv.getStackInSlot( x );
				invBySlot[x] = null;
				handlersBySlot[x] = null;

				if ( is != null )
				{
					handlersBySlot[x] = AEApi.instance().registries().cell().getHandler( is );

					if ( handlersBySlot[x] != null )
					{
						IMEInventoryHandler cell = handlersBySlot[x].getCellInventory( is, this, StorageChannel.ITEMS );

						if ( cell != null )
						{
							power += handlersBySlot[x].cellIdleDrain( is, cell );

							DriveWatcher<IAEItemStack> ih = new DriveWatcher( cell, is, handlersBySlot[x], this );
							ih.myPriority = priority;
							invBySlot[x] = ih;
							items.add( ih );
						}
						else
						{
							cell = handlersBySlot[x].getCellInventory( is, this, StorageChannel.FLUIDS );

							if ( cell != null )
							{
								power += handlersBySlot[x].cellIdleDrain( is, cell );

								DriveWatcher<IAEItemStack> ih = new DriveWatcher( cell, is, handlersBySlot[x], this );
								ih.myPriority = priority;
								invBySlot[x] = ih;
								fluids.add( ih );
							}
						}
					}
				}
			}

			gridProxy.setIdlePowerUsage( power );

			isCached = true;
		}
	}

	@Override
	public List<IMEInventoryHandler> getCellArray(StorageChannel channel)
	{
		if ( gridProxy.isActive() )
		{
			updateState();
			return (List) (channel == StorageChannel.ITEMS ? items : fluids);
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
		return 10;
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

		ItemStack cell = inv.getStackInSlot( 2 );
		ICellHandler ch = handlersBySlot[slot];

		MEInventoryHandler handler = invBySlot[slot];
		if ( handler == null )
			return 0;

		if ( handler.getChannel() == StorageChannel.ITEMS )
		{
			if ( ch != null )
				return ch.getStatusForCell( cell, handler.getInternal() );
		}

		if ( handler.getChannel() == StorageChannel.FLUIDS )
		{
			if ( ch != null )
				return ch.getStatusForCell( cell, handler.getInternal() );
		}

		return 0;
	}

	@Override
	public boolean isPowered()
	{
		if ( Platform.isClient() )
			return (state & 0x80000000) == 0x80000000;

		return gridProxy.isActive();
	}

	@Override
	public void setPriority(int newValue)
	{
		priority = newValue;
		markDirty();

		isCached = false; // recalculate the storage cell.
		updateState();

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
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return itemstack != null && AEApi.instance().registries().cell().isCellHandled( itemstack );
	}

	@Override
	public void saveChanges(IMEInventory cellInventory)
	{
		worldObj.markTileEntityChunkModified( this.xCoord, this.yCoord, this.zCoord, this );
	}
}
