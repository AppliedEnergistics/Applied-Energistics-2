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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.execution.CraftingCpuHelper;
import appeng.crafting.execution.InputTemplate;
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
    private final AEKey what;
    private final long amount;
    /**
     * The patterns that can make this node. Null if they haven't been computed yet.
     */
    private ArrayList<CraftingTreeProcess> nodes = null;
    private final boolean canEmit;

    public CraftingTreeNode(ICraftingService cc, CraftingCalculation job, AEKey what, long amount,
            CraftingTreeProcess par, int slot) {
        this.parent = par;
        this.parentInput = slot == -1 ? null : par.details.getInputs()[slot];
        this.level = job.getLevel();
        this.job = job;
        this.what = findCraftedStack(cc, what);
        this.amount = amount;

        this.canEmit = cc.canEmitFor(what);
    }

    private AEKey findCraftedStack(ICraftingService cc, AEKey wat) {
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

    private void buildChildPatterns() {
        // Sanity check: this should never be called if this is emitable
        if (this.canEmit) {
            throw new IllegalStateException("Internal AE2 error: this node is emitable, it shouldn't use patterns!");
        }

        if (this.nodes == null) {
            this.nodes = new ArrayList<>();

            var gridNode = this.job.simRequester.getGridNode();

            // If the node is null, we just skip patterns and let the request (likely) fail.
            if (gridNode != null) {
                var craftingService = gridNode.getGrid().getCraftingService();

                for (var details : craftingService.getCraftingFor(this.what)) {
                    if (this.parent == null || this.parent.notRecursive(details)) {
                        this.nodes.add(new CraftingTreeProcess(craftingService, job, details, this));
                    }
                }
            }
        }
    }

    /**
     * Return true if adding this pattern as a child would not cause recursion.
     */
    boolean notRecursive(final IPatternDetails details) {
        for (var output : details.getOutputs()) {
            if (this.what.matches(output)) {
                return false;
            }
        }

        for (var input : details.getInputs()) {
            if (this.what.matches(input.getPossibleInputs()[0])) {
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
            @Nullable KeyCounter containerItems)
            throws CraftBranchFailure, InterruptedException {
        this.job.handlePausing();

        inv.addStackBytes(what, amount, requestedAmount);

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
                addContainerItems(template.key(), extracted, containerItems);

                if (requestedAmount == 0) {
                    return;
                }
            }
        }

        // Already add the container items: if we fail, the process above will fail and they will be discarded anyway.
        addContainerItems(what, requestedAmount, containerItems);

        /*
         * 2) EMITABLE ITEMS
         */
        if (this.canEmit) {
            inv.emitItems(this.what, this.amount * requestedAmount);
            return;
        }

        /*
         * 3) USE PATTERNS
         */
        buildChildPatterns();
        long totalRequestedItems = requestedAmount * this.amount;
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
                var available = inv.extract(this.what, totalRequestedItems, Actionable.MODULATE);
                if (available != 0) {
                    totalRequestedItems -= available;

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
                        var available = child.extract(this.what, totalRequestedItems, Actionable.MODULATE);

                        if (available != 0) {
                            child.applyDiff(inv);

                            totalRequestedItems -= available;

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
            job.addMissing(this.what, totalRequestedItems);
        } else {
            throw new CraftBranchFailure(this.what, totalRequestedItems);
        }
    }

    // Only item stacks are supported.
    private void addContainerItems(AEKey template, long multiplier,
            @Nullable KeyCounter outputList) {
        if (outputList != null) {
            var containerItem = parentInput.getContainerItem(template);
            if (containerItem != null) {
                outputList.add(containerItem, multiplier);
            }
        }
    }

    /**
     * Get all stack templates that can be used for this node.
     * 
     * @param inv Crafting inventory, used for fuzzy matching.
     */
    private Iterable<InputTemplate> getValidItemTemplates(ICraftingInventory inv) {
        if (this.parentInput == null)
            return List.of(new InputTemplate(what, 1));
        return CraftingCpuHelper.getValidItemTemplates(inv, this.parentInput, level);
    }

    long getNodeCount() {
        long tot = 1;
        if (this.nodes != null) {
            for (final CraftingTreeProcess pro : this.nodes) {
                tot += pro.getNodeCount();
            }
        }
        return tot;
    }

    boolean hasMultiplePaths() {
        if (this.nodes == null) {
            return false;
        }
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
