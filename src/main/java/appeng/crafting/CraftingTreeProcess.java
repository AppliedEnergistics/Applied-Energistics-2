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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import appeng.api.networking.crafting.IPatternDetails;
import appeng.api.storage.StorageChannels;
import appeng.crafting.pattern.PatternDetailsAdapter;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.inv.CraftingSimulationState;
import appeng.util.Platform;

/**
 * A crafting tree process is what represents a pattern in the crafting process. It has a parent node (its output), and
 * a list of child nodes for its inputs.
 */
public class CraftingTreeProcess {

    private final CraftingTreeNode parent;
    final IPatternDetails details;
    private final CraftingCalculation job;
    private final Map<CraftingTreeNode, Long> nodes = new HashMap<>();
    boolean possible = true;
    private boolean containerItems;
    /**
     * If true, we perform this pattern by 1 at the time. This ensures that container items or outputs get reused when
     * possible.
     */
    private boolean limitQty;

    public CraftingTreeProcess(final ICraftingService cc, final CraftingCalculation job,
            final IPatternDetails details,
            final CraftingTreeNode craftingTreeNode) {
        this.parent = craftingTreeNode;
        this.details = details;
        this.job = job;

        updateLimitQty();

        final IPatternDetails.IInput[] inputs = this.details.getInputs();
        for (int x = 0; x < inputs.length; ++x) {
            var input = inputs[x];
            this.nodes.put(new CraftingTreeNode(cc, job, input.getPossibleInputs()[0].copy(), this, x), input.getMultiplier());
        }
    }

    /**
     * @see CraftingTreeNode#notRecursive
     */
    boolean notRecursive(final IPatternDetails details) {
        return this.parent == null || this.parent.notRecursive(details);
    }

    /**
     * Check if this pattern has one of its outputs as input. If that's the case, update {@code limitQty} to make sure
     * we simulate this pattern one by one. Also check for container items.
     */
    private void updateLimitQty() {
        // TODO: consider checking substitute inputs as well?
        for (final IPatternDetails.IInput input : details.getInputs()) {
            IAEItemStack primaryInput = input.getPossibleInputs()[0];
            boolean isAnInput = false;

            for (final IAEItemStack output : details.getOutputs()) {
                if (output.equals(primaryInput)) {
                    isAnInput = true;
                    break;
                }
            }

            if (isAnInput) {
                this.limitQty = true;
            }

            if (input.getContainerItem(primaryInput) != null) {
                this.limitQty = this.containerItems = true;
            }
        }
    }

    boolean limitsQuantity() {
        return this.limitQty;
    }

    void request(final CraftingSimulationState<IAEItemStack> inv, final long times)
            throws CraftBranchFailure, InterruptedException {
        this.job.handlePausing();

        var containerItems = this.containerItems ? StorageChannels.items().createList() : null;

        // request and remove inputs...
        for (final Entry<CraftingTreeNode, Long> entry : this.nodes.entrySet()) {
            entry.getKey().request(inv, entry.getValue() * times, containerItems);
        }

        // by now we must have succeeded, otherwise an exception would have been thrown by request() above

        // add container items
        if (containerItems != null) {
            for (var stack : containerItems) {
                inv.injectItems(stack, Actionable.MODULATE);
                inv.addBytes(stack.getStackSize());
            }
        }

        // add crafting results..
        for (final IAEItemStack out : this.details.getOutputs()) {
            final IAEItemStack o = out.copy();
            o.setStackSize(o.getStackSize() * times);
            inv.injectItems(o, Actionable.MODULATE);
        }

        inv.addCrafting(details, times);
        inv.addBytes(times);
    }

    long getTreeSize() {
        long tot = 0;

        for (CraftingTreeNode node : this.nodes.keySet()) {
            tot += node.getTreeSize();
        }

        return tot;
    }

    IAEItemStack getMatchingOutput(IAEItemStack requestedItem) {
        for (final IAEItemStack is : this.details.getOutputs()) {
            if (is.equals(requestedItem)) {
                return is.copy();
            }
        }

        // more fuzzy!
        for (final IAEItemStack is : this.details.getOutputs()) {
            if (is.getItem() == requestedItem.getItem()
                    && (!is.getItem().canBeDepleted() || is.getItemDamage() == requestedItem.getItemDamage())) {
                return is.copy();
            }
        }

        throw new IllegalStateException("Crafting Tree construction failed.");
    }
}
