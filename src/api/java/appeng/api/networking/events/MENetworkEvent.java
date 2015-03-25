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

package appeng.api.networking.events;


import appeng.api.networking.IGrid;


/**
 * Part of AE's Event Bus.
 *
 * Posted via {@link IGrid}.postEvent or {@link IGrid}.postEventTo
 */
public class MENetworkEvent
{

	private int visited = 0;
	private boolean canceled = false;

	/**
	 * Call to prevent AE from posting the event to any further objects.
	 */
	public void cancel()
	{
		this.canceled = true;
	}

	/**
	 * called by AE after each object is called to cancel any future calls.
	 *
	 * @return true to cancel future calls
	 */
	public boolean isCanceled()
	{
		return this.canceled;
	}

	/**
	 * the number of objects that were visited by the event.
	 *
	 * @return number of visitors
	 */
	public int getVisitedObjects()
	{
		return this.visited;
	}

	/**
	 * Called by AE after iterating the event subscribers.
	 *
	 * @param v current number of visitors
	 */
	public void setVisitedObjects( int v )
	{
		this.visited = v;
	}
}
