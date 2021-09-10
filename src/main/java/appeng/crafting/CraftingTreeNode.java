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

import appeng.api.networking.crafting.IPatternDetails;
import appeng.api.storage.data.IItemList;
import appeng.crafting.execution.CraftingCpuHelper;

import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.inv.ChildCraftingSimulationState;
import appeng.crafting.inv.CraftingSimulationState;
import appeng.crafting.inv.ICraftingInventory;

import javax.annotation.Nullable;

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
    private final IPatternDetails.IInput parentInput;
    private final CraftingCalculation job;
    // parent node.
    private final CraftingTreeProcess parent;
    private final Level level;
    /**
     * "Template" of the item this node is making.
     * For top-level node: the count is always 1.
     * For child nodes: the count is that of the template of the corresponding input.
     */
    private final IAEItemStack what;
    // what are the crafting patterns for this?
    private final ArrayList<CraftingTreeProcess> nodes = new ArrayList<>();
    private final boolean canEmit;

    public CraftingTreeNode(final ICraftingService cc, final CraftingCalculation job, final IAEItemStack wat,
            final CraftingTreeProcess par, final int slot) {
        this.what = wat;
        this.parent = par;
        this.parentInput = slot == -1 ? null : par.details.getInputs()[slot];
        this.level = job.getLevel();
        this.job = job;

        this.canEmit = cc.canEmitFor(this.what);

        if (this.canEmit) {
            return; // if you can emit for something, you can't make it with patterns.
        }

        for (var details : cc.getCraftingFor(this.what,
                this.parent == null ? null : this.parent.details, slot, this.level)) {
            if (this.parent == null || this.parent.notRecursive(details)) {
                this.nodes.add(new CraftingTreeProcess(cc, job, details, this));
            }
        }
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
     * @param requestedAmount How many items. The raw amount for top-level requests, or the number of inputs for requests that have a parent.
     * @param containerItems  A list where produced container items are written if it's not null.
     * @throws CraftBranchFailure If the request failed.
     */
    // TODO: get rid of the return, it's only used to inject container items back into the inventory.
    void request(final CraftingSimulationState<IAEItemStack> inv, long requestedAmount, @Nullable IItemList<IAEItemStack> containerItems)
            throws CraftBranchFailure, InterruptedException {
        this.job.handlePausing();

        inv.addBytes(requestedAmount);

        /*
         * 1) COLLECT ITEMS FROM THE INVENTORY
         */
        // Templates: must copy before using!
        for (var template : getValidItemTemplates(inv)) {
            long extracted = CraftingCpuHelper.extractTemplates(inv, template, requestedAmount);

            if (extracted > 0) {
                requestedAmount -= extracted;
                addContainerItems(template, extracted, containerItems);

                if (requestedAmount == 0) {
                    return;
                }
            }
        }

        /*
         * 2) EMITABLE ITEMS
         */
        if (this.canEmit) {
            var emitted = this.what.copy().multStackSize(requestedAmount);
            inv.emitItems(emitted);
            addContainerItems(this.what, requestedAmount, containerItems);
            return;
        }

        /*
         * 3) USE PATTERNS
         */
        long totalRequestedItems = requestedAmount * this.what.getStackSize();
        if (this.nodes.size() == 1) {
            // Single branch: just query as much as we can and let it throw if that's not possible.
            final CraftingTreeProcess pro = this.nodes.get(0);
            final IAEItemStack matchingOutput = pro.getMatchingOutput(this.what);

            while (pro.possible && totalRequestedItems > 0) {
                long times;
                if (pro.limitsQuantity()) {
                    times = 1;
                } else {
                    // Craft all at once!
                    times = (totalRequestedItems + matchingOutput.getStackSize() - 1) / matchingOutput.getStackSize();
                }
                pro.request(inv, times);

                // by now we have succeeded, as request throws an exception in case of failure
                // check how much was actually produced
                final IAEItemStack available = inv.extractItems(matchingOutput.copyWithStackSize(totalRequestedItems),
                        Actionable.MODULATE);
                if (available != null) {
                    totalRequestedItems -= available.getStackSize();
                    // TODO: add any produced container items to the list, for now they are ignored!

                    if (totalRequestedItems <= 0) {
                        return;
                    }
                } else {
                    throw new UnsupportedOperationException("Unexpected error in the crafting calculation.");
                }
            }
        } else if (this.nodes.size() > 1) {
            // Multiple branches: try as much as possible of one branch before moving to the next one.
            // TODO: this doesn't check for a matching output and always uses this.what?
            // TODO: either it should check, or the single branch logic should use this.what directly.
            for (final CraftingTreeProcess pro : this.nodes) {
                try {
                    while (pro.possible && totalRequestedItems > 0) {
                        final ChildCraftingSimulationState<IAEItemStack> child = new ChildCraftingSimulationState<>(
                                StorageChannels.items(), inv);
                        // craft one by one, using the sub inventory as target
                        pro.request(child, 1);

                        // by now we have succeeded, as request throws an exception in case of failure
                        final IAEItemStack available = child.extractItems(this.what.copyWithStackSize(totalRequestedItems), Actionable.MODULATE);

                        if (available != null) {
                            child.applyDiff(inv);

                            totalRequestedItems -= available.getStackSize();
                            // TODO: add any produced container items to the list, for now they are ignored!

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
            job.addMissing(this.what.copyWithStackSize(totalRequestedItems));
        } else {
            throw new CraftBranchFailure(this.what, totalRequestedItems);
        }
    }

    // Only item stacks are supported.
    private void addContainerItems(IAEItemStack template, long multiplier, @Nullable IItemList<IAEItemStack> outputList) {
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
    private Iterable<IAEItemStack> getValidItemTemplates(ICraftingInventory<IAEItemStack> inv) {
        if (this.parentInput == null) return List.of(this.what.copyWithStackSize(1));
        return CraftingCpuHelper.getValidItemTemplates(inv, this.parentInput, level);
    }

    long getTreeSize() {
        long tot = 1;

        for (final CraftingTreeProcess pro : this.nodes) {
            tot += pro.getTreeSize();
        }

        return tot;
    }

    IAEItemStack getStackWithSize(final long size) {
        return this.what.copyWithStackSize(size);
    }
}
