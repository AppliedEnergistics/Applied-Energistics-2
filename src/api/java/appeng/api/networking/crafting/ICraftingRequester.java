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

package appeng.api.networking.crafting;


import com.google.common.collect.ImmutableSet;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.data.IAEItemStack;


public interface ICraftingRequester extends IActionHost
{

	/**
	 * called when the host is added to the grid, and should return all crafting links it poses so they can be connected
	 * with the cpu that hosts the job.
	 *
	 * @return set of jobs, or an empty list.
	 */
	ImmutableSet<ICraftingLink> getRequestedJobs();

	/**
	 * items are injected into the requester as they are completed, any items that cannot be taken, or are unwanted can
	 * be returned.
	 *
	 * @param items item
	 * @param mode  action mode
	 *
	 * @return unwanted item
	 */
	IAEItemStack injectCraftedItems( ICraftingLink link, IAEItemStack items, Actionable mode );

	/**
	 * called when the job changes from in progress, to either complete, or canceled.
	 *
	 * after this call the crafting link is "dead" and should be discarded.
	 */
	void jobStateChange( ICraftingLink link );
}
