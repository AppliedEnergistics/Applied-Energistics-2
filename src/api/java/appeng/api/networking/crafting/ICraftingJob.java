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


import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;


public interface ICraftingJob
{

	/**
	 * @return if this job is a simulation, simulations cannot be submitted and only represent 1 possible future
	 * crafting job with fake items.
	 */
	boolean isSimulation();

	/**
	 * @return total number of bytes to process this job.
	 */
	long getByteTotal();

	/**
	 * Populates the plan list with stack size, and requestable values that represent the stored, and crafting job
	 * contents respectively.
	 *
	 * @param plan plan
	 */
	void populatePlan( IItemList<IAEItemStack> plan );

	/**
	 * @return the final output of the job.
	 */
	IAEItemStack getOutput();
}
