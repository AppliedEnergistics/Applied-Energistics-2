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


import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.util.AECableType;
import appeng.tile.grid.AENetworkTile;


public final class TileCreativeEnergyCell extends AENetworkTile implements IAEPowerStorage
{

	public TileCreativeEnergyCell()
	{
		this.gridProxy.setIdlePowerUsage( 0 );
	}

	@Override
	public final AECableType getCableConnectionType( ForgeDirection dir )
	{
		return AECableType.COVERED;
	}

	@Override
	public final double injectAEPower( double amt, Actionable mode )
	{
		return 0;
	}

	@Override
	public final double getAEMaxPower()
	{
		return Long.MAX_VALUE / 10000;
	}

	@Override
	public final double getAECurrentPower()
	{
		return Long.MAX_VALUE / 10000;
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
		return amt;
	}
}
