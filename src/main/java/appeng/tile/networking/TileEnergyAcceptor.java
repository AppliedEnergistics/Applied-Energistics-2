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


import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.util.AECableType;
import appeng.me.GridAccessException;
import appeng.tile.grid.AENetworkPowerTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;


public class TileEnergyAcceptor extends AENetworkPowerTile
{

	final static AppEngInternalInventory INTERNAL_INVENTORY = new AppEngInternalInventory( null, 0 );
	final int[] sides = new int[] {};

	public TileEnergyAcceptor()
	{
		this.gridProxy.setIdlePowerUsage( 0.0 );
		this.internalMaxPower = 0;
	}

	@Override
	public void readFromNBT_AENetwork( NBTTagCompound data )
	{
		/**
		 * Does nothing here since the NBT tag in the parent is not needed anymore
		 */
	}

	@Override
	public void writeToNBT_AENetwork( NBTTagCompound data )
	{
		/**
		 * Does nothing here since the NBT tag in the parent is not needed anymore
		 */
	}

	@Override
	public AECableType getCableConnectionType( ForgeDirection dir )
	{
		return AECableType.COVERED;
	}

	@Override
	protected double getFunnelPowerDemand( double maxRequired )
	{
		try
		{
			IEnergyGrid grid = this.gridProxy.getEnergy();
			return grid.getEnergyDemand( maxRequired );
		}
		catch( GridAccessException e )
		{
			return this.internalMaxPower;
		}
	}

	@Override
	protected double funnelPowerIntoStorage( double newPower, Actionable mode )
	{
		try
		{
			IEnergyGrid grid = this.gridProxy.getEnergy();
			double leftOver = grid.injectPower( newPower, mode );
			if( mode == Actionable.SIMULATE )
				return leftOver;
			return 0.0;
		}
		catch( GridAccessException e )
		{
			return super.funnelPowerIntoStorage( newPower, mode );
		}
	}

	@Override
	public IInventory getInternalInventory()
	{
		return INTERNAL_INVENTORY;
	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added )
	{

	}

	@Override
	public int[] getAccessibleSlotsBySide( ForgeDirection side )
	{
		return this.sides;
	}
}
