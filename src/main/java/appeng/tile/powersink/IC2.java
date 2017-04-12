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

package appeng.tile.powersink;


import appeng.api.config.PowerUnits;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IIC2;
import appeng.transformer.annotations.Integration.Interface;
import appeng.util.Platform;
import ic2.api.energy.tile.IEnergySink;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;


@Interface( iname = IntegrationType.IC2, iface = "ic2.api.energy.tile.IEnergySink" )
public abstract class IC2 extends AERootPoweredTile implements IEnergySink
{

	private boolean isInIC2 = false;

	@Override
	public final boolean acceptsEnergyFrom( final TileEntity emitter, final ForgeDirection direction )
	{
		return this.getPowerSides().contains( direction );
	}

	@Override
	public final double getDemandedEnergy()
	{
		return this.getExternalPowerDemand( PowerUnits.EU, Double.MAX_VALUE );
	}

	@Override
	public final int getSinkTier()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public final double injectEnergy( final ForgeDirection directionFrom, final double amount, final double voltage )
	{
		// just store the excess in the current block, if I return the waste,
		// IC2 will just disintegrate it - Oct 20th 2013
		final double overflow = PowerUnits.EU.convertTo( PowerUnits.AE, this.injectExternalPower( PowerUnits.EU, amount ) );
		this.setInternalCurrentPower( this.getInternalCurrentPower() + overflow );
		return 0; // see above comment.
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		this.removeFromENet();
	}

	private void removeFromENet()
	{
		if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.IC2 ) )
		{
			final IIC2 ic2Integration = (IIC2) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.IC2 );
			if( this.isInIC2 && Platform.isServer() && ic2Integration != null )
			{
				ic2Integration.removeFromEnergyNet( this );
				this.isInIC2 = false;
			}
		}
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

	private void addToENet()
	{
		if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.IC2 ) )
		{
			final IIC2 ic2Integration = (IIC2) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.IC2 );
			if( !this.isInIC2 && Platform.isServer() && ic2Integration != null )
			{
				ic2Integration.addToEnergyNet( this );
				this.isInIC2 = true;
			}
		}
	}

	@Override
	protected void setPowerSides( final EnumSet<ForgeDirection> sides )
	{
		super.setPowerSides( sides );
		this.removeFromENet();
		this.addToENet();
	}
}
