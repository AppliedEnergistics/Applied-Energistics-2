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
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.GenericStack;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;
import appeng.core.AELog;
import appeng.crafting.CraftingLink;
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
    private final ListCraftingInventory inventory = new ListCraftingInventory(
            (what, delta) -> CraftingCpuLogic.this.postChange(what));
    /**
     * Used crafting operations over the last 3 ticks.
     */
    private final int[] usedOps = new int[3];
    private final Set<Consumer<AEKey>> listeners = new HashSet<>();

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
        var craftId = this.generateCraftId(plan.finalOutput());
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

        var remainingOperations = cluster.getCoProcessors() + 1 - (this.usedOps[0] + this.usedOps[1] + this.usedOps[2]);
        final var started = remainingOperations;

        if (remainingOperations > 0) {
            do {
                var pushedPatterns = executeCrafting(remainingOperations, cc, eg, cluster.getLevel());

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
        var job = this.job;
        if (job == null)
            return 0;

        var pushedPatterns = 0;

        var it = job.tasks.entrySet().iterator();
        taskLoop: while (it.hasNext()) {
            var task = it.next();
            if (task.getValue().value <= 0) {
                it.remove();
                continue;
            }

            var details = task.getKey();
            var expectedOutputs = new KeyCounter();
            // Contains the inputs for the pattern.
            @Nullable
            var craftingContainer = CraftingCpuHelper.extractPatternInputs(
                    details, inventory, energyService, level, expectedOutputs);

            // Try to push to each provider.
            for (var provider : craftingService.getProviders(details)) {
                if (craftingContainer == null)
                    break;
                if (provider.isBusy())
                    continue;

                if (provider.pushPattern(details, craftingContainer)) {
                    CraftingCpuHelper.extractPatternPower(details, energyService, Actionable.MODULATE);
                    pushedPatterns++;

                    for (var expectedOutput : expectedOutputs) {
                        job.waitingFor.insert(expectedOutput.getKey(), expectedOutput.getLongValue(),
                                Actionable.MODULATE);
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
                    expectedOutputs.reset();
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
     *
     * @return Consumed amount.
     */
    public long insert(AEKey what, long amount, Actionable type) {
        // also stop accepting items when the job is complete, i.e. to prevent re-insertion when pushing out
        // items during storeItems
        if (what == null || job == null)
            return 0;

        // Only accept items we are waiting for.
        var waitingFor = job.waitingFor.extract(what, amount, Actionable.SIMULATE);
        if (waitingFor <= 0) {
            return 0;
        }

        // Make sure we don't insert more than what we are waiting for.
        if (amount > waitingFor) {
            amount = waitingFor;
        }

        if (type == Actionable.MODULATE) {
            job.timeTracker.decrementItems(amount);
            job.waitingFor.extract(what, amount, Actionable.MODULATE);
            cluster.markDirty();
        }

        long inserted = amount;
        if (what.matches(job.finalOutput)) {
            // Final output is special: it goes directly into the requester
            inserted = job.link.insert(what, amount, type);

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
                postChange(what);
                job.finalOutput = new GenericStack(what, job.finalOutput.amount() - amount);

                if (job.finalOutput.amount() <= 0) {
                    finishJob(true);
                    cluster.updateOutput(null);
                } else {
                    cluster.updateOutput(job.finalOutput);
                }
            }
        } else {
            if (type == Actionable.MODULATE) {
                inventory.insert(what, amount, Actionable.MODULATE);
            }
        }

        return inserted;
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
                postChange(output.what());
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

        var g = cluster.getGrid();
        if (g == null)
            return;

        var storage = g.getStorageService().getInventory();

        for (var entry : this.inventory.list) {
            this.postChange(entry.getKey());
            var inserted = storage.insert(entry.getKey(), entry.getLongValue(),
                    Actionable.MODULATE, cluster.getSrc());

            // The network was unable to receive all of the items, i.e. no or not enough storage space left
            entry.setValue(entry.getLongValue() - inserted);
        }
        this.inventory.list.removeZeros();

        cluster.markDirty();
    }

    private String generateCraftId(GenericStack finalOutput) {
        final var now = System.currentTimeMillis();
        final var hash = System.identityHashCode(this);
        final var hmm = Objects.hashCode(finalOutput);

        return Long.toString(now, Character.MAX_RADIX) + '-' + Integer.toString(hash, Character.MAX_RADIX) + '-'
                + Integer.toString(hmm, Character.MAX_RADIX);
    }

    private void postChange(AEKey what) {
        for (var listener : listeners) {
            listener.accept(what);
        }
    }

    private void postCraftingDifference(AEKey what, long amount) {
        var grid = cluster.getGrid();
        if (grid == null)
            return;

        var craftingService = (CraftingService) grid.getCraftingService();
        for (var watcher : craftingService.getInterestManager().get(what)) {
            watcher.getHost().onRequestChange(craftingService, what);
        }

        // Also notify opened menus
        postChange(what);
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
    public void addListener(Consumer<AEKey> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<AEKey> listener) {
        listeners.remove(listener);
    }

    public long getStored(AEKey template) {
        return this.inventory.extract(template, Long.MAX_VALUE, Actionable.SIMULATE);
    }

    public long getWaitingFor(AEKey template) {
        if (this.job != null) {
            return this.job.waitingFor.extract(template, Long.MAX_VALUE, Actionable.SIMULATE);
        }
        return 0;
    }

    public long getPendingOutputs(AEKey template) {
        long count = 0;
        if (this.job != null) {
            for (var t : job.tasks.entrySet()) {
                for (var output : t.getKey().getOutputs()) {
                    if (template.matches(output)) {
                        count += output.amount() * t.getValue().value;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Used by the menu to gather all the kinds of stored items.
     */
    public void getAllItems(KeyCounter out) {
        out.addAll(this.inventory.list);
        if (this.job != null) {
            out.addAll(job.waitingFor.list);
            for (final var t : job.tasks.entrySet()) {
                for (var output : t.getKey().getOutputs()) {
                    out.add(output.what(), output.amount() * t.getValue().value);
                }
            }
        }
    }
}
