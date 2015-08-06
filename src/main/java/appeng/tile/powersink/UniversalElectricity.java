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


/*
 * import net.minecraftforge.common.util.ForgeDirection;
 * import universalelectricity.core.block.IElectrical;
 * import universalelectricity.core.electricity.ElectricityPack;
 * import appeng.api.config.PowerUnits;
 * public abstract class UniversalElectricity extends ThermalExpansion implements IElectrical
 * {
 * @Override
 * public final boolean canConnect(ForgeDirection direction)
 * {
 * return internalCanAcceptPower && getPowerSides().contains( direction );
 * }
 * @Override
 * public final float receiveElectricity(ForgeDirection from, ElectricityPack receive, boolean doReceive)
 * {
 * float accepted = 0;
 * double receivedPower = receive.getWatts();
 * if ( doReceive )
 * {
 * accepted = (float) (receivedPower - injectExternalPower( PowerUnits.KJ, receivedPower ));
 * }
 * else
 * {
 * double whatIWant = getExternalPowerDemand( PowerUnits.KJ );
 * if ( whatIWant > receivedPower )
 * accepted = (float) receivedPower;
 * else
 * accepted = (float) whatIWant;
 * }
 * return accepted;
 * }
 * @Override
 * public final float getRequest(ForgeDirection direction)
 * {
 * return (float) getExternalPowerDemand( PowerUnits.KJ );
 * }
 * @Override
 * public final float getVoltage()
 * {
 * return 120;
 * }
 * @Override
 * public final ElectricityPack provideElectricity(ForgeDirection from, ElectricityPack request, boolean doProvide)
 * {
 * return null; // cannot be dis-charged
 * }
 * @Override
 * public final float getProvide(ForgeDirection direction)
 * {
 * return 0;
 * }
 * }
 */