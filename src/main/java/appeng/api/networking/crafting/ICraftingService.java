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

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.AEKeyFilter;

public interface ICraftingService extends IGridService {

    /**
     * @param whatToCraft requested craft
     *
     * @return an unmodifiable collection of crafting patterns for the item in question.
     */
    Collection<IPatternDetails> getCraftingFor(AEKey whatToCraft);

    /**
     * @return true if the grid knows how to craft the given key
     */
    default boolean isCraftable(AEKey whatToCraft) {
        return !getCraftingFor(whatToCraft).isEmpty();
    }

    /**
     * Refreshes the crafting mounts provided by a {@link IGridNode node} through its {@link ICraftingProvider}.
     *
     * @throws IllegalArgumentException If the given node is not part of this grid, or did not provide
     *                                  {@link ICraftingProvider}.
     */
    void refreshNodeCraftingProvider(IGridNode node);

    /**
     * Important: Never mutate the passed or returned stacks.
     *
     * @return another fuzzy equals stack that can be crafted and matches the filter, or null if none exists
     */
    @Nullable
    AEKey getFuzzyCraftable(AEKey whatToCraft, AEKeyFilter filter);

    /**
     * Begin calculating a crafting job.
     *
     * @param level        crafting level
     * @param simRequester source
     * @param craftWhat    result
     *
     * @return a future which will at an undetermined point in the future get you the {@link ICraftingPlan} do not wait
     *         on this, your be waiting forever.
     */
    Future<ICraftingPlan> beginCraftingCalculation(Level level, ICraftingSimulationRequester simRequester,
            AEKey craftWhat, long amount);

    /**
     * Submit the job to the Crafting system for processing.
     *
     * @param job               - the crafting job from beginCraftingJob
     * @param requestingMachine - a machine if its being requested via automation, may be null.
     * @param target            - can be null
     * @param prioritizePower   - if cpu is null, this determine if the system should prioritize power, or if it should
     *                          find the lower end cpus, automatic processes generally should pick lower end cpus.
     * @param src               - the action source to use when starting the job, this will be used for extracting
     *                          items, should usually be the same as the one provided to beginCraftingJob.
     *
     * @return null ( if failed ) or an {@link ICraftingLink} other wise, if you send requestingMachine you need to
     *         properly keep track of this and handle the nbt saving and loading of the object as well as the
     *         {@link ICraftingRequester} methods. if you send null, this object should be discarded after verifying the
     *         return state.
     */
    ICraftingLink submitJob(ICraftingPlan job, ICraftingRequester requestingMachine, ICraftingCPU target,
            boolean prioritizePower, IActionSource src);

    /**
     * @return list of all the crafting cpus on the grid
     */
    ImmutableSet<ICraftingCPU> getCpus();

    /**
     * @param what to be requested item
     *
     * @return true if the item can be requested via a crafting emitter.
     */
    boolean canEmitFor(AEKey what);

    /**
     * Get the set of things that can be crafted for a given storage channel.
     */
    Set<AEKey> getCraftables(AEKeyFilter filter);

    /**
     * is this item being crafted?
     *
     * @param what item being crafted
     *
     * @return true if it is being crafting
     */
    boolean isRequesting(AEKey what);

    /**
     * The total amount being requested across all crafting cpus of a grid.
     *
     * @param what item being requested, ignores stacksize
     *
     * @return The total amount being requested.
     */
    long requesting(AEKey what);
}
