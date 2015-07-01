/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.tile.networking;


import java.util.EnumSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkControllerChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkPowerStorage;
import appeng.api.networking.events.MENetworkPowerStorage.PowerEventType;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.util.AECableType;
import appeng.me.GridAccessException;
import appeng.tile.grid.AENetworkPowerTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;


public class TileController extends AENetworkPowerTile
{
	private static final IInventory NULL_INVENTORY = new AppEngInternalInventory( null, 0 );
	private static final int[] ACCESSIBLE_SLOTS_BY_SIDE = new int[] {};

	private boolean isValid = false;

	public TileController()
	{
		this.internalMaxPower = 8000;
		this.internalPublicPowerStorage = true;
		this.gridProxy.setIdlePowerUsage( 3 );
		this.gridProxy.setFlags( GridFlags.CANNOT_CARRY, GridFlags.DENSE_CAPACITY );
	}

	@Override
	public AECableType getCableConnectionType( ForgeDirection dir )
	{
		return AECableType.DENSE;
	}

	@Override
	public void onReady()
	{
		this.onNeighborChange( true );
		super.onReady();
	}

	public void onNeighborChange( boolean force )
	{
		final boolean xx = checkController( this.xCoord - 1, this.yCoord, this.zCoord ) && checkController( this.xCoord + 1, this.yCoord, this.zCoord );
		final boolean yy = checkController( this.xCoord, this.yCoord - 1, this.zCoord ) && checkController( this.xCoord, this.yCoord + 1, this.zCoord );
		final boolean zz = checkController( this.xCoord, this.yCoord, this.zCoord - 1 ) && checkController( this.xCoord, this.yCoord, this.zCoord + 1 );

		// int meta = world.getBlockMetadata( xCoord, yCoord, zCoord );
		// boolean hasPower = meta > 0;
		// boolean isConflict = meta == 2;

		final boolean oldValid = this.isValid;

		this.isValid = ( xx && !yy && !zz ) || ( !xx && yy && !zz ) || ( !xx && !yy && zz ) || ( ( xx ? 1 : 0 ) + ( yy ? 1 : 0 ) + ( zz ? 1 : 0 ) <= 1 );

		if( oldValid != this.isValid || force )
		{
			if( this.isValid )
			{
				this.gridProxy.setValidSides( EnumSet.allOf( ForgeDirection.class ) );
			}
			else
			{
				this.gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
			}

			this.updateMeta();
		}

	}

	private void updateMeta()
	{
		if( !this.gridProxy.isReady() )
		{
			return;
		}

		int meta = 0;

		try
		{
			if( this.gridProxy.getEnergy().isNetworkPowered() )
			{
				meta = 1;

				if( this.gridProxy.getPath().getControllerState() == ControllerState.CONTROLLER_CONFLICT )
				{
					meta = 2;
				}
			}
		}
		catch( GridAccessException e )
		{
			meta = 0;
		}

		if( checkController( this.xCoord, this.yCoord, this.zCoord ) && this.worldObj.getBlockMetadata( this.xCoord, this.yCoord, this.zCoord ) != meta )
		{
			this.worldObj.setBlockMetadataWithNotify( this.xCoord, this.yCoord, this.zCoord, meta, 2 );
		}
	}

	@Override
	protected double getFunnelPowerDemand( double maxReceived )
	{
		try
		{
			return this.gridProxy.getEnergy().getEnergyDemand( 8000 );
		}
		catch( GridAccessException e )
		{
			// no grid? use local...
			return super.getFunnelPowerDemand( maxReceived );
		}
	}

	@Override
	protected double funnelPowerIntoStorage( double power, Actionable mode )
	{
		try
		{
			double ret = this.gridProxy.getEnergy().injectPower( power, mode );
			if( mode == Actionable.SIMULATE )
			{
				return ret;
			}
			return 0;
		}
		catch( GridAccessException e )
		{
			// no grid? use local...
			return super.funnelPowerIntoStorage( power, mode );
		}
	}

	@Override
	protected void PowerEvent( PowerEventType x )
	{
		try
		{
			this.gridProxy.getGrid().postEvent( new MENetworkPowerStorage( this, x ) );
		}
		catch( GridAccessException e )
		{
			// not ready!
		}
	}

	@MENetworkEventSubscribe
	public void onControllerChange( MENetworkControllerChange status )
	{
		this.updateMeta();
	}

	@MENetworkEventSubscribe
	public void onPowerChange( MENetworkPowerStatusChange status )
	{
		this.updateMeta();
	}

	@Override
	public IInventory getInternalInventory()
	{
		return NULL_INVENTORY;
	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added )
	{

	}

	@Override
	public int[] getAccessibleSlotsBySide( ForgeDirection side )
	{
		return ACCESSIBLE_SLOTS_BY_SIDE;
	}

	/**
	 * Check for a controller at this coordinates as well as is it loaded.
	 *
	 * @return true if there is a loaded controller
	 */
	private boolean checkController( int x, int y, int z )
	{
		if( this.worldObj.getChunkProvider().chunkExists( this.xCoord >> 4, this.zCoord >> 4 ) )
		{
			return this.worldObj.getTileEntity( x, y, z ) instanceof TileController;
		}
		return false;
	}
}
