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

import java.util.EnumSet;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import ic2.api.energy.tile.IEnergySink;

import appeng.api.config.PowerUnits;
import appeng.core.AppEng;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IIC2;
import appeng.transformer.annotations.integration.Interface;
import appeng.util.Platform;

@Interface(iname = "IC2", iface = "ic2.api.energy.tile.IEnergySink")
public abstract class IC2 extends MinecraftJoules6 implements IEnergySink
{

	boolean isInIC2 = false;

	@Override
	final public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return this.getPowerSides().contains( direction );
	}

	@Override
	final public double getDemandedEnergy()
	{
		return this.getExternalPowerDemand( PowerUnits.EU, Double.MAX_VALUE );
	}

	@Override
	final public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage)
	{
		// just store the excess in the current block, if I return the waste,
		// IC2 will just disintegrate it - Oct 20th 2013
		double overflow = PowerUnits.EU.convertTo( PowerUnits.AE, this.injectExternalPower( PowerUnits.EU, amount ) );
		this.internalCurrentPower += overflow;
		return 0; // see above comment.
	}

	@Override
	final public int getSinkTier()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		this.removeFromENet();
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		this.removeFromENet();
	}

	@Override
	public void onReady()
	{
		super.onReady();
		this.addToENet();
	}

	@Override
	protected void setPowerSides(EnumSet<ForgeDirection> sides)
	{
		super.setPowerSides( sides );
		this.removeFromENet();
		this.addToENet();
	}

	private void addToENet()
	{
		if ( AppEng.instance.isIntegrationEnabled( IntegrationType.IC2 ) )
		{
			IIC2 ic2Integration = (IIC2) AppEng.instance.getIntegration( IntegrationType.IC2 );
			if ( !this.isInIC2 && Platform.isServer() && ic2Integration != null )
			{
				ic2Integration.addToEnergyNet( this );
				this.isInIC2 = true;
			}
		}
	}

	private void removeFromENet()
	{
		if ( AppEng.instance.isIntegrationEnabled( IntegrationType.IC2 ) )
		{
			IIC2 ic2Integration = (IIC2) AppEng.instance.getIntegration( IntegrationType.IC2 );
			if ( this.isInIC2 && Platform.isServer() && ic2Integration != null )
			{
				ic2Integration.removeFromEnergyNet( this );
				this.isInIC2 = false;
			}
		}
	}

}
