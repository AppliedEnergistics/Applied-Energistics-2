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

package appeng.api.storage.data;


import java.util.Collection;

import appeng.api.config.FuzzyMode;


/**
 * Represents a list of items in AE.
 *
 * Don't Implement.
 *
 * Construct with Util.createItemList()
 */
public interface IItemContainer<StackType extends IAEStack>
{

	/**
	 * add a stack to the list, this will merge the stack with an item already in the list if found.
	 *
	 * @param option added stack
	 */
	void add( StackType option ); // adds stack as is

	/**
	 * @param i compared item
	 *
	 * @return a stack equivalent to the stack passed in, but with the correct stack size information, or null if its
	 * not present
	 */
	StackType findPrecise( StackType i );

	/**
	 * @param input compared item
	 *
	 * @return a list of relevant fuzzy matched stacks
	 */
	Collection<StackType> findFuzzy( StackType input, FuzzyMode fuzzy );

	/**
	 * @return true if there are no items in the list
	 */
	boolean isEmpty();
}