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

package appeng.api.implementations.tiles;


import net.minecraft.inventory.InventoryCrafting;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.networking.crafting.ICraftingPatternDetails;


public interface ICraftingMachine
{

	/**
	 * inserts a crafting plan, and the necessary items into the crafting machine.
	 *
	 * @param patternDetails    details of pattern
	 * @param table             crafting table
	 * @param ejectionDirection ejection direction
	 *
	 * @return if it was accepted, all or nothing.
	 */
	boolean pushPattern( ICraftingPatternDetails patternDetails, InventoryCrafting table, ForgeDirection ejectionDirection );

	/**
	 * check if the crafting machine is accepting pushes via pushPattern, if this is false, all calls to push will fail,
	 * you can try inserting into the inventory instead.
	 *
	 * @return true, if pushPattern can complete, if its false push will always be false.
	 */
	boolean acceptsPlans();
}
