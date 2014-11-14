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

package appeng.parts.layers;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;
import cofh.api.energy.IEnergyHandler;

public class LayerIEnergyHandler extends LayerBase implements IEnergyHandler
{

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		IPart part = getPart( from );
		if ( part instanceof IEnergyHandler )
			return ((IEnergyHandler) part).receiveEnergy( from, maxReceive, simulate );

		return 0;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		IPart part = getPart( from );
		if ( part instanceof IEnergyHandler )
			return ((IEnergyHandler) part).extractEnergy( from, maxExtract, simulate );

		return 0;
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		IPart part = getPart( from );
		if ( part instanceof IEnergyHandler )
			return ((IEnergyHandler) part).getEnergyStored( from );

		return 0;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		IPart part = getPart( from );
		if ( part instanceof IEnergyHandler )
			return ((IEnergyHandler) part).getMaxEnergyStored( from );

		return 0;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		IPart part = getPart( from );
		if ( part instanceof IEnergyHandler )
			return ((IEnergyHandler) part).canConnectEnergy( from );

		return false;
	}

}
