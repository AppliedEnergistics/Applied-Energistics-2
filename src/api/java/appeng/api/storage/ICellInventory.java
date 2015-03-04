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

package appeng.api.storage;


import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;


public interface ICellInventory extends IMEInventory<IAEItemStack>
{

	/**
	 * @return the item stack of this storage cell.
	 */
	ItemStack getItemStack();

	/**
	 * @return idle cost for this Storage Cell
	 */
	double getIdleDrain();

	/**
	 * @return fuzzy setting
	 */
	FuzzyMode getFuzzyMode();

	/**
	 * @return access configured list
	 */
	IInventory getConfigInventory();

	/**
	 * @return access installed upgrades.
	 */
	IInventory getUpgradesInventory();

	/**
	 * @return How many bytes are used for each type?
	 */
	int getBytesPerType();

	/**
	 * @return true if a new item type can be added.
	 */
	boolean canHoldNewItem();

	/**
	 * @return total byte storage.
	 */
	long getTotalBytes();

	/**
	 * @return how many bytes are free.
	 */
	long getFreeBytes();

	/**
	 * @return how many bytes are in use.
	 */
	long getUsedBytes();

	/**
	 * @return max number of types.
	 */
	long getTotalItemTypes();

	/**
	 * @return how many items are stored.
	 */
	long getStoredItemCount();

	/**
	 * @return how many items types are currently stored.
	 */
	long getStoredItemTypes();

	/**
	 * @return how many item types remain.
	 */
	long getRemainingItemTypes();

	/**
	 * @return how many more items can be stored.
	 */
	long getRemainingItemCount();

	/**
	 * @return how many items can be added without consuming another byte.
	 */
	int getUnusedItemCount();

	/**
	 * @return the status number for this drive.
	 */
	int getStatusForCell();
}
