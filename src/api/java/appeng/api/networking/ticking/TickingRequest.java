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


/**
 * Describes how your tiles ticking is executed.
 */
public class TickingRequest
{

	/**
	 * the minimum number of ticks that must pass between ticks.
	 *
	 * Valid Values are : 1+
	 *
	 * Suggested is 5-20
	 */
	public final int minTickRate;

	/**
	 * the maximum number of ticks that can pass between ticks, if this value is
	 * exceeded the tile must tick.
	 *
	 * Valid Values are 1+
	 *
	 * Suggested is 20-40
	 */
	public final int maxTickRate;

	/**
	 * Determines the current expected state of your node, if your node expects
	 * to be sleeping, then return true.
	 */
	public final boolean isSleeping;

	/**
	 * True only if you call {@link ITickManager}.alertDevice( IGridNode );
	 */
	public final boolean canBeAlerted;

	public TickingRequest( int min, int max, boolean sleep, boolean alertable )
	{
		this.minTickRate = min;
		this.maxTickRate = max;
		this.isSleeping = sleep;
		this.canBeAlerted = alertable;
	}
}
