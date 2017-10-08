/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 AlgorithmX2
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

package appeng.api.features;


import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import net.minecraft.item.Item;

import appeng.api.implementations.items.IAEItemPowerStorage;


public interface IChargerRegistry
{

	/**
	 * Fetch a charge rate for a specific item.
	 * 
	 * If no custom exists, it will return a default of 160.
	 * 
	 * 
	 * @param item A {@link Item} implementing {@link IAEItemPowerStorage}.
	 * @return custom rate or 160
	 */
	@Nonnegative
	double getChargeRate( @Nonnull Item item );

	/**
	 * Register a custom charge rate for a specific item.
	 * 
	 * Capped at 16000 to avoid extracting too much energy from a network for each operation.
	 * 
	 * @param item A {@link Item} implementing {@link IAEItemPowerStorage}.
	 * @param chargeRate the custom rate, must be > 0, capped to 16000d
	 */
	void addChargeRate( @Nonnull Item item, @Nonnull double chargeRate );

	/**
	 * Remove the custom rate for a specific item.
	 * 
	 * It will still use the default value after removing it.
	 * 
	 * @param item A {@link Item} implementing {@link IAEItemPowerStorage}.
	 */
	void removeChargeRate( @Nonnull Item item );

}
