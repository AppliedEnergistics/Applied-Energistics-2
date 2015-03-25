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

package appeng.api.networking.ticking;


import appeng.api.networking.IGridNode;


/**
 * Implement on IGridHosts which want to use AE's Network Ticking Feature.
 */
public interface IGridTickable
{

	/**
	 * Important note regarding IGridTickables with more then one one node,
	 *
	 * If your IGridHost hosts multiple nodes, it may be on multiple grids, or
	 * its node may be present on the same grid multiple times, this is as
	 * designed, however if you choose to use the grid to tick these Hosts you
	 * must be aware that they they should probably pick a single node to tick
	 * for, and not tick for each node.
	 *
	 */

	/**
	 * You can return null, if you wish to tick using MC's ticking mechanism, or
	 * you can return a valid TickingRequest to tell AE a guide for which type
	 * of responsiveness your device wants.
	 *
	 * this will be called for your tile any time your tile changes grids, this
	 * can happen at any time, so if your using the sleep feature you may wish
	 * to preserve your sleep, in the result of this method. or you can simply
	 * reset it.
	 *
	 * @return null or a valid new TickingRequest
	 */
	TickingRequest getTickingRequest( IGridNode node );

	/**
	 * AE lets you adjust your tick rate based on the results of your tick, if
	 * your block as accomplished work you may wish to increase the ticking
	 * speed, if your block is idle you may wish to slow it down.
	 *
	 * Its up to you.
	 *
	 * Note: this is never called if you return null from getTickingRequest.
	 *
	 * @param TicksSinceLastCall the number of world ticks that were skipped since your last
	 *                           tick, you can use this to adjust speed of processing or adjust
	 *                           your tick rate.
	 *
	 * @return tick rate adjustment.
	 */
	TickRateModulation tickingRequest( IGridNode node, int TicksSinceLastCall );
}
