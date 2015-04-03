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


import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;


/**
 * Used to access information about AE's various power accepting blocks for monitoring purposes.
 */
public interface IAEPowerStorage extends IEnergySource
{

	/**
	 * Inject amt, power into the device, it will store what it can, and return the amount unable to be stored.
	 *
	 * @param amt  to be injected amount
	 * @param mode action mode
	 *
	 * @return amount of power which was unable to be stored
	 */
	double injectAEPower( double amt, Actionable mode );

	/**
	 * @return the current maximum power ( this can change :P )
	 */
	double getAEMaxPower();

	/**
	 * @return the current AE Power Level, this may exceed getMEMaxPower()
	 */
	double getAECurrentPower();

	/**
	 * Checked on network reset to see if your block can be used as a public power storage ( use getPowerFlow to control
	 * the behavior )
	 *
	 * @return true if it can be used as a public power storage
	 */
	boolean isAEPublicPowerStorage();

	/**
	 * Control the power flow by telling what the network can do, either add? or subtract? or both!
	 *
	 * @return access restriction what the network can do
	 */
	AccessRestriction getPowerFlow();
}