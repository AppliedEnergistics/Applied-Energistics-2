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

	boolean isValid = false;

	public TileController() {
		internalMaxPower = 8000;
		internalPublicPowerStorage = true;
		gridProxy.setIdlePowerUsage( 3 );
		gridProxy.setFlags( GridFlags.CANNOT_CARRY, GridFlags.DENSE_CAPACITY );
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.DENSE;
	}

	@Override
	protected double getFunnelPowerDemand(double maxReceived)
	{
		try
		{
			return gridProxy.getEnergy().getEnergyDemand( 8000 );
		}
		catch (GridAccessException e)
		{
			// no grid? use local...
			return super.getFunnelPowerDemand( maxReceived );
		}
	}

	@Override
	protected double funnelPowerIntoStorage(double AEUnits, Actionable mode)
	{
		try
		{
			double ret = gridProxy.getEnergy().injectPower( AEUnits, mode );
			if ( mode == Actionable.SIMULATE )
				return ret;
			return 0;
		}
		catch (GridAccessException e)
		{
			// no grid? use local...
			return super.funnelPowerIntoStorage( AEUnits, mode );
		}
	}

	@Override
	protected void PowerEvent(PowerEventType x)
	{
		try
		{
			gridProxy.getGrid().postEvent( new MENetworkPowerStorage( this, x ) );
		}
		catch (GridAccessException e)
		{
			// not ready!
		}
	}

	@MENetworkEventSubscribe
	public void onControllerChange(MENetworkControllerChange status)
	{
		updateMeta();
	}

	@MENetworkEventSubscribe
	public void onPowerChange(MENetworkPowerStatusChange status)
	{
		updateMeta();
	}

	@Override
	public void onReady()
	{
		onNeighborChange( true );
		super.onReady();
	}

	public void onNeighborChange(boolean force)
	{
		boolean xx = worldObj.getTileEntity( xCoord - 1, yCoord, zCoord ) instanceof TileController
				&& worldObj.getTileEntity( xCoord + 1, yCoord, zCoord ) instanceof TileController;
		boolean yy = worldObj.getTileEntity( xCoord, yCoord - 1, zCoord ) instanceof TileController
				&& worldObj.getTileEntity( xCoord, yCoord + 1, zCoord ) instanceof TileController;
		boolean zz = worldObj.getTileEntity( xCoord, yCoord, zCoord - 1 ) instanceof TileController
				&& worldObj.getTileEntity( xCoord, yCoord, zCoord + 1 ) instanceof TileController;

		// int meta = world.getBlockMetadata( xCoord, yCoord, zCoord );
		// boolean hasPower = meta > 0;
		// boolean isConflict = meta == 2;

		boolean oldValid = isValid;

		isValid = (xx && !yy && !zz) || (!xx && yy && !zz) || (!xx && !yy && zz) || ((xx ? 1 : 0) + (yy ? 1 : 0) + (zz ? 1 : 0) <= 1);

		if ( oldValid != isValid || force )
		{
			if ( isValid )
				gridProxy.setValidSides( EnumSet.allOf( ForgeDirection.class ) );
			else
				gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
		}

		updateMeta();
	}

	private void updateMeta()
	{
		if ( !gridProxy.isReady() )
			return;

		int meta = 0;

		try
		{
			if ( gridProxy.getEnergy().isNetworkPowered() )
			{
				meta = 1;

				if ( gridProxy.getPath().getControllerState() == ControllerState.CONTROLLER_CONFLICT )
					meta = 2;
			}
		}
		catch (GridAccessException e)
		{
			meta = 0;
		}

		worldObj.setBlockMetadataWithNotify( xCoord, yCoord, zCoord, meta, 2 );
	}

	final int sides[] = new int[] {};
	static final AppEngInternalInventory inv = new AppEngInternalInventory( null, 0 );

	@Override
	public IInventory getInternalInventory()
	{
		return inv;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{

	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection side)
	{
		return sides;
	}

}
