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

import appeng.integration.abstraction.helpers.BaseMJPerdition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.PowerUnits;
import appeng.core.AppEng;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IMJ5;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.transformer.annotations.integration.Interface;
import appeng.transformer.annotations.integration.Method;
import appeng.util.Platform;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;

@Interface(iname = "MJ5", iface = "buildcraft.api.power.IPowerReceptor")
public abstract class MinecraftJoules5 extends AERootPoweredTile implements IPowerReceptor
{

	BaseMJPerdition bcPowerWrapper;

	@Method(iname = "MJ5")
	@TileEvent(TileEventType.TICK)
	public void Tick_MinecraftJoules5()
	{
		if ( this.bcPowerWrapper != null )
			this.bcPowerWrapper.Tick();
	}

	public MinecraftJoules5() {
		if ( Platform.isServer() )
		{
			try
			{
				if ( AppEng.instance.isIntegrationEnabled( IntegrationType.MJ5 ) )
				{
					IMJ5 mjIntegration = (IMJ5) AppEng.instance.getIntegration( IntegrationType.MJ5 );
					if ( mjIntegration != null )
					{
						this.bcPowerWrapper = (BaseMJPerdition) mjIntegration.createPerdition( this );
						if ( this.bcPowerWrapper != null )
							this.bcPowerWrapper.configure( 1, 380, 1.0f / 5.0f, 1000 );
					}
				}
			}
			catch (Throwable t)
			{
				// ignore.. no bc?
			}
		}
	}

	@Override
	@Method(iname = "MJ5")
	final public PowerReceiver getPowerReceiver(ForgeDirection side)
	{
		if ( this.getPowerSides().contains( side ) && this.bcPowerWrapper != null )
			return this.bcPowerWrapper.getPowerReceiver();
		return null;
	}

	@Override
	@Method(iname = "MJ5")
	final public void doWork(PowerHandler workProvider)
	{
		float required = (float) this.getExternalPowerDemand( PowerUnits.MJ, this.bcPowerWrapper.getPowerReceiver().getEnergyStored() );
		double failed = this.injectExternalPower( PowerUnits.MJ, this.bcPowerWrapper.useEnergy( 0.0f, required, true ) );
		if ( failed > 0.01 )
			this.bcPowerWrapper.addEnergy( (float) failed );
	}

	@Override
	@Method(iname = "MJ5")
	final public World getWorld()
	{
		return this.worldObj;
	}

}
