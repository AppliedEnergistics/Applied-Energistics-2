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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterators;

import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.inv.ChildCraftingSimulationState;
import appeng.crafting.inv.CraftingSimulationState;
import appeng.crafting.inv.ICraftingInventory;

/**
 * A crafting tree node is what represents a single requested stack in the crafting process. It can either be the
 * top-level requested stack (slot is then -1, parent is null), or a stack used in a pattern (slot is then the position
 * of this stack in the pattern, parent is the parent node).
 */
public class CraftingTreeNode {

    // what slot!
    private final int slot;
    private final CraftingCalculation job;
    // parent node.
    private final CraftingTreeProcess parent;
    private final Level level;
    // what item is this?
    // note: the count is not necessarily correct at construction time, it's set in request()
    private final IAEItemStack what;
    // what are the crafting patterns for this?
    private final ArrayList<CraftingTreeProcess> nodes = new ArrayList<>();
    private final boolean canEmit;

    public CraftingTreeNode(final ICraftingService cc, final CraftingCalculation job, final IAEItemStack wat,
            final CraftingTreeProcess par, final int slot) {
        this.what = wat;
        this.parent = par;
        this.slot = slot;
        this.level = job.getLevel();
        this.job = job;

        this.canEmit = cc.canEmitFor(this.what);

        if (this.canEmit) {
            return; // if you can emit for something, you can't make it with patterns.
        }

        for (final ICraftingPatternDetails details : cc.getCraftingFor(this.what,
                this.parent == null ? null : this.parent.details, slot, this.level)) {
            if (this.parent == null || this.parent.notRecursive(details)) {
                this.nodes.add(new CraftingTreeProcess(cc, job, details, this));
            }
        }
    }

    /**
     * Return true if adding this pattern as a child would not cause recursion.
     */
    boolean notRecursive(final ICraftingPatternDetails details) {
        Collection<IAEItemStack> o = details.getOutputs();

        for (final IAEItemStack i : o) {
            if (i.equals(this.what)) {
                return false;
            }
        }

        o = details.getInputs();

        for (final IAEItemStack i : o) {
            if (i.equals(this.what)) {
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
     * @param requestedAmount How much.
     * @return One of the items that were inserted.
     * @throws CraftBranchFailure If the request failed.
     */
    // TODO: get rid of the return, it's only used to inject container items back into the inventory.
    IAEItemStack request(final CraftingSimulationState<IAEItemStack> inv, long requestedAmount)
            throws CraftBranchFailure, InterruptedException {
        this.job.handlePausing();

        /*
         * 1) COLLECT ITEMS FROM THE INVENTORY
         */
        // Templates: must copy before using!
        Iterator<IAEItemStack> validItemTemplates = getValidItemTemplates(inv);

        while (validItemTemplates.hasNext()) {
            IAEItemStack toExtract = validItemTemplates.next().copyWithStackSize(requestedAmount);
            IAEItemStack available = inv.extractItems(toExtract, Actionable.MODULATE);

            if (available != null) {
                inv.addBytes(available.getStackSize());
                requestedAmount -= available.getStackSize();

                if (requestedAmount == 0) {
                    return available;
                }
            }
        }

        /*
         * 2) EMITABLE ITEMS
         */
        if (this.canEmit) {
            final IAEItemStack wat = this.what.copyWithStackSize(requestedAmount);

            inv.emitItems(wat);
            inv.addBytes(wat.getStackSize());

            return wat;
        }

        /*
         * 3) USE PATTERNS
         */
        if (this.nodes.size() == 1) {
            // Single branch: just query as much as we can and let it throw if that's not possible.
            final CraftingTreeProcess pro = this.nodes.get(0);

            while (pro.possible && requestedAmount > 0) {
                // either of these two functions may limit how much is crafted at once.
                final IAEItemStack madeWhat = pro.getAmountCrafted(this.what);
                long times = pro.getTimes(requestedAmount, madeWhat.getStackSize());
                pro.request(inv, times);

                // by now we have succeeded, as request throws an exception in case of failure
                // check how much was actually produced
                final IAEItemStack available = inv.extractItems(madeWhat.setStackSize(requestedAmount),
                        Actionable.MODULATE);
                if (available != null) {
                    inv.addBytes(available.getStackSize());
                    requestedAmount -= available.getStackSize();

                    if (requestedAmount <= 0) {
                        return available;
                    }
                } else {
                    pro.possible = false; // ;P
                }
            }
        } else if (this.nodes.size() > 1) {
            // Multiple branches: try as much as possible of one branch before moving to the next one.
            for (final CraftingTreeProcess pro : this.nodes) {
                try {
                    while (pro.possible && requestedAmount > 0) {
                        final ChildCraftingSimulationState<IAEItemStack> child = new ChildCraftingSimulationState<>(
                                StorageChannels.items(), inv);
                        // final MECraftingInventory subInv = new MECraftingInventory(inv, true, true);
                        // craft one by one, using the sub inventory as target
                        pro.request(child, 1);

                        // by now we have succeeded, as request throws an exception in case of failure
                        this.what.setStackSize(requestedAmount);
                        final IAEItemStack available = child.extractItems(this.what, Actionable.MODULATE);

                        if (available != null) {
                            child.applyDiff(inv);

                            inv.addBytes(available.getStackSize());
                            requestedAmount -= available.getStackSize();

                            if (requestedAmount <= 0) {
                                return available;
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
            job.addMissing(this.what.copyWithStackSize(requestedAmount));
            inv.addBytes(requestedAmount);
            return this.what.copyWithStackSize(requestedAmount);
        } else {
            throw new CraftBranchFailure(this.what, requestedAmount);
        }
    }

    /**
     * Get all stack templates that can be used for this node.
     * 
     * @param inv Crafting inventory, used for fuzzy matching.
     */
    private Iterator<IAEItemStack> getValidItemTemplates(ICraftingInventory<IAEItemStack> inv) {
        if (this.getSlot() >= 0) {
            if (this.parent == null || !this.parent.details.isCraftable()) {
                throw new AssertionError("If the slot is not -1, the parent must be a crafting pattern.");
            }

            // Special case: if this is a crafting pattern and there is a parent, also try to use a substitute input.
            final Collection<IAEItemStack> itemList; // All possible substitute inputs, and fuzzy matching stacks.

            if (this.parent.details.canSubstitute()) {
                final List<IAEItemStack> substitutes = this.parent.details.getSubstituteInputs(this.slot);
                itemList = new ArrayList<>(substitutes.size());

                for (IAEItemStack stack : substitutes) {
                    itemList.addAll(inv.findFuzzyTemplates(stack));
                }
            } else {
                itemList = List.of(this.what);
            }

            // Fuzzy might match too much: make sure the items are actually valid in the pattern.
            return Iterators.filter(itemList.iterator(), stack -> {
                return this.parent.details.isValidItemForSlot(this.getSlot(),
                        stack.copy().setStackSize(1).createItemStack(), this.level);
            });
        } else {
            return List.of(this.what).iterator();
        }
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

    int getSlot() {
        return this.slot;
    }
}
