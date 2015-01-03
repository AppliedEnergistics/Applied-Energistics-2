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


import appeng.api.networking.IGridHost;
import appeng.api.storage.ICellContainer;
import appeng.api.util.IOrientable;


public interface IChestOrDrive extends ICellContainer, IGridHost, IOrientable
{

	/**
	 * @return how many slots are available. Chest has 1, Drive has 10.
	 */
	int getCellCount();

	/**
	 * 0 - cell is missing.
	 *
	 * 1 - green,
	 *
	 * 2 - orange,
	 *
	 * 3 - red
	 *
	 * @param slot slot index
	 *
	 * @return status of the slot, one of the above indices.
	 */
	int getCellStatus( int slot );

	/**
	 * @return if the device is online you should check this before providing any other information.
	 */
	boolean isPowered();

	/**
	 * @param slot slot index
	 *
	 * @return is the cell currently blinking to show activity.
	 */
	boolean isCellBlinking( int slot );
}
