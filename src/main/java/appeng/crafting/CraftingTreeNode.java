/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.crafting;

import java.util.*;

import javax.annotation.Nullable;

import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.MixedItemList;
import appeng.crafting.execution.CraftingCpuHelper;
import appeng.crafting.inv.ChildCraftingSimulationState;
import appeng.crafting.inv.CraftingSimulationState;
import appeng.crafting.inv.ICraftingInventory;

/**
 * A crafting tree node is what represents a single requested stack in the crafting process. It can either be the
 * top-level requested stack (slot is then -1, parent is null), or a stack used in a pattern (slot is then the position
 * of this stack in the pattern, parent is the parent node).
 */
public class CraftingTreeNode {

    /**
     * what input this node is for. Null for the top-level node.
     */
    @Nullable
    final IPatternDetails.IInput parentInput;
    private final CraftingCalculation job;
    // parent node.
    private final CraftingTreeProcess parent;
    private final Level level;
    /**
     * "Template" of the item this node is making. For top-level node: the count is always 1. For child nodes: the count
     * is that of the template of the corresponding input.
     */
    private final IAEStack what;
    // what are the crafting patterns for this?
    private final ArrayList<CraftingTreeProcess> nodes = new ArrayList<>();
    private final boolean canEmit;

    public CraftingTreeNode(final ICraftingService cc, final CraftingCalculation job, final IAEStack wat,
            final CraftingTreeProcess par, final int slot) {
        this.parent = par;
        this.parentInput = slot == -1 ? null : par.details.getInputs()[slot];
        this.level = job.getLevel();
        this.job = job;
        this.what = findCraftedStack(cc, wat);

        this.canEmit = cc.canEmitFor(wat);
        if (this.canEmit) {
            return; // if you can emit for something, you can't make it with patterns.
        }

        for (var details : cc.getCraftingFor(this.what)) {
            if (this.parent == null || this.parent.notRecursive(details)) {
                this.nodes.add(new CraftingTreeProcess(cc, job, details, this));
            }
        }
    }

    private IAEStack findCraftedStack(ICraftingService cc, IAEStack wat) {
        if (cc.canEmitFor(wat)) {
            return wat; // if we can emit for something, use that.
        }

        var patterns = cc.getCraftingFor(wat);

        if (patterns.isEmpty() && parentInput != null) {
            // No pattern: try to fuzzy match the primary output,
            // in case one of the patterns used the wrong damage by mistake.
            var fuzzy = cc.getFuzzyCraftable(wat, fuzzyCandidate -> {
                return this.parentInput.isValid(fuzzyCandidate, level);
            });

            if (fuzzy != null) {
                return fuzzy;
            }
        }

        return wat;
    }

    /**
     * Return true if adding this pattern as a child would not cause recursion.
     */
    boolean notRecursive(final IPatternDetails details) {
        for (var output : details.getOutputs()) {
            if (output.equals(this.what)) {
                return false;
            }
        }

        for (var input : details.getInputs()) {
            if (input.getPossibleInputs()[0].equals(this.what)) {
                return false;
            }
        }

        if (this.parent == null) {
            return true;
        }

        return this.parent.notRecursive(details);
    }

    /**
     * Request items. Will always succeed or throw an exception.
     * 
     * @param inv             Current simulated inventory.
     * @param requestedAmount How many items. The raw amount for top-level requests, or the number of inputs for
     *                        requests that have a parent.
     * @param containerItems  A list where produced container items are written if it's not null.
     * @throws CraftBranchFailure If the request failed.
     */
    void request(final CraftingSimulationState inv, long requestedAmount,
            @Nullable MixedItemList containerItems)
            throws CraftBranchFailure, InterruptedException {
        this.job.handlePausing();

        if (this.parent == null) {
            // Raw amount, so we need to divide by transfer factor.
            long transferFactor = this.what.getChannel().transferFactor();
            inv.addBytes((requestedAmount + transferFactor - 1) / transferFactor);
        } else {
            inv.addBytes(requestedAmount);
        }

        /*
         * 1) COLLECT ITEMS FROM THE INVENTORY
         */
        // Templates: must copy before using!
        for (var template : getValidItemTemplates(inv)) {
            long extracted = CraftingCpuHelper.extractTemplates(inv, template, requestedAmount);

            if (extracted > 0) {
                // TODO: we should keep track of which items we extracted to make sure the CPU uses exactly those when
                // TODO: it processes the job.
                requestedAmount -= extracted;
                addContainerItems(template, extracted, containerItems);

                if (requestedAmount == 0) {
                    return;
                }
            }
        }

        // Already add the container items: if we fail, the process above will fail and they will be discarded anyway.
        addContainerItems(this.what, requestedAmount, containerItems);

        /*
         * 2) EMITABLE ITEMS
         */
        if (this.canEmit) {
            var emitted = (IAEStack) IAEStack.copy(this.what);
            emitted.multStackSize(requestedAmount);
            inv.emitItems(emitted);
            return;
        }

        /*
         * 3) USE PATTERNS
         */
        long totalRequestedItems = requestedAmount * this.what.getStackSize();
        if (this.nodes.size() == 1) {
            // Single branch: just query as much as we can and let it throw if that's not possible.
            final CraftingTreeProcess pro = this.nodes.get(0);
            var craftedPerPattern = pro.getOutputCount(this.what);

            while (pro.possible && totalRequestedItems > 0) {
                long times;
                if (pro.limitsQuantity()) {
                    times = 1;
                } else {
                    // Craft all at once!
                    times = (totalRequestedItems + craftedPerPattern - 1) / craftedPerPattern;
                }
                pro.request(inv, times);

                // by now we have succeeded, as request throws an exception in case of failure
                // check how much was actually produced
                var available = inv.extractItems(IAEStack.copy(this.what, totalRequestedItems),
                        Actionable.MODULATE);
                if (available != null) {
                    totalRequestedItems -= available.getStackSize();

                    if (totalRequestedItems <= 0) {
                        return;
                    }
                } else {
                    throw new UnsupportedOperationException("Unexpected error in the crafting calculation.");
                }
            }
        } else if (this.nodes.size() > 1) {
            // Multiple branches: try as much as possible of one branch before moving to the next one.
            for (final CraftingTreeProcess pro : this.nodes) {
                try {
                    while (pro.possible && totalRequestedItems > 0) {
                        final ChildCraftingSimulationState child = new ChildCraftingSimulationState(inv);
                        // craft one by one, using the sub inventory as target
                        pro.request(child, 1);

                        // by now we have succeeded, as request throws an exception in case of failure
                        var available = child
                                .extractItems(IAEStack.copy(this.what, totalRequestedItems), Actionable.MODULATE);

                        if (available != null) {
                            child.applyDiff(inv);

                            totalRequestedItems -= available.getStackSize();

                            if (totalRequestedItems <= 0) {
                                return;
                            }
                        } else {
                            pro.possible = false; // ;P
                        }
                    }
                } catch (final CraftBranchFailure fail) {
                    // TODO: why try again after a failure? just in case we receive the right inputs by chance?
                    pro.possible = true;
                }
            }
        }

        if (this.job.isSimulation()) {
            job.addMissing(IAEStack.copy(this.what, totalRequestedItems));
        } else {
            throw new CraftBranchFailure(this.what, totalRequestedItems);
        }
    }

    // Only item stacks are supported.
    private void addContainerItems(IAEStack template, long multiplier,
            @Nullable MixedItemList outputList) {
        if (outputList != null) {
            var containerItem = parentInput.getContainerItem(template);
            if (containerItem != null) {
                containerItem.multStackSize(multiplier);
                outputList.addStorage(containerItem);
            }
        }
    }

    /**
     * Get all stack templates that can be used for this node.
     * 
     * @param inv Crafting inventory, used for fuzzy matching.
     */
    private Iterable<IAEStack> getValidItemTemplates(ICraftingInventory inv) {
        if (this.parentInput == null)
            return List.of(IAEStack.copy(this.what, 1));
        return CraftingCpuHelper.getValidItemTemplates(inv, this.parentInput, level);
    }

    boolean isValid(IAEStack stack) {
        return parentInput != null && parentInput.isValid(stack, level);
    }

    long getNodeCount() {
        long tot = 1;

        for (final CraftingTreeProcess pro : this.nodes) {
            tot += pro.getNodeCount();
        }

        return tot;
    }

    boolean hasMultiplePaths() {
        if (this.nodes.size() > 1) {
            return true;
        }
        for (var pro : this.nodes) {
            if (pro.hasMultiplePaths()) {
                return true;
            }
        }
        return false;
    }
}
