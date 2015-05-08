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

package appeng.tile.powersink;


import java.util.Collection;
import java.util.EnumSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.MENetworkPowerStorage.PowerEventType;
import appeng.tile.AEBaseInvTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;


public abstract class AERootPoweredTile extends AEBaseInvTile implements IAEPowerStorage
{

	protected final boolean internalCanAcceptPower = true;
	// values that determine general function, are set by inheriting classes if
	// needed. These should generally remain static.
	protected double internalMaxPower = 10000;
	protected boolean internalPublicPowerStorage = false;
	protected AccessRestriction internalPowerFlow = AccessRestriction.READ_WRITE;
	// the current power buffer.
	protected double internalCurrentPower = 0;
	private EnumSet<ForgeDirection> internalPowerSides = EnumSet.allOf( ForgeDirection.class );

	protected Collection<ForgeDirection> getPowerSides()
	{
		return this.internalPowerSides.clone();
	}

	protected void setPowerSides( EnumSet<ForgeDirection> sides )
	{
		this.internalPowerSides = sides;
		// trigger re-calc!
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_AERootPoweredTile( NBTTagCompound data )
	{
		data.setDouble( "internalCurrentPower", this.internalCurrentPower );
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_AERootPoweredTile( NBTTagCompound data )
	{
		this.internalCurrentPower = data.getDouble( "internalCurrentPower" );
	}

	protected final double getExternalPowerDemand( PowerUnits externalUnit, double maxPowerRequired )
	{
		return PowerUnits.AE.convertTo( externalUnit, Math.max( 0.0, this.getFunnelPowerDemand( externalUnit.convertTo( PowerUnits.AE, maxPowerRequired ) ) ) );
	}

	protected double getFunnelPowerDemand( double maxRequired )
	{
		return this.internalMaxPower - this.internalCurrentPower;
	}

	public final double injectExternalPower( PowerUnits input, double amt )
	{
		return PowerUnits.AE.convertTo( input, this.funnelPowerIntoStorage( input.convertTo( PowerUnits.AE, amt ), Actionable.MODULATE ) );
	}

	protected double funnelPowerIntoStorage( double power, Actionable mode )
	{
		return this.injectAEPower( power, mode );
	}

	@Override
	public final double injectAEPower( double amt, Actionable mode )
	{
		if( amt < 0.000001 )
		{
			return 0;
		}

		if( mode == Actionable.SIMULATE )
		{
			double fakeBattery = this.internalCurrentPower + amt;

			if( fakeBattery > this.internalMaxPower )
			{
				return fakeBattery - this.internalMaxPower;
			}

			return 0;
		}
		else
		{
			if( this.internalCurrentPower < 0.01 && amt > 0.01 )
			{
				this.PowerEvent( PowerEventType.PROVIDE_POWER );
			}

			this.internalCurrentPower += amt;
			if( this.internalCurrentPower > this.internalMaxPower )
			{
				amt = this.internalCurrentPower - this.internalMaxPower;
				this.internalCurrentPower = this.internalMaxPower;
				return amt;
			}

			return 0;
		}
	}

	protected void PowerEvent( PowerEventType x )
	{
		// nothing.
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
		return this.internalPublicPowerStorage;
	}

	@Override
	public final AccessRestriction getPowerFlow()
	{
		return this.internalPowerFlow;
	}

	@Override
	public final double extractAEPower( double amt, Actionable mode, PowerMultiplier multiplier )
	{
		return multiplier.divide( this.extractAEPower( multiplier.multiply( amt ), mode ) );
	}

	protected double extractAEPower( double amt, Actionable mode )
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
			this.PowerEvent( PowerEventType.REQUEST_POWER );
		}

		if( this.internalCurrentPower > amt )
		{
			this.internalCurrentPower -= amt;
			return amt;
		}

		amt = this.internalCurrentPower;
		this.internalCurrentPower = 0;
		return amt;
	}
}
