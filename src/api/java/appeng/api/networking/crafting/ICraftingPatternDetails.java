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

package appeng.api.networking.crafting;


import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.storage.data.IAEItemStack;


/**
 * do not implement provided by {@link ICraftingPatternItem}
 *
 * caching this INSTANCE will increase performance of validation and checks.
 */
public interface ICraftingPatternDetails
{

	/**
	 * @return source item.
	 */
	ItemStack getPattern();

	/**
	 * @param slotIndex specific slot index
	 * @param itemStack item in slot
	 * @param world     crafting world
	 *
	 * @return if an item can be used in the specific slot for this pattern.
	 */
	boolean isValidItemForSlot( int slotIndex, ItemStack itemStack, World world );

	/**
	 * @return if this pattern is a crafting pattern ( work bench )
	 */
	boolean isCraftable();

	/**
	 * @return a list of the inputs, will include nulls.
	 */
	IAEItemStack[] getInputs();

	/**
	 * @return a list of the inputs, will be clean
	 */
	IAEItemStack[] getCondensedInputs();

	/**
	 * @return a list of the outputs, will be clean
	 */
	IAEItemStack[] getCondensedOutputs();

	/**
	 * @return a list of the outputs, will include nulls.
	 */
	IAEItemStack[] getOutputs();

	/**
	 * @return if this pattern is enabled to support substitutions.
	 */
	boolean canSubstitute();

	/**
	 * Allow using this INSTANCE of the pattern details to preform the crafting action with performance enhancements.
	 *
	 * @param craftingInv inventory
	 * @param world       crafting world
	 *
	 * @return the crafted ( work bench ) item.
	 */
	ItemStack getOutput( InventoryCrafting craftingInv, World world );

	/**
	 * Get the priority of this pattern
	 *
	 * @return the priority of this pattern
	 */
	int getPriority();

	/**
	 * Set the priority the of this pattern.
	 *
	 * @param priority priority of pattern
	 */
	void setPriority( int priority );
}
