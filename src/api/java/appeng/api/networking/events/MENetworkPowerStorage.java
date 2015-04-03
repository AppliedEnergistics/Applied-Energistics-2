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


import appeng.api.networking.energy.IAEPowerStorage;


/**
 * informs the network, that a {@link IAEPowerStorage} block that had either run,
 * out of power, or was full, is no longer in that state.
 *
 * failure to post this event when your {@link IAEPowerStorage} changes state will
 * result in your block not charging, or not-discharging.
 *
 * you do not need to send this event when your node is added / removed from the grid.
 */
public class MENetworkPowerStorage extends MENetworkEvent
{

	public final IAEPowerStorage storage;
	public final PowerEventType type;

	public MENetworkPowerStorage( IAEPowerStorage t, PowerEventType y )
	{
		this.storage = t;
		this.type = y;
	}

	public enum PowerEventType
	{
		/**
		 * informs the network this tile is ready to receive power again.
		 */
		REQUEST_POWER,

		/**
		 * informs the network this tile is ready to provide power again.
		 */
		PROVIDE_POWER
	}
}
