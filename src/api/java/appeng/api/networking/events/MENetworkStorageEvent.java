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


import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;


/**
 * posted by the network when the networks Storage Changes, you can use the currentItems list to check levels, and
 * update status.
 *
 * this is the least useful method of getting info about changes in the network.
 *
 * Do not modify the list or its contents in anyway.
 */
public class MENetworkStorageEvent extends MENetworkEvent
{

	public final IMEMonitor monitor;
	public final StorageChannel channel;

	public MENetworkStorageEvent( IMEMonitor o, StorageChannel chan )
	{
		this.monitor = o;
		this.channel = chan;
	}
}
