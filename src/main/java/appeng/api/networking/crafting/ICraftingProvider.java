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

import java.util.List;
import java.util.Set;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNodeService;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;

/**
 * Allows a node to provide crafting patterns and emitable items to the network.
 */
public interface ICraftingProvider extends IGridNodeService {
    /**
     * Return the patterns offered by this provider. {@link #pushPattern} will be called if they need to be crafted.
     */
    List<IPatternDetails> getAvailablePatterns();

    /**
     * Return the priority for the patterns offered by this provider. The crafting calculation will prioritize patterns
     * with the highest priority.
     */
    default int getPatternPriority() {
        return 0;
    }

    /**
     * Instruct a provider to craft one of the patterns.
     *
     * @param patternDetails details
     * @param inputHolder    the requested stacks, for each input slot of the pattern
     *
     * @return if the pattern was successfully pushed.
     */
    boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder);

    /**
     * @return if this is true, the crafting engine will refuse to send patterns to this provider.
     */
    boolean isBusy();

    /**
     * Return the emitable items offered by this provider. They should be crafted and inserted into the network when
     * {@link ICraftingService#isRequesting} is true.
     */
    default Set<AEKey> getEmitableItems() {
        return Set.of();
    }

    /**
     * This convenience method can be used when the crafting options or emitable items have changed to request an update
     * of the crafting service's cache.This only works if the given managed grid node provides this service.
     */
    static void requestUpdate(IManagedGridNode managedNode) {
        var node = managedNode.getNode();
        if (node != null) {
            node.getGrid().getCraftingService().refreshNodeCraftingProvider(node);
        }
    }
}
