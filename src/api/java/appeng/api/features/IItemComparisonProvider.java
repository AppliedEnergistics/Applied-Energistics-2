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

package appeng.api.features;


import net.minecraft.item.ItemStack;


/**
 * Provider for special comparisons. when an item is encountered AE Will request
 * if the comparison function handles the item, by trying to request a
 * IItemComparison class.
 */
public interface IItemComparisonProvider
{

	/**
	 * should return a new IItemComparison, or return null if it doesn't handle
	 * the supplied item.
	 *
	 * @param is item
	 *
	 * @return IItemComparison, or null
	 */
	IItemComparison getComparison( ItemStack is );

	/**
	 * Simple test for support ( AE generally skips this and calls the above function. )
	 *
	 * @param stack item
	 *
	 * @return true, if getComparison will return a valid IItemComparison Object
	 */
	boolean canHandle( ItemStack stack );
}