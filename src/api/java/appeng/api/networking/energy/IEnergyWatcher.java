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

package appeng.api.networking.energy;


/**
 * DO NOT IMPLEMENT.
 * 
 * Will be injected when adding an {@link IEnergyWatcherHost} to a grid.
 */
public interface IEnergyWatcher
{
	/**
	 * Add a specific threshold to watch.
	 * 
	 * Supports multiple values, duplicate ones will not be added.
	 * 
	 * @param amount
	 * @return true, if successfully added.
	 */
	boolean add( double amount );

	/**
	 * Remove a specific threshold from the watcher.
	 * 
	 * @param amount
	 * @return true, if successfully removed.
	 */
	boolean remove( double amount );

	/**
	 * Removes all thresholds and resets the watcher to a clean state.
	 */
	void reset();

}
