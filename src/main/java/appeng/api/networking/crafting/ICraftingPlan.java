/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
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

import java.util.Map;

import appeng.api.crafting.IPatternDetails;
import appeng.api.storage.GenericStack;
import appeng.api.storage.data.KeyCounter;

/**
 * Result of a {@linkplain ICraftingService#beginCraftingCalculation crafting job calculation}. Do not edit any of the
 * map/lists, they are exposed directly!
 */
public interface ICraftingPlan {
    /**
     * Final output of the job.
     */
    GenericStack finalOutput();

    /**
     * Total bytes used by the job.
     */
    long bytes();

    /**
     * True if some things were missing and this is just a simulation.
     */
    boolean simulation();

    /**
     * True there were multiple paths in the crafting tree, i.e. at least one item had multiple patterns that could
     * produce it.
     */
    boolean multiplePaths();

    /**
     * List of items that were used. (They would need to be extracted to start the job).
     */
    KeyCounter usedItems();

    /**
     * List of items that need to be emitted for this job.
     */
    KeyCounter emittedItems();

    /**
     * List of missing items if this is a simulation.
     */
    KeyCounter missingItems();

    /**
     * Map of each pattern to the number of times it needs to be crafted. Can be used to retrieve the crafted items:
     * outputs * times.
     */
    Map<IPatternDetails, Long> patternTimes();
}
