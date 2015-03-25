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


import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.networking.security.BaseActionSource;


/**
 * A Registration Record for {@link IExternalStorageRegistry}
 */
public interface IExternalStorageHandler
{

	/**
	 * if this can handle the provided inventory, return true. ( Generally skipped by AE, and it just calls getInventory
	 * )
	 *
	 * @param te    to be handled tile entity
	 * @param mySrc source
	 *
	 * @return true, if it can get a handler via getInventory
	 */
	boolean canHandle( TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource mySrc );

	/**
	 * if this can handle the given inventory, return the a IMEInventory implementing class for it, if not return null
	 *
	 * please note that if your inventory changes and requires polling, you must use an {@link IMEMonitor} instead of an
	 * {@link IMEInventory} failure to do so will result in invalid item counts and reporting of the inventory.
	 *
	 * @param te      to be handled tile entity
	 * @param d       direction
	 * @param channel channel
	 * @param src     source
	 *
	 * @return The Handler for the inventory
	 */
	IMEInventory getInventory( TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource src );
}