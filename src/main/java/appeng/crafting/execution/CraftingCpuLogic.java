/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.crafting.execution;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.MixedStackList;
import appeng.core.AELog;
import appeng.crafting.CraftingLink;
import appeng.crafting.CraftingWatcher;
import appeng.crafting.inv.ListCraftingInventory;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.service.CraftingService;

/**
 * Stores the crafting logic of a crafting CPU.
 */
public class CraftingCpuLogic {
    final CraftingCPUCluster cluster;
    /**
     * Current job.
     */
    private ExecutingCraftingJob job = null;
    /**
     * Inventory.
     */
    private final ListCraftingInventory inventory = new ListCraftingInventory() {
        @Override
        public void postChange(IAEStack template, long delta) {
            CraftingCpuLogic.this.postChange(template);
        }
    };
    /**
     * Used crafting operations over the last 3 ticks.
     */
    private final int[] usedOps = new int[3];
    private final Set<Consumer<IAEStack>> listeners = new HashSet<>();

    public CraftingCpuLogic(CraftingCPUCluster cluster) {
        this.cluster = cluster;
    }

    @Nullable
    public ICraftingLink trySubmitJob(IGrid grid, ICraftingPlan plan, IActionSource src,
            @Nullable ICraftingRequester requester) {
        // Already have a job.
        if (this.job != null)
            return null;
        // Check that the node is active.
        if (!cluster.isActive())
            return null;
        // Check bytes.
        if (cluster.getAvailableStorage() < plan.bytes())
            return null;

        if (!inventory.list.isEmpty())
            AELog.warn("Crafting CPU inventory is not empty yet a job was submitted.");

        // Try to extract required items.
        if (!CraftingCpuHelper.tryExtractInitialItems(plan, grid, inventory, src))
            return null;

        // Set CPU link and job.
        String craftId = this.generateCraftId(plan.finalOutput());
        var linkCpu = new CraftingLink(CraftingCpuHelper.generateLinkData(craftId, requester == null, false), cluster);
        this.job = new ExecutingCraftingJob(plan, this::postCraftingDifference, linkCpu);
        cluster.updateOutput(plan.finalOutput());
        cluster.markDirty();

        // TODO: post monitor difference?

        // Non-standalone jobs need another link for the requester, and both links need to be submitted to the cache.
        if (requester != null) {
            var linkReq = new CraftingLink(CraftingCpuHelper.generateLinkData(craftId, false, true), requester);

            var craftingService = (CraftingService) grid.getCraftingService();
            craftingService.addLink(linkCpu);
            craftingService.addLink(linkReq);

            return linkReq;
        } else {
            return linkCpu;
        }
    }

    public void tickCraftingLogic(final IEnergyService eg, final CraftingService cc) {
        // Don't tick if we're not active.
        if (!cluster.isActive())
            return;
        // If we don't have a job, just try to dump our items.
        if (this.job == null) {
            this.storeItems();
            return;
        }
        // Check if the job was cancelled.
        if (job.link.isCanceled()) {
            cancel();
            return;
        }

        int remainingOperations = cluster.getCoProcessors() + 1 - (this.usedOps[0] + this.usedOps[1] + this.usedOps[2]);
        final int started = remainingOperations;

        if (remainingOperations > 0) {
            do {
                int pushedPatterns = executeCrafting(remainingOperations, cc, eg, cluster.getLevel());

                if (pushedPatterns > 0) {
                    remainingOperations -= pushedPatterns;
                } else {
                    break;
                }
            } while (remainingOperations > 0);
        }
        this.usedOps[2] = this.usedOps[1];
        this.usedOps[1] = this.usedOps[0];
        this.usedOps[0] = started - remainingOperations;
    }

    /**
     * Try to push patterns into available interfaces, i.e. do the actual crafting execution.
     * 
     * @return How many patterns were successfully pushed.
     */
    public int executeCrafting(int maxPatterns, CraftingService craftingService, IEnergyService energyService,
            Level level) {
        ExecutingCraftingJob job = this.job;
        if (job == null)
            return 0;

        int pushedPatterns = 0;

        var it = job.tasks.entrySet().iterator();
        taskLoop: while (it.hasNext()) {
            var task = it.next();
            if (task.getValue().value <= 0) {
                it.remove();
                continue;
            }

            IPatternDetails details = task.getKey();
            var expectedOutputs = new MixedStackList();
            // Contains the inputs for the pattern.
            @Nullable
            var craftingContainer = CraftingCpuHelper.extractPatternInputs(details, inventory, energyService, level,
                    expectedOutputs);

            // Try to push to each medium.
            for (ICraftingMedium medium : craftingService.getMediums(details)) {
                if (craftingContainer == null)
                    break;
                if (medium.isBusy())
                    continue;

                if (medium.pushPattern(details, craftingContainer)) {
                    CraftingCpuHelper.extractPatternPower(details, energyService, Actionable.MODULATE);
                    pushedPatterns++;

                    for (var expectedOutput : expectedOutputs) {
                        job.waitingFor.injectItems(expectedOutput, Actionable.MODULATE);
                    }

                    cluster.markDirty();

                    task.getValue().value--;
                    if (task.getValue().value <= 0) {
                        it.remove();
                        continue taskLoop;
                    }

                    if (pushedPatterns == maxPatterns) {
                        break taskLoop;
                    }

                    // Prepare next inputs.
                    expectedOutputs.resetStatus();
                    craftingContainer = CraftingCpuHelper.extractPatternInputs(details, inventory, energyService,
                            level, expectedOutputs);
                }
            }

            // Failed to push this pattern, reinject the inputs.
            if (craftingContainer != null) {
                CraftingCpuHelper.reinjectPatternInputs(inventory, craftingContainer);
            }
        }

        return pushedPatterns;
    }

    /**
     * Called by the CraftingService with an Integer.MAX_VALUE priority to inject items that are being waited for.
     */
    public IAEStack injectItems(IAEStack input, Actionable type) {
        // also stop accepting items when the job is complete, i.e. to prevent re-insertion when pushing out
        // items during storeItems
        if (input == null || job == null)
            return input;

        // Only accept items we are waiting for.
        IAEStack waitingFor = job.waitingFor.extractItems(input, Actionable.SIMULATE);
        if (IAEStack.getStackSizeOrZero(waitingFor) <= 0)
            return input;

        IAEStack leftOver = IAEStack.copy(input, 0);
        input = IAEStack.copy(input);

        // Make sure we don't insert more than what we are waiting for.
        if (input.getStackSize() > waitingFor.getStackSize()) {
            long difference = input.getStackSize() - waitingFor.getStackSize();
            leftOver.incStackSize(difference);
            input.decStackSize(difference);
        }

        if (type == Actionable.MODULATE) {
            job.timeTracker.decrementItems(input.getStackSize());
            job.waitingFor.extractItems(input, Actionable.MODULATE);
            cluster.markDirty();
        }

        if (input.equals(job.finalOutput)) {
            // Final output is special: it goes directly into the requester
            IAEStack.add(leftOver, job.link.injectItems(input, type));

            // Note: we ignore any remainder (could be the entire input if there is no requester),
            // we already marked the items as done, and we might even finish the job.

            // This means that the job can be marked as finished even if some items were not actually inserted.
            // In some cases, repeated failed inserts of a fraction of the final output might prevent some recipes from
            // being pushed.
            // TODO: Look into fixing this, perhaps we could use the network monitor to check how much was really
            // TODO: inserted into the network.
            // TODO: Another solution is to wait until all recipes have been pushed before cancelling the job.

            if (type == Actionable.MODULATE) {
                // Update count and displayed CPU stack, and finish the job if possible.
                postChange(input);
                job.finalOutput.decStackSize(input.getStackSize());

                if (job.finalOutput.getStackSize() <= 0) {
                    finishJob(true);
                    cluster.updateOutput(null);
                } else {
                    cluster.updateOutput(job.finalOutput);
                }
            }
        } else {
            IAEStack.add(leftOver, input);

            if (type == Actionable.MODULATE) {
                inventory.injectItems(input, Actionable.MODULATE);
            }
        }

        return leftOver.getStackSize() == 0 ? null : leftOver;
    }

    /**
     * Finish the current job.
     * 
     * @param success True if the job is complete, false if it was cancelled.
     */
    private void finishJob(boolean success) {
        if (success) {
            job.link.markDone();
        } else {
            job.link.cancel();
        }

        // TODO: log

        // Clear waitingFor list and post all the relevant changes.
        job.waitingFor.clear();
        // Notify opened menus of cancelled scheduled tasks.
        for (var entry : job.tasks.entrySet()) {
            for (var output : entry.getKey().getOutputs()) {
                postChange(output);
            }
        }
        // Finish job.
        this.job = null;

        // Store all remaining items.
        this.storeItems();
    }

    /**
     * Cancel the current job.
     */
    public void cancel() {
        // No job to cancel :P
        if (job == null)
            return;

        // Clear displayed stack.
        cluster.updateOutput(null);

        finishJob(false);
    }

    /**
     * Tries to dump all locally stored items back into the storage network.
     */
    public void storeItems() {
        Preconditions.checkState(job == null, "CPU should not have a job to prevent re-insertion when dumping items");
        // Short-circuit if there is nothing to do.
        if (this.inventory.list.isEmpty())
            return;

        final IGrid g = cluster.getGrid();
        if (g == null)
            return;

        final IStorageService sg = g.getService(IStorageService.class);

        for (IAEStack is : this.inventory.list) {
            this.postChange(is);
            IAEStack remainder = GenericStackHelper.injectMonitorable(sg, IAEStack.copy(is), Actionable.MODULATE,
                    cluster.getSrc());

            // The network was unable to receive all of the items, i.e. no or not enough storage space left
            if (remainder != null) {
                is.setStackSize(remainder.getStackSize());
            } else {
                is.reset();
            }
        }

        cluster.markDirty();
    }

    private String generateCraftId(IAEStack finalOutput) {
        final long now = System.currentTimeMillis();
        final int hash = System.identityHashCode(this);
        final int hmm = Objects.hashCode(finalOutput);

        return Long.toString(now, Character.MAX_RADIX) + '-' + Integer.toString(hash, Character.MAX_RADIX) + '-'
                + Integer.toString(hmm, Character.MAX_RADIX);
    }

    private void postChange(IAEStack stack) {
        for (var listener : listeners) {
            listener.accept(stack);
        }
    }

    private void postCraftingDifference(IAEStack stack) {
        IGrid grid = cluster.getGrid();
        if (grid == null)
            return;

        var craftingService = (CraftingService) grid.getCraftingService();
        for (CraftingWatcher watcher : craftingService.getInterestManager().get(stack)) {
            watcher.getHost().onRequestChange(craftingService, stack);
        }

        // Also notify opened menus
        postChange(stack);
    }

    public boolean hasJob() {
        return this.job != null;
    }

    public ElapsedTimeTracker getElapsedTimeTracker() {
        if (this.job != null) {
            return this.job.timeTracker;
        } else {
            return new ElapsedTimeTracker(0);
        }
    }

    public void readFromNBT(CompoundTag data) {
        this.inventory.readFromNBT(data.getList("inventory", 10));
        if (data.contains("job")) {
            this.job = new ExecutingCraftingJob(data.getCompound("job"), this::postCraftingDifference, this);
            cluster.updateOutput(this.job.finalOutput);
        } else {
            cluster.updateOutput(null);
        }
    }

    public void writeToNBT(CompoundTag data) {
        data.put("inventory", this.inventory.writeToNBT());
        if (this.job != null) {
            data.put("job", this.job.writeToNBT());
        }
    }

    public ICraftingLink getLastLink() {
        if (this.job != null) {
            return this.job.link;
        }
        return null;
    }

    public ListCraftingInventory getInventory() {
        return this.inventory;
    }

    /**
     * Register a listener that will receive stacks when either the stored items, await items or pending outputs change.
     * This is only used by the menu. Make sure to remove it by calling {@link #removeListener}.
     */
    public void addListener(Consumer<IAEStack> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<IAEStack> listener) {
        listeners.remove(listener);
    }

    public long getStored(IAEStack template) {
        var extracted = this.inventory.extractItems(IAEStack.copy(template, Long.MAX_VALUE), Actionable.SIMULATE);
        return extracted == null ? 0 : extracted.getStackSize();
    }

    public long getWaitingFor(IAEStack template) {
        IAEStack stack = null;
        if (this.job != null) {
            stack = this.job.waitingFor.extractItems(IAEStack.copy(template, Long.MAX_VALUE), Actionable.SIMULATE);
        }
        return stack == null ? 0 : stack.getStackSize();
    }

    public long getPendingOutputs(IAEStack template) {
        long count = 0;
        if (this.job != null) {
            for (final Map.Entry<IPatternDetails, ExecutingCraftingJob.TaskProgress> t : job.tasks.entrySet()) {
                for (var output : t.getKey().getOutputs()) {
                    if (output.equals(template)) {
                        count += output.getStackSize() * t.getValue().value;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Used by the menu to gather all the kinds of stored items.
     */
    public void getAllItems(MixedStackList out) {
        for (var stack : this.inventory.list) {
            out.add(stack);
        }
        if (this.job != null) {
            for (var stack : job.waitingFor.list) {
                out.add(stack);
            }
            for (final Map.Entry<IPatternDetails, ExecutingCraftingJob.TaskProgress> t : job.tasks.entrySet()) {
                for (var output : t.getKey().getOutputs()) {
                    out.add(IAEStack.copy(output, output.getStackSize() * t.getValue().value));
                }
            }
        }
    }
}
