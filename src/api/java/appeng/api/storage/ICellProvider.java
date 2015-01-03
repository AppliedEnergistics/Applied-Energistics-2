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


import java.util.List;


/**
 * Allows you to provide cells via non IGridHosts directly to the storage system, drives, and similar features should go
 * though {@link ICellContainer} and be automatically handled by the storage system.
 */
public interface ICellProvider
{

	/**
	 * Inventory of the tile for use with ME, should always return an valid list, never NULL.
	 *
	 * You must return the correct Handler for the correct channel, if your handler returns a IAEItemStack handler, for
	 * a Fluid Channel stuffs going to explode, same with the reverse.
	 *
	 * @return a valid list of handlers, NEVER NULL
	 */
	List<IMEInventoryHandler> getCellArray( StorageChannel channel );

	/**
	 * the storage's priority.
	 *
	 * Positive and negative are supported
	 */
	int getPriority();
}
