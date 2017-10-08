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


/**
 * A registry to allow mapping {@link Item}s to a specific charge rate when being placed inside a charger.
 * 
 * The registry is used in favor of an additional method for {@link IAEItemPowerStorage} with a fixed value per item.
 * This allows more flexibility for other charger like machines to choose their own values when needed.
 * 
 * There is no guarantee that this is charged per tick, it only represents the value per operation.
 * By default this is one charging operation every 10 ticks in case of an AE2 charger.
 * 
 * @author yueh
 * @version rv5
 * @since rv5
 */
public interface IChargerRegistry
{

	/**
	 * Fetch a charge rate for a specific item.
	 * 
	 * The specific item does not need to have a mapping registered at all.
	 * In this case it will use a default value of 160 AE.
	 * 
	 * @param item A {@link Item} implementing {@link IAEItemPowerStorage}.
	 * @return custom rate or default of 160
	 */
	@Nonnegative
	double getChargeRate( @Nonnull Item item );

	/**
	 * Register a custom charge rate for a specific item.
	 * 
	 * Capped at 16000 to avoid extracting too much energy from a network for each operation.
	 * This is done silently without any feedback or exception.
	 * Further the cap is not fixed, it can change at any time in the future should power issues arise.
	 * 
	 * @param item A {@link Item} implementing {@link IAEItemPowerStorage}.
	 * @param chargeRate the custom rate, must be > 0, capped to 16000d
	 */
	void addChargeRate( @Nonnull Item item, @Nonnegative double chargeRate );

	/**
	 * Remove the custom rate for a specific item.
	 * 
	 * It will revert to the default value afterwards.
	 * 
	 * @param item A {@link Item} implementing {@link IAEItemPowerStorage}.
	 */
	void removeChargeRate( @Nonnull Item item );

}
