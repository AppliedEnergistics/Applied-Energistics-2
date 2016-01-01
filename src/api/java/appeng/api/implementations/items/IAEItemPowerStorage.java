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

package appeng.api.implementations.items;


import net.minecraft.item.ItemStack;

import appeng.api.config.AccessRestriction;
import appeng.api.networking.energy.IAEPowerStorage;


/**
 * Basically the same as {@link IAEPowerStorage}, but for items.
 */
public interface IAEItemPowerStorage
{

	/**
	 * Inject amt, power into the device, it will store what it can, and return
	 * the amount unable to be stored.
	 *
	 * @return amount unable to be stored
	 */
	double injectAEPower( ItemStack is, double amt );

	/**
	 * Attempt to extract power from the device, it will extract what it can and
	 * return it.
	 *
	 * @param amt to be extracted power from device
	 *
	 * @return what it could extract
	 */
	double extractAEPower( ItemStack is, double amt );

	/**
	 * @return the current maximum power ( this can change :P )
	 */
	double getAEMaxPower( ItemStack is );

	/**
	 * @return the current AE Power Level, this may exceed getMEMaxPower()
	 */
	double getAECurrentPower( ItemStack is );

	/**
	 * Control the power flow by telling what the network can do, either add? or
	 * subtract? or both!
	 *
	 * @return access restriction of network
	 */
	AccessRestriction getPowerFlow( ItemStack is );
}