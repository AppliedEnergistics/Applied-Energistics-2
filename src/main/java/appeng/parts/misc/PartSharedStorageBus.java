/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.parts.misc;


import java.util.Collections;
import java.util.List;

import appeng.api.config.AccessRestriction;
import appeng.fluids.parts.PartFluidInterface;
import appeng.fluids.tile.TileFluidInterface;
import appeng.tile.misc.TileInterface;
import appeng.tile.networking.TileCableBus;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import appeng.api.AEApi;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.parts.automation.PartUpgradeable;


/**
 * @author BrockWS
 * @version rv6 - 22/05/2018
 * @since rv6 22/05/2018
 */
public abstract class PartSharedStorageBus extends PartUpgradeable implements IGridTickable, ICellContainer, IPriorityHost
{
	private boolean wasActive = false;
	private int priority = 0;
	protected boolean accessChanged;

	public PartSharedStorageBus( ItemStack is )
	{
		super( is );
	}

	protected void updateStatus()
	{
		final boolean currentActive = this.getProxy().isActive();
		if( this.wasActive != currentActive )
		{
			this.wasActive = currentActive;
			try
			{
				this.getProxy().getGrid().postEvent( new MENetworkCellArrayUpdate() );
				this.getHost().markForUpdate();
			}
			catch( final GridAccessException ignore )
			{
				// :P
			}
		}
	}

	@MENetworkEventSubscribe
	public void updateChannels( final MENetworkChannelsChanged changedChannels )
	{
		this.updateStatus();
	}

	/**
	 * Helper method to get this parts storage channel
	 *
	 * @return Storage channel
	 */
	public IStorageChannel getStorageChannel()
	{
		return AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class );
	}

	protected abstract void resetCache();

	protected abstract void resetCache( boolean fullReset );

	@Override
	public List<IMEInventoryHandler> getCellArray( final IStorageChannel channel )
	{
		return Collections.emptyList();
	}

	@Override
	public void blinkCell( int slot )
	{
	}

	@Override
	public void saveChanges( ICellInventory<?> cellInventory )
	{
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
		this.getHost().markForSave();
		this.resetCache( true );
	}

	@Override
	@MENetworkEventSubscribe
	public void powerRender( final MENetworkPowerStatusChange c )
	{
		this.updateStatus();
	}

	@Override
	public void upgradesChanged()
	{
		super.upgradesChanged();
		this.resetCache( true );
	}

	@Override
	public void updateSetting( final IConfigManager manager, final Enum settingName, final Enum newValue )
	{
		if( settingName instanceof AccessRestriction )
		{
			this.accessChanged = true;
		}
		this.resetCache( true );
		this.getHost().markForSave();
	}

	@Override
	public void onNeighborChanged( IBlockAccess w, BlockPos pos, BlockPos neighbor )
	{
		if( pos.offset( this.getSide().getFacing() ).equals( neighbor ) )
		{

			final TileEntity te = w.getTileEntity( neighbor );

			// In case the TE was destroyed, we have to do a full reset immediately.
			if( te instanceof TileCableBus )
			{
				if( ( (TileCableBus) te ).getPart( this.getSide().getOpposite() ) instanceof PartFluidInterface )
				{
					this.resetCache( true );
					this.resetCache();
				}
			}
			if( te == null || te instanceof TileFluidInterface )
			{
				this.resetCache( true );
				this.resetCache();
			}
			else
			{
				this.resetCache( false );
			}
		}
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.priority = data.getInteger( "priority" );
	}

	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );
		data.setInteger( "priority", this.priority );
	}

	@Override
	public void getBoxes( final IPartCollisionHelper bch )
	{
		bch.addBox( 3, 3, 15, 13, 13, 16 );
		bch.addBox( 2, 2, 14, 14, 14, 15 );
		bch.addBox( 5, 5, 12, 11, 11, 14 );
	}

	@Override
	protected int getUpgradeSlots()
	{
		return 5;
	}

	@Override
	public float getCableConnectionLength( AECableType cable )
	{
		return 4;
	}
}
