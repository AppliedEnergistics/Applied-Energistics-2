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


import java.util.Iterator;


/**
 * Represents a list of items in AE.
 *
 * Don't Implement.
 *
 * Construct with Util.createItemList()
 */
public interface IItemList<StackType extends IAEStack> extends IItemContainer<StackType>, Iterable<StackType>
{

	/**
	 * add a stack to the list stackSize is used to add to stackSize, this will merge the stack with an item already in
	 * the list if found.
	 *
	 * @param option stacktype option
	 */
	void addStorage( StackType option ); // adds a stack as stored

	/**
	 * add a stack to the list as craftable, this will merge the stack with an item already in the list if found.
	 *
	 * @param option stacktype option
	 */
	void addCrafting( StackType option );

	/**
	 * add a stack to the list, stack size is used to add to requestable, this will merge the stack with an item already
	 * in the list if found.
	 *
	 * @param option stacktype option
	 */
	void addRequestable( StackType option ); // adds a stack as requestable

	/**
	 * @return the first item in the list
	 */
	StackType getFirstItem();

	/**
	 * @return the number of items in the list
	 */
	int size();

	/**
	 * allows you to iterate the list.
	 */
	@Override
	Iterator<StackType> iterator();

	/**
	 * resets stack sizes to 0.
	 */
	void resetStatus();
}