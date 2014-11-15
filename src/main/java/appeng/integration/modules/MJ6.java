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

package appeng.integration.modules;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.integration.BaseModule;
import appeng.integration.abstraction.IMJ6;
import appeng.transformer.annotations.integration.Method;
import buildcraft.api.mj.IBatteryObject;
import buildcraft.api.mj.IBatteryProvider;
import buildcraft.api.mj.ISidedBatteryProvider;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;

public class MJ6 extends BaseModule implements IMJ6
{

	public static MJ6 instance;

	public MJ6() {
		TestClass( IBatteryObject.class );
		TestClass( IBatteryProvider.class );
		TestClass( ISidedBatteryProvider.class );
	}

	@Override
	public void Init() throws Throwable
	{
	}

	@Override
	public void PostInit()
	{
	}

	@Override
	@Method(iname = "MJ5")
	public IBatteryObject provider(final TileEntity te, final ForgeDirection side)
	{
		if ( te instanceof IPowerReceptor )
		{
			final IPowerReceptor receptor = (IPowerReceptor) te;
			final PowerReceiver ph = receptor.getPowerReceiver( side );

			if ( ph == null )
				return null;

			return new IBatteryObject() {

				@Override
				public void setEnergyStored(double mj)
				{

				}

				@Override
				public IBatteryObject reconfigure(double maxCapacity, double maxReceivedPerCycle, double minimumConsumption)
				{
					return this;
				}

				@Override
				public double minimumConsumption()
				{
					return ph.getMinEnergyReceived();
				}

				@Override
				public double maxReceivedPerCycle()
				{
					return ph.getMaxEnergyReceived();
				}

				@Override
				public double maxCapacity()
				{
					return ph.getMaxEnergyStored();
				}

				@Override
				public String kind()
				{
					return MjAPI.DEFAULT_POWER_FRAMEWORK;
				}

				@Override
				public double getEnergyStored()
				{
					return ph.getEnergyStored();
				}

				@Override
				public double getEnergyRequested()
				{
					return ph.getMaxEnergyStored() - ph.getEnergyStored();
				}

				@Override
				public double addEnergy(double mj, boolean ignoreCycleLimit)
				{
					return ph.receiveEnergy( Type.PIPE, mj, side );
				}

				@Override
				public double addEnergy(double mj)
				{
					return ph.receiveEnergy( Type.PIPE, mj, side );
				}
			};
		}
		return null;
	}
}
