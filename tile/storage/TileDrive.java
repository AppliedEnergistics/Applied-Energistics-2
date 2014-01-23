package appeng.tile.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
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
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.storage.DriveWatcher;
import appeng.me.storage.MEInventoryHandler;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;

public class TileDrive extends AENetworkInvTile implements IChestOrDrive
{

	final int sides[] = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	AppEngInternalInventory inv = new AppEngInternalInventory( this, 10 );

	boolean isCached = false;
	ICellHandler handlersBySlot[] = new ICellHandler[10];
	DriveWatcher<IAEItemStack> invBySlot[] = new DriveWatcher[10];
	List<MEInventoryHandler> items = new LinkedList();
	List<MEInventoryHandler> fluids = new LinkedList();

	BaseActionSource mySrc;
	long lastStateChange = 0;
	int state = 0;
	int priority = 0;

	private void recalculateDisplay()
	{
		int oldState = 0;

		if ( gridProxy.isActive() )
			state |= 0x80000000;
		else
			state &= ~0x80000000;

		for (int x = 0; x < getCellCount(); x++)
			state |= (getCellStatus( x ) << (3 * x));

		if ( oldState != state )
			markForUpdate();
	}

	private class invManger extends AETileEventHandler
	{

		public invManger() {
			super( EnumSet.of( TileEventType.WORLD_NBT, TileEventType.NETWORK ) );
		}

		@Override
		public void writeToStream(DataOutputStream data) throws IOException
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

		@Override
		public boolean readFromStream(DataInputStream data) throws IOException
		{
			int oldState = state;
			state = data.readInt();
			lastStateChange = worldObj.getTotalWorldTime();
			return (state & 0xDB6DB6DB) != (oldState & 0xDB6DB6DB);
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

	public TileDrive() {
		mySrc = new MachineSource( this );
		gridProxy.setFlags( GridFlags.REQURE_CHANNEL );
		addNewHandler( new invManger() );
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
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		isCached = false; // recalculate the storage cell.
		updateState();

		try
		{
			IStorageGrid gs = gridProxy.getStorage();
			Platform.postChanges( gs, removed, added, mySrc );

			gridProxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );
		}
		catch (GridAccessException e)
		{
		}

		markForUpdate();
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return sides;
	}

	public void updateState()
	{
		if ( !isCached )
		{
			items = new LinkedList();
			fluids = new LinkedList();

			for (int x = 0; x < inv.getSizeInventory(); x++)
			{
				ItemStack is = inv.getStackInSlot( x );
				invBySlot[x] = null;
				handlersBySlot[x] = null;

				if ( is != null )
				{
					handlersBySlot[x] = AEApi.instance().registries().cell().getHander( is );

					if ( handlersBySlot[x] != null )
					{
						IMEInventoryHandler cell = handlersBySlot[x].getCellInventory( is, StorageChannel.ITEMS );

						if ( cell != null )
						{
							DriveWatcher<IAEItemStack> ih = new DriveWatcher( cell, is, handlersBySlot[x], this );
							ih.myPriority = priority;
							invBySlot[x] = ih;
							items.add( ih );
						}
						else
						{
							cell = handlersBySlot[x].getCellInventory( is, StorageChannel.FLUIDS );

							if ( cell != null )
							{
								DriveWatcher<IAEItemStack> ih = new DriveWatcher( cell, is, handlersBySlot[x], this );
								ih.myPriority = priority;
								invBySlot[x] = ih;
								fluids.add( ih );
							}
						}
					}
				}
			}

			isCached = true;
		}
	}

	@Override
	public List<IMEInventoryHandler> getCellArray(StorageChannel channel)
	{
		updateState();
		return (List) (channel == StorageChannel.ITEMS ? items : fluids);
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

}
