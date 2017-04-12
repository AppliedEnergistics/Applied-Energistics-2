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


import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.MENetworkPowerStorage.PowerEventType;
import appeng.tile.AEBaseInvTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;


public abstract class AERootPoweredTile extends AEBaseInvTile implements IAEPowerStorage
{

	// values that determine general function, are set by inheriting classes if
	// needed. These should generally remain static.
	private double internalMaxPower = 10000;
	private boolean internalPublicPowerStorage = false;
	private AccessRestriction internalPowerFlow = AccessRestriction.READ_WRITE;
	// the current power buffer.
	private double internalCurrentPower = 0;
	private EnumSet<ForgeDirection> internalPowerSides = EnumSet.allOf( ForgeDirection.class );

	protected EnumSet<ForgeDirection> getPowerSides()
	{
		return this.internalPowerSides.clone();
	}

	protected void setPowerSides( final EnumSet<ForgeDirection> sides )
	{
		this.internalPowerSides = sides;
		// trigger re-calc!
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_AERootPoweredTile( final NBTTagCompound data )
	{
		data.setDouble( "internalCurrentPower", this.getInternalCurrentPower() );
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_AERootPoweredTile( final NBTTagCompound data )
	{
		this.setInternalCurrentPower( data.getDouble( "internalCurrentPower" ) );
	}

	protected final double getExternalPowerDemand( final PowerUnits externalUnit, final double maxPowerRequired )
	{
		return PowerUnits.AE.convertTo( externalUnit, Math.max( 0.0, this.getFunnelPowerDemand( externalUnit.convertTo( PowerUnits.AE, maxPowerRequired ) ) ) );
	}

	protected double getFunnelPowerDemand( final double maxRequired )
	{
		return this.getInternalMaxPower() - this.getInternalCurrentPower();
	}

	public final double injectExternalPower( final PowerUnits input, final double amt )
	{
		return PowerUnits.AE.convertTo( input, this.funnelPowerIntoStorage( input.convertTo( PowerUnits.AE, amt ), Actionable.MODULATE ) );
	}

	protected double funnelPowerIntoStorage( final double power, final Actionable mode )
	{
		return this.injectAEPower( power, mode );
	}

	@Override
	public final double injectAEPower( double amt, final Actionable mode )
	{
		if( amt < 0.000001 )
		{
			return 0;
		}

		if( mode == Actionable.SIMULATE )
		{
			final double fakeBattery = this.getInternalCurrentPower() + amt;

			if( fakeBattery > this.getInternalMaxPower() )
			{
				return fakeBattery - this.getInternalMaxPower();
			}

			return 0;
		}
		else
		{
			if( this.getInternalCurrentPower() < 0.01 && amt > 0.01 )
			{
				this.PowerEvent( PowerEventType.PROVIDE_POWER );
			}

			this.setInternalCurrentPower( this.getInternalCurrentPower() + amt );
			if( this.getInternalCurrentPower() > this.getInternalMaxPower() )
			{
				amt = this.getInternalCurrentPower() - this.getInternalMaxPower();
				this.setInternalCurrentPower( this.getInternalMaxPower() );
				return amt;
			}

			return 0;
		}
	}

	protected void PowerEvent( final PowerEventType x )
	{
		// nothing.
	}

	@Override
	public final double getAEMaxPower()
	{
		return this.getInternalMaxPower();
	}

	@Override
	public final double getAECurrentPower()
	{
		return this.getInternalCurrentPower();
	}

	@Override
	public final boolean isAEPublicPowerStorage()
	{
		return this.isInternalPublicPowerStorage();
	}

	@Override
	public final AccessRestriction getPowerFlow()
	{
		return this.getInternalPowerFlow();
	}

	@Override
	public final double extractAEPower( final double amt, final Actionable mode, final PowerMultiplier multiplier )
	{
		return multiplier.divide( this.extractAEPower( multiplier.multiply( amt ), mode ) );
	}

	protected double extractAEPower( double amt, final Actionable mode )
	{
		if( mode == Actionable.SIMULATE )
		{
			if( this.getInternalCurrentPower() > amt )
			{
				return amt;
			}
			return this.getInternalCurrentPower();
		}

		final boolean wasFull = this.getInternalCurrentPower() >= this.getInternalMaxPower() - 0.001;
		if( wasFull && amt > 0.001 )
		{
			this.PowerEvent( PowerEventType.REQUEST_POWER );
		}

		if( this.getInternalCurrentPower() > amt )
		{
			this.setInternalCurrentPower( this.getInternalCurrentPower() - amt );
			return amt;
		}

		amt = this.getInternalCurrentPower();
		this.setInternalCurrentPower( 0 );
		return amt;
	}

	public double getInternalCurrentPower()
	{
		return this.internalCurrentPower;
	}

	public void setInternalCurrentPower( final double internalCurrentPower )
	{
		this.internalCurrentPower = internalCurrentPower;
	}

	public double getInternalMaxPower()
	{
		return this.internalMaxPower;
	}

	public void setInternalMaxPower( final double internalMaxPower )
	{
		this.internalMaxPower = internalMaxPower;
	}

	private boolean isInternalPublicPowerStorage()
	{
		return this.internalPublicPowerStorage;
	}

	public void setInternalPublicPowerStorage( final boolean internalPublicPowerStorage )
	{
		this.internalPublicPowerStorage = internalPublicPowerStorage;
	}

	private AccessRestriction getInternalPowerFlow()
	{
		return this.internalPowerFlow;
	}

	public void setInternalPowerFlow( final AccessRestriction internalPowerFlow )
	{
		this.internalPowerFlow = internalPowerFlow;
	}
}
