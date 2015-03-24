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


import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridNode;


/**
 * The network tick manager.
 */
public interface ITickManager extends IGridCache
{

	/**
	 * immediately sets the node to tick, only valid if your node is marked as "Alertable" in its TickingRequest
	 *
	 * Sleeping Devices Still Alertable, when your tile is alerted its new status is determined by the result of its
	 * tick.
	 *
	 * @param node gridnode
	 */
	boolean alertDevice( IGridNode node );

	/**
	 * disables ticking for your device.
	 *
	 * @param node gridnode
	 *
	 * @return if the call was successful.
	 */
	boolean sleepDevice( IGridNode node );

	/**
	 * enables ticking for your device, undoes a sleepDevice call.
	 *
	 * @param node gridnode
	 *
	 * @return if the call was successful.
	 */
	boolean wakeDevice( IGridNode node );
}
