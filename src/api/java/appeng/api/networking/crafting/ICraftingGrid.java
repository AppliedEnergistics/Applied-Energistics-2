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


import java.util.concurrent.Future;

import net.minecraft.world.World;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEItemStack;


public interface ICraftingGrid extends IGridCache
{

	/**
	 * @param whatToCraft requested craft
	 * @param world       crafting world
	 * @param slot        slot index
	 * @param details     pattern details
	 *
	 * @return a collection of crafting patterns for the item in question.
	 */
	ImmutableCollection<ICraftingPatternDetails> getCraftingFor( IAEItemStack whatToCraft, ICraftingPatternDetails details, int slot, World world );

	/**
	 * Begin calculating a crafting job.
	 *
	 * @param world     crafting world
	 * @param grid      network
	 * @param actionSrc source
	 * @param craftWhat result
	 * @param callback  callback
	 *                  -- optional
	 *
	 * @return a future which will at an undetermined point in the future get you the {@link ICraftingJob} do not wait
	 * on this, your be waiting forever.
	 */
	Future<ICraftingJob> beginCraftingJob( World world, IGrid grid, BaseActionSource actionSrc, IAEItemStack craftWhat, ICraftingCallback callback );

	/**
	 * Submit the job to the Crafting system for processing.
	 *
	 * @param job               - the crafting job from beginCraftingJob
	 * @param requestingMachine - a machine if its being requested via automation, may be null.
	 * @param target            - can be null
	 * @param prioritizePower   - if cpu is null, this determine if the system should prioritize power, or if it should find the lower
	 *                          end cpus, automatic processes generally should pick lower end cpus.
	 * @param src               - the action source to use when starting the job, this will be used for extracting items, should
	 *                          usually be the same as the one provided to beginCraftingJob.
	 *
	 * @return null ( if failed ) or an {@link ICraftingLink} other wise, if you send requestingMachine you need to
	 * properly keep track of this and handle the nbt saving and loading of the object as well as the
	 * {@link ICraftingRequester} methods. if you send null, this object should be discarded after verifying the
	 * return state.
	 */
	ICraftingLink submitJob( ICraftingJob job, ICraftingRequester requestingMachine, ICraftingCPU target, boolean prioritizePower, BaseActionSource src );

	/**
	 * @return list of all the crafting cpus on the grid
	 */
	ImmutableSet<ICraftingCPU> getCpus();

	/**
	 * @param what to be requested item
	 *
	 * @return true if the item can be requested via a crafting emitter.
	 */
	boolean canEmitFor( IAEItemStack what );

	/**
	 * is this item being crafted?
	 *
	 * @param aeStackInSlot item being crafted
	 *
	 * @return true if it is being crafting
	 */
	boolean isRequesting( IAEItemStack aeStackInSlot );
}
