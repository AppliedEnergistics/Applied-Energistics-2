package appeng.crafting.execution;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.*;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
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
    private final ListCraftingInventory<IAEItemStack> inventory = new ListCraftingInventory<>(StorageChannels.items()) {
        @Override
        public void postChange(IAEItemStack template, long delta) {
            CraftingCpuLogic.this.postChange(template);
        }
    };
    /**
     * Used crafting operations over the last 3 ticks.
     */
    private final int[] usedOps = new int[3];
    private final Set<Consumer<IAEItemStack>> listeners = new HashSet<Consumer<IAEItemStack>>();

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
            // Contains the inputs for the pattern.
            @Nullable
            var craftingContainer = CraftingCpuHelper.extractPatternInputs(details, inventory, energyService, level);

            // Try to push to each medium.
            for (ICraftingMedium medium : craftingService.getMediums(details)) {
                if (craftingContainer == null)
                    break;
                if (medium.isBusy())
                    continue;

                if (medium.pushPattern(details, craftingContainer)) {
                    CraftingCpuHelper.extractPatternPower(details, energyService, Actionable.MODULATE);
                    pushedPatterns++;

                    for (IAEItemStack expectedOutput : CraftingCpuHelper.getExpectedOutputs(details)) {
                        job.waitingFor.injectItems(expectedOutput, Actionable.MODULATE);
                        postChange(expectedOutput);
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
                    craftingContainer = CraftingCpuHelper.extractPatternInputs(details, inventory, energyService, level);
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
    public IAEItemStack injectItems(IAEItemStack input, Actionable type) {
        // also stop accepting items when the job is complete, i.e. to prevent re-insertion when pushing out
        // items during storeItems
        if (input == null || job == null)
            return input;

        // Only accept items we are waiting for.
        IAEItemStack waitingFor = job.waitingFor.extractItems(input, Actionable.SIMULATE);
        if (waitingFor == null || waitingFor.getStackSize() <= 0)
            return input;

        IAEItemStack leftOver = input.copyWithStackSize(0);
        input = input.copy();

        // Make sure we don't insert more than what we are waiting for.
        if (input.getStackSize() > waitingFor.getStackSize()) {
            long difference = input.getStackSize() - waitingFor.getStackSize();
            leftOver.incStackSize(difference);
            input = input.copy().decStackSize(difference);
        }

        if (type == Actionable.MODULATE) {
            job.timeTracker.decrementItems(input.getStackSize());
            job.waitingFor.extractItems(input, Actionable.MODULATE);
            cluster.markDirty();
        }

        if (input.equals(job.finalOutput)) {
            // Final output is special: it goes directly into the requester
            leftOver.add(job.link.injectItems(input, type));

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
            leftOver.add(input);

            if (type == Actionable.MODULATE) {
                inventory.injectItems(input, Actionable.MODULATE);
            }
        }

        return leftOver;
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
        final IMEInventory<IAEItemStack> ii = sg.getInventory(StorageChannels.items());

        for (IAEItemStack is : this.inventory.list) {
            this.postChange(is);
            IAEItemStack remainder = ii.injectItems(is.copy(), Actionable.MODULATE, cluster.getSrc());

            // The network was unable to receive all of the items, i.e. no or not enough storage space left
            if (remainder != null) {
                is.setStackSize(remainder.getStackSize());
            } else {
                is.reset();
            }
        }

        cluster.markDirty();
    }

    private String generateCraftId(IAEItemStack finalOutput) {
        final long now = System.currentTimeMillis();
        final int hash = System.identityHashCode(this);
        final int hmm = Objects.hashCode(finalOutput);

        return Long.toString(now, Character.MAX_RADIX) + '-' + Integer.toString(hash, Character.MAX_RADIX) + '-'
                + Integer.toString(hmm, Character.MAX_RADIX);
    }

    private void postChange(IAEItemStack stack) {
        for (Consumer<IAEItemStack> listener : listeners) {
            listener.accept(stack);
        }
    }

    private void postCraftingDifference(IAEItemStack stack) {
        IGrid grid = cluster.getGrid();
        if (grid == null)
            return;

        var craftingService = (CraftingService) grid.getCraftingService();
        for (CraftingWatcher watcher : craftingService.getInterestManager().get(stack)) {
            watcher.getHost().onRequestChange(craftingService, stack);
        }
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

    public ListCraftingInventory<IAEItemStack> getInventory() {
        return this.inventory;
    }

    /**
     * Register a listener that will receive stacks when either the stored items, await items or pending outputs change.
     * This is only used by the menu. Make sure to remove it by calling {@link #removeListener}.
     */
    public void addListener(Consumer<IAEItemStack> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<IAEItemStack> listener) {
        listeners.remove(listener);
    }

    public long getStored(IAEItemStack template) {
        var extracted = this.inventory.extractItems(template.copyWithStackSize(Long.MAX_VALUE), Actionable.SIMULATE);
        return extracted == null ? 0 : extracted.getStackSize();
    }

    public long getWaitingFor(IAEItemStack template) {
        IAEItemStack stack = null;
        if (this.job != null) {
            stack = this.job.waitingFor.extractItems(template.copyWithStackSize(Long.MAX_VALUE), Actionable.SIMULATE);
        }
        return stack == null ? 0 : stack.getStackSize();
    }

    public long getPendingOutputs(IAEItemStack template) {
        long count = 0;
        if (this.job != null) {
            for (final Map.Entry<IPatternDetails, ExecutingCraftingJob.TaskProgress> t : job.tasks.entrySet()) {
                for (IAEItemStack output : t.getKey().getOutputs()) {
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
    public void getAllItems(IItemList<IAEItemStack> out) {
        for (IAEItemStack stack : this.inventory.list) {
            out.add(stack);
        }
        if (this.job != null) {
            for (IAEItemStack stack : job.waitingFor.list) {
                out.add(stack);
            }
            for (final Map.Entry<IPatternDetails, ExecutingCraftingJob.TaskProgress> t : job.tasks.entrySet()) {
                for (IAEItemStack output : t.getKey().getOutputs()) {
                    out.add(output.copyWithStackSize(output.getStackSize() * t.getValue().value));
                }
            }
        }
    }
}
