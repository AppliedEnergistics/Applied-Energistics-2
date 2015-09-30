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
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkControllerChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkPowerStorage;
import appeng.api.networking.events.MENetworkPowerStorage.PowerEventType;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.block.networking.BlockController;
import appeng.block.networking.BlockController.ControllerBlockState;
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
	public AECableType getCableConnectionType( AEPartLocation dir )
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
		final boolean xx = checkController( pos.offset( EnumFacing.EAST ) ) && checkController( pos.offset( EnumFacing.WEST ) );
		final boolean yy = checkController( pos.offset( EnumFacing.UP ) ) && checkController( pos.offset( EnumFacing.DOWN ) );
		final boolean zz = checkController( pos.offset( EnumFacing.NORTH ) ) && checkController(  pos.offset( EnumFacing.SOUTH ) );

		// int meta = world.getBlockMetadata( xCoord, yCoord, zCoord );
		// boolean hasPower = meta > 0;
		// boolean isConflict = meta == 2;

		final boolean oldValid = this.isValid;

		this.isValid = ( xx && !yy && !zz ) || ( !xx && yy && !zz ) || ( !xx && !yy && zz ) || ( ( xx ? 1 : 0 ) + ( yy ? 1 : 0 ) + ( zz ? 1 : 0 ) <= 1 );

		if( oldValid != this.isValid || force )
		{
			if( this.isValid )
			{
				this.gridProxy.setValidSides( EnumSet.allOf( EnumFacing.class ) );
			}
			else
			{
				this.gridProxy.setValidSides( EnumSet.noneOf( EnumFacing.class ) );
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

		ControllerBlockState metaState = ControllerBlockState.OFFLINE;
		
		try
		{
			if( this.gridProxy.getEnergy().isNetworkPowered() )
			{
				metaState = ControllerBlockState.ONLINE;

				if( this.gridProxy.getPath().getControllerState() == ControllerState.CONTROLLER_CONFLICT )
				{
					metaState = ControllerBlockState.CONFLICTED;
				}
			}
		}
		catch( GridAccessException e )
		{
			metaState = ControllerBlockState.OFFLINE;
		}
		
		if( checkController( pos ) && this.worldObj.getBlockState( pos ).getValue( BlockController.CONTROLLER_STATE ) != metaState )
		{
			this.worldObj.setBlockState( pos, worldObj.getBlockState( pos ).withProperty( BlockController.CONTROLLER_STATE, metaState ) );
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
	public int[] getAccessibleSlotsBySide( EnumFacing whichSide )
	{
		return ACCESSIBLE_SLOTS_BY_SIDE;
	}

	/**
	 * Check for a controller at this coordinates as well as is it loaded.
	 *
	 * @return true if there is a loaded controller
	 */
	private boolean checkController( BlockPos pos )
	{
		final BlockPos ownPos = this.getPos();
		if( this.worldObj.getChunkProvider().chunkExists( ownPos.getX() >> 4, ownPos.getZ() >> 4 ) )
		{
			return this.worldObj.getTileEntity( pos ) instanceof TileController;
		}
		
		return false;
	}
}
