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


import net.minecraft.item.ItemStack;

import appeng.api.IAppEngApi;


/**
 * Storage Cell Registry, used for specially implemented cells, if you just want to make a item act like a cell, or new
 * cell with different bytes, then you should probably consider IStorageCell instead its considerably simpler.
 *
 * Do not Implement, obtained via {@link IAppEngApi}.getCellRegistry()
 */
public interface ICellRegistry
{

	/**
	 * Register a new handler.
	 *
	 * @param handler cell handler
	 */
	void addCellHandler( ICellHandler handler );

	/**
	 * return true, if you can get a InventoryHandler for the item passed.
	 *
	 * @param is to be checked item
	 *
	 * @return true if the provided item, can be handled by a handler in AE, ( AE May choose to skip this and just get
	 * the handler instead. )
	 */
	boolean isCellHandled( ItemStack is );

	/**
	 * get the handler, for the requested type.
	 *
	 * @param is to be checked item
	 *
	 * @return the handler registered for this item type.
	 */
	ICellHandler getHandler( ItemStack is );

	/**
	 * returns an IMEInventoryHandler for the provided item.
	 *
	 * @param is   item with inventory handler
	 * @param host can be null, or the hosting tile / part.
	 * @param chan the storage channel to request the handler for.
	 *
	 * @return new IMEInventoryHandler, or null if there isn't one.
	 */
	IMEInventoryHandler getCellInventory( ItemStack is, ISaveProvider host, StorageChannel chan );
}