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

import appeng.api.crafting.IPatternDetails;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;

/**
 * A place to send Items for crafting purposes, registered by a {@link ICraftingProvider} with
 * {@link ICraftingProviderHelper#addCraftingOption}.
 */
public interface ICraftingMedium {

    /**
     * instruct a medium to create the item represented by the pattern+details, the items on the table, and where if
     * possible the output should be directed.
     *
     * @param patternDetails details
     * @param inputHolder    the requested stacks, for each input slot of the pattern
     *
     * @return if the pattern was successfully pushed.
     */
    boolean pushPattern(IPatternDetails patternDetails, KeyCounter<AEKey>[] inputHolder);

    /**
     * @return if this is false, the crafting engine will refuse to send new jobs to this medium.
     */
    boolean isBusy();
}
