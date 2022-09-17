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


import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;
import appeng.api.networking.energy.IAEPowerStorage;


public interface IExternalPowerSink extends IAEPowerStorage {

    /**
     * Inject power into the network
     *
     * @param externalUnit The {@link PowerUnits} used by the input
     * @param amount       The amount offered to the sink.
     * @param mode         Modulate or simulate the operation.
     * @return The unused amount, which could not be inserted into the sink.
     */
    double injectExternalPower(PowerUnits externalUnit, double amount, Actionable mode);

    /**
     * @param externalUnit     The {@link PowerUnits} used by the input
     * @param maxPowerRequired Limit the demand to this upper bound.
     * @return The amount of power demanded by the sink.
     */
    double getExternalPowerDemand(PowerUnits externalUnit, double maxPowerRequired);

}
