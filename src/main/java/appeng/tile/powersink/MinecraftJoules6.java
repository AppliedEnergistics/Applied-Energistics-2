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

import appeng.api.config.PowerUnits;
import appeng.transformer.annotations.integration.Interface;
import appeng.transformer.annotations.integration.InterfaceList;
import appeng.transformer.annotations.integration.Method;
import buildcraft.api.mj.IBatteryObject;
import buildcraft.api.mj.IBatteryProvider;

@InterfaceList(value = { @Interface(iname = "MJ6", iface = "buildcraft.api.mj.IBatteryProvider"),
		@Interface(iname = "MJ6", iface = "buildcraft.api.mj.IBatteryObject") })
public abstract class MinecraftJoules6 extends MinecraftJoules5 implements IBatteryProvider, IBatteryObject
{

	@Override
	@Method(iname = "MJ6")
	public String kind()
	{
		return null;
	}

	@Override
	@Method(iname = "MJ6")
	public double getEnergyRequested()
	{
		return this.getExternalPowerDemand( PowerUnits.MJ, Double.MAX_VALUE );
	}

	@Override
	@Method(iname = "MJ6")
	public double addEnergy(double amount)
	{
		double demand = this.getExternalPowerDemand( PowerUnits.MJ, Double.MAX_VALUE );
		if ( amount > demand )
			amount = demand;

		double overflow = this.injectExternalPower( PowerUnits.MJ, amount );
		return amount - overflow;
	}

	@Override
	@Method(iname = "MJ6")
	public double addEnergy(double amount, boolean ignoreCycleLimit)
	{
		double overflow = this.injectExternalPower( PowerUnits.MJ, amount );
		return amount - overflow;
	}

	@Override
	@Method(iname = "MJ6")
	public double getEnergyStored()
	{
		return PowerUnits.AE.convertTo( PowerUnits.MJ, this.internalCurrentPower );
	}

	@Override
	@Method(iname = "MJ6")
	public void setEnergyStored(double mj)
	{
		this.internalCurrentPower = PowerUnits.MJ.convertTo( PowerUnits.AE, mj );
	}

	@Override
	@Method(iname = "MJ6")
	public double maxCapacity()
	{
		return PowerUnits.AE.convertTo( PowerUnits.MJ, this.internalMaxPower );
	}

	@Override
	@Method(iname = "MJ6")
	public double minimumConsumption()
	{
		return 0.1;
	}

	@Override
	@Method(iname = "MJ6")
	public double maxReceivedPerCycle()
	{
		return 999999.0;
	}

	@Override
	@Method(iname = "MJ6")
	public IBatteryObject reconfigure(double maxCapacity, double maxReceivedPerCycle, double minimumConsumption)
	{
		return this.getMjBattery( "" );
	}

	@Override
	@Method(iname = "MJ6")
	public IBatteryObject getMjBattery(String kind)
	{
		return this;
	}

}
