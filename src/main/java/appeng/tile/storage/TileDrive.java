/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.tile.storage;


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
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class TileDrive extends AENetworkInvTile implements IChestOrDrive, IPriorityHost
{

	private final int[] sides = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	private final AppEngInternalInventory inv = new AppEngInternalInventory( this, 10 );
	private final ICellHandler[] handlersBySlot = new ICellHandler[10];
	private final DriveWatcher<IAEItemStack>[] invBySlot = new DriveWatcher[10];
	private final BaseActionSource mySrc;
	private boolean isCached = false;
	private List<MEInventoryHandler> items = new LinkedList<MEInventoryHandler>();
	private List<MEInventoryHandler> fluids = new LinkedList<MEInventoryHandler>();
	private long lastStateChange = 0;
	private int state = 0;
	private int priority = 0;
	private boolean wasActive = false;

	public TileDrive()
	{
		this.mySrc = new MachineSource( this );
		this.getProxy().setFlags( GridFlags.REQUIRE_CHANNEL );
	}

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToStream_TileDrive( final ByteBuf data )
	{
		if( this.worldObj.getTotalWorldTime() - this.lastStateChange > 8 )
		{
			this.state = 0;
		}
		else
		{
			this.state &= 0x24924924; // just keep the blinks...
		}

		if( this.getProxy().isActive() )
		{
			this.state |= 0x80000000;
		}
		else
		{
			this.state &= ~0x80000000;
		}

		for( int x = 0; x < this.getCellCount(); x++ )
		{
			this.state |= ( this.getCellStatus( x ) << ( 3 * x ) );
		}

		data.writeInt( this.state );
	}

	@Override
	public int getCellCount()
	{
		return 10;
	}

	@Override
	public int getCellStatus( final int slot )
	{
		if( Platform.isClient() )
		{
			return ( this.state >> ( slot * 3 ) ) & 3;
		}

		final ItemStack cell = this.inv.getStackInSlot( 2 );
		final ICellHandler ch = this.handlersBySlot[slot];

		final MEInventoryHandler handler = this.invBySlot[slot];
		if( handler == null )
		{
			return 0;
		}

		if( handler.getChannel() == StorageChannel.ITEMS )
		{
			if( ch != null )
			{
				return ch.getStatusForCell( cell, handler.getInternal() );
			}
		}

		if( handler.getChannel() == StorageChannel.FLUIDS )
		{
			if( ch != null )
			{
				return ch.getStatusForCell( cell, handler.getInternal() );
			}
		}

		return 0;
	}

	@Override
	public boolean isPowered()
	{
		if( Platform.isClient() )
		{
			return ( this.state & 0x80000000 ) == 0x80000000;
		}

		return this.getProxy().isActive();
	}

	@Override
	public boolean isCellBlinking( final int slot )
	{
		final long now = this.worldObj.getTotalWorldTime();
		if( now - this.lastStateChange > 8 )
		{
			return false;
		}

		return ( ( this.state >> ( slot * 3 + 2 ) ) & 0x01 ) == 0x01;
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean readFromStream_TileDrive( final ByteBuf data )
	{
		final int oldState = this.state;
		this.state = data.readInt();
		this.lastStateChange = this.worldObj.getTotalWorldTime();
		return ( this.state & 0xDB6DB6DB ) != ( oldState & 0xDB6DB6DB );
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileDrive( final NBTTagCompound data )
	{
		this.isCached = false;
		this.priority = data.getInteger( "priority" );
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileDrive( final NBTTagCompound data )
	{
		data.setInteger( "priority", this.priority );
	}

	@MENetworkEventSubscribe
	public void powerRender( final MENetworkPowerStatusChange c )
	{
		this.recalculateDisplay();
	}

	private void recalculateDisplay()
	{
		final boolean currentActive = this.getProxy().isActive();
		int newState = this.state;

		if( currentActive )
		{
			newState |= 0x80000000;
		}
		else
		{
			newState &= ~0x80000000;
		}

		if( this.wasActive != currentActive )
		{
			this.wasActive = currentActive;
			try
			{
				this.getProxy().getGrid().postEvent( new MENetworkCellArrayUpdate() );
			}
			catch( final GridAccessException e )
			{
				// :P
			}
		}

		for( int x = 0; x < this.getCellCount(); x++ )
		{
			newState |= ( this.getCellStatus( x ) << ( 3 * x ) );
		}

		if( newState != this.state )
		{
			this.state = newState;
			this.markForUpdate();
		}
	}

	@MENetworkEventSubscribe
	public void channelRender( final MENetworkChannelsChanged c )
	{
		this.recalculateDisplay();
	}

	@Override
	public AECableType getCableConnectionType( final ForgeDirection dir )
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
		return this.inv;
	}

	@Override
	public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
	{
		return itemstack != null && AEApi.instance().registries().cell().isCellHandled( itemstack );
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{
		if( this.isCached )
		{
			this.isCached = false; // recalculate the storage cell.
			this.updateState();
		}

		try
		{
			this.getProxy().getGrid().postEvent( new MENetworkCellArrayUpdate() );

			final IStorageGrid gs = this.getProxy().getStorage();
			Platform.postChanges( gs, removed, added, this.mySrc );
		}
		catch( final GridAccessException ignored )
		{
		}

		this.markForUpdate();
	}

	@Override
	public int[] getAccessibleSlotsBySide( final ForgeDirection side )
	{
		return this.sides;
	}

	private void updateState()
	{
		if( !this.isCached )
		{
			this.items = new LinkedList();
			this.fluids = new LinkedList();

			double power = 2.0;

			for( int x = 0; x < this.inv.getSizeInventory(); x++ )
			{
				final ItemStack is = this.inv.getStackInSlot( x );
				this.invBySlot[x] = null;
				this.handlersBySlot[x] = null;

				if( is != null )
				{
					this.handlersBySlot[x] = AEApi.instance().registries().cell().getHandler( is );

					if( this.handlersBySlot[x] != null )
					{
						IMEInventoryHandler cell = this.handlersBySlot[x].getCellInventory( is, this, StorageChannel.ITEMS );

						if( cell != null )
						{
							power += this.handlersBySlot[x].cellIdleDrain( is, cell );

							final DriveWatcher<IAEItemStack> ih = new DriveWatcher( cell, is, this.handlersBySlot[x], this );
							ih.setPriority( this.priority );
							this.invBySlot[x] = ih;
							this.items.add( ih );
						}
						else
						{
							cell = this.handlersBySlot[x].getCellInventory( is, this, StorageChannel.FLUIDS );

							if( cell != null )
							{
								power += this.handlersBySlot[x].cellIdleDrain( is, cell );

								final DriveWatcher<IAEItemStack> ih = new DriveWatcher( cell, is, this.handlersBySlot[x], this );
								ih.setPriority( this.priority );
								this.invBySlot[x] = ih;
								this.fluids.add( ih );
							}
						}
					}
				}
			}

			this.getProxy().setIdlePowerUsage( power );

			this.isCached = true;
		}
	}

	@Override
	public void onReady()
	{
		super.onReady();
		this.updateState();
	}

	@Override
	public List<IMEInventoryHandler> getCellArray( final StorageChannel channel )
	{
		if( this.getProxy().isActive() )
		{
			this.updateState();
			return (List) ( channel == StorageChannel.ITEMS ? this.items : this.fluids );
		}
		return new ArrayList();
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
		this.markDirty();

		this.isCached = false; // recalculate the storage cell.
		this.updateState();

		try
		{
			this.getProxy().getGrid().postEvent( new MENetworkCellArrayUpdate() );
		}
		catch( final GridAccessException e )
		{
			// :P
		}
	}

	@Override
	public void blinkCell( final int slot )
	{
		final long now = this.worldObj.getTotalWorldTime();
		if( now - this.lastStateChange > 8 )
		{
			this.state = 0;
		}
		this.lastStateChange = now;

		this.state |= 1 << ( slot * 3 + 2 );

		this.recalculateDisplay();
	}

	@Override
	public void saveChanges( final IMEInventory cellInventory )
	{
		this.worldObj.markTileEntityChunkModified( this.xCoord, this.yCoord, this.zCoord, this );
	}
}
