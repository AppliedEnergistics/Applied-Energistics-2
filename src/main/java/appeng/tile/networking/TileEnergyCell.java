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


import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.MENetworkPowerStorage;
import appeng.api.networking.events.MENetworkPowerStorage.PowerEventType;
import appeng.api.util.AECableType;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.util.SettingsFrom;


public class TileEnergyCell extends AENetworkTile implements IAEPowerStorage
{

	protected double internalCurrentPower = 0.0;
	protected double internalMaxPower = 200000.0;

	private byte currentMeta = -1;

	public TileEnergyCell()
	{
		this.gridProxy.setIdlePowerUsage( 0 );
	}

	@Override
	public final AECableType getCableConnectionType( ForgeDirection dir )
	{
		return AECableType.COVERED;
	}

	@Override
	public void onReady()
	{
		super.onReady();
		this.currentMeta = (byte) this.worldObj.getBlockMetadata( this.xCoord, this.yCoord, this.zCoord );
		this.changePowerLevel();
	}

	private void changePowerLevel()
	{
		if( this.notLoaded() )
		{
			return;
		}

		byte boundMetadata = (byte) ( 8.0 * ( this.internalCurrentPower / this.internalMaxPower ) );

		if( boundMetadata > 7 )
		{
			boundMetadata = 7;
		}
		if( boundMetadata < 0 )
		{
			boundMetadata = 0;
		}

		if( this.currentMeta != boundMetadata )
		{
			this.currentMeta = boundMetadata;
			this.worldObj.setBlockMetadataWithNotify( this.xCoord, this.yCoord, this.zCoord, this.currentMeta, 2 );
		}
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileEnergyCell( NBTTagCompound data )
	{
		if( !this.worldObj.isRemote )
		{
			data.setDouble( "internalCurrentPower", this.internalCurrentPower );
		}
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileEnergyCell( NBTTagCompound data )
	{
		this.internalCurrentPower = data.getDouble( "internalCurrentPower" );
	}

	@Override
	public final boolean canBeRotated()
	{
		return false;
	}

	@Override
	public void uploadSettings( SettingsFrom from, NBTTagCompound compound )
	{
		if( from == SettingsFrom.DISMANTLE_ITEM )
		{
			this.internalCurrentPower = compound.getDouble( "internalCurrentPower" );
		}
	}

	@Override
	public NBTTagCompound downloadSettings( SettingsFrom from )
	{
		if( from == SettingsFrom.DISMANTLE_ITEM )
		{
			NBTTagCompound tag = new NBTTagCompound();
			tag.setDouble( "internalCurrentPower", this.internalCurrentPower );
			tag.setDouble( "internalMaxPower", this.internalMaxPower ); // used for tool tip.
			return tag;
		}
		return null;
	}

	@Override
	public final double injectAEPower( double amt, Actionable mode )
	{
		if( mode == Actionable.SIMULATE )
		{
			double fakeBattery = this.internalCurrentPower + amt;
			if( fakeBattery > this.internalMaxPower )
			{
				return fakeBattery - this.internalMaxPower;
			}

			return 0;
		}

		if( this.internalCurrentPower < 0.01 && amt > 0.01 )
		{
			this.gridProxy.getNode().getGrid().postEvent( new MENetworkPowerStorage( this, PowerEventType.PROVIDE_POWER ) );
		}

		this.internalCurrentPower += amt;
		if( this.internalCurrentPower > this.internalMaxPower )
		{
			amt = this.internalCurrentPower - this.internalMaxPower;
			this.internalCurrentPower = this.internalMaxPower;

			this.changePowerLevel();
			return amt;
		}

		this.changePowerLevel();
		return 0;
	}

	@Override
	public final double getAEMaxPower()
	{
		return this.internalMaxPower;
	}

	@Override
	public final double getAECurrentPower()
	{
		return this.internalCurrentPower;
	}

	@Override
	public final boolean isAEPublicPowerStorage()
	{
		return true;
	}

	@Override
	public final AccessRestriction getPowerFlow()
	{
		return AccessRestriction.READ_WRITE;
	}

	@Override
	public final double extractAEPower( double amt, Actionable mode, PowerMultiplier pm )
	{
		return pm.divide( this.extractAEPower( pm.multiply( amt ), mode ) );
	}

	private double extractAEPower( double amt, Actionable mode )
	{
		if( mode == Actionable.SIMULATE )
		{
			if( this.internalCurrentPower > amt )
			{
				return amt;
			}
			return this.internalCurrentPower;
		}

		boolean wasFull = this.internalCurrentPower >= this.internalMaxPower - 0.001;

		if( wasFull && amt > 0.001 )
		{
			try
			{
				this.gridProxy.getGrid().postEvent( new MENetworkPowerStorage( this, PowerEventType.REQUEST_POWER ) );
			}
			catch( GridAccessException ignored )
			{

			}
		}

		if( this.internalCurrentPower > amt )
		{
			this.internalCurrentPower -= amt;

			this.changePowerLevel();
			return amt;
		}

		amt = this.internalCurrentPower;
		this.internalCurrentPower = 0;

		this.changePowerLevel();
		return amt;
	}
}
