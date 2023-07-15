/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.networking.energy;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridService;

/**
 * AE's Power system.
 */
public interface IEnergyService extends IGridService, IEnergySource {

    /**
     * Return the current calculated idle energy drain each tick, is used internally to drain power for each tick. It's
     * the sum of the {@linkplain #getChannelPowerUsage() idle channel power usage} and the idle usages of the machines
     * in the network.
     */
    double getIdlePowerUsage();

    /**
     * @return the current idle power usage of channels
     */
    double getChannelPowerUsage();

    /**
     * @return the average power drain over the past 10 ticks, includes idle usage during this time, and all use of
     *         extractPower.
     */
    double getAvgPowerUsage();

    /**
     * @return the average energy injected into the system per tick, for the last 10 ticks.
     */
    double getAvgPowerInjection();

    /**
     * AE maintains an idle draw of power separate from active power draw, it condenses this into a single operation
     * that determines the networks "powered state" if the network is considered off-line, your machines should not
     * function.
     * <p>
     * Nodes are notified via {@link appeng.api.networking.IGridNodeListener#onStateChanged} when this value changes.
     * Most machines can simply test the value when they are about to perform work, without listening to this event.
     *
     * @return if the network is powered or not.
     */
    boolean isNetworkPowered();

    /**
     * Inject power in the network. Note that each network has some power storage even if there are no energy cells.
     *
     * @param amt  power to inject into the network
     * @param mode should the action be simulated or performed?
     * @return the amount of power that the network could not accept
     */
    double injectPower(double amt, Actionable mode);

    /**
     * this is should be considered an estimate, and not relied upon for real calculations.
     *
     * @return estimated available power.
     */
    double getStoredPower();

    /**
     * this is should be considered an estimate, and not relied upon for real calculations.
     *
     * @return estimated available power.
     */
    double getMaxStoredPower();

    /**
     * Calculation will be capped at maxRequired, this improves performance by limiting the number of nodes needed to
     * calculate the demand.
     *
     * @return Amount of power required to charge the grid, in AE.
     */
    double getEnergyDemand(double maxRequired);
}
