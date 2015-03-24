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

package appeng.api.networking;


/**
 * Allows you to create a network wise service, AE2 uses these for providing
 * item, spatial, and tunnel services.
 *
 * Any Class that implements this, should have a public default constructor that
 * takes a single argument of type IGrid.
 */
public interface IGridCache
{

	/**
	 * Called each tick for the network, allows you to have active network wide
	 * behaviors.
	 */
	void onUpdateTick();

	/**
	 * inform your cache, that a machine was removed from the grid.
	 *
	 * Important: Do not trust the grids state in this method, interact only
	 * with the node you are passed, if you need to manage other grid
	 * information, do it on the next updateTick.
	 *
	 * @param gridNode removed from that grid
	 * @param machine  to be removed machine
	 */
	void removeNode( IGridNode gridNode, IGridHost machine );

	/**
	 * informs you cache that a machine was added to the grid.
	 *
	 * Important: Do not trust the grids state in this method, interact only
	 * with the node you are passed, if you need to manage other grid
	 * information, do it on the next updateTick.
	 *
	 * @param gridNode added to grid node
	 * @param machine  to be added machine
	 */
	void addNode( IGridNode gridNode, IGridHost machine );

	/**
	 * Called when a grid splits into two grids, AE will call a split as it
	 * Iteratively processes changes. The destination should receive half, and
	 * the current cache should receive half.
	 *
	 * @param destinationStorage storage which receives half of old grid
	 */
	void onSplit( IGridStorage destinationStorage );

	/**
	 * Called when two grids merge into one, AE will call a join as it
	 * Iteratively processes changes. Use this method to incorporate all the
	 * data from the source into your cache.
	 *
	 * @param sourceStorage old storage
	 */
	void onJoin( IGridStorage sourceStorage );

	/**
	 * Called when saving changes,
	 *
	 * @param destinationStorage storage
	 */
	void populateGridStorage( IGridStorage destinationStorage );
}
