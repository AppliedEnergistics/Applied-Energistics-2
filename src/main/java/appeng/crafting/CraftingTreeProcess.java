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

import net.minecraft.world.item.ItemStack;

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
    final ICraftingPatternDetails details;
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
            final ICraftingPatternDetails details,
            final CraftingTreeNode craftingTreeNode) {
        this.parent = craftingTreeNode;
        this.details = details;
        this.job = job;

        if (details.isCraftable()) {
            final IAEItemStack[] list = details.getSparseInputs();

            updateLimitQty(true);

            if (this.containerItems) {
                // One node per SPARSE input!
                for (int x = 0; x < list.length; x++) {
                    final IAEItemStack part = list[x];
                    if (part != null) {
                        this.nodes.put(new CraftingTreeNode(cc, job, part.copy(), this, x),
                                part.getStackSize());
                    }
                }
            } else {
                // Here we combine identical slots into one node: one node per (non sparse) input.
                for (final IAEItemStack part : details.getInputs()) {
                    for (int x = 0; x < list.length; x++) {
                        final IAEItemStack comparePart = list[x];
                        if (part != null && part.equals(comparePart)) {
                            // use the first slot...
                            this.nodes.put(new CraftingTreeNode(cc, job, part.copy(), this, x),
                                    part.getStackSize());
                            break;
                        }
                    }
                }
            }
        } else {
            updateLimitQty(false);

            for (final IAEItemStack part : details.getInputs()) {
                this.nodes.put(new CraftingTreeNode(cc, job, part.copy(), this, -1), part.getStackSize());
            }
        }
    }

    /**
     * @see CraftingTreeNode#notRecursive
     */
    boolean notRecursive(final ICraftingPatternDetails details) {
        return this.parent == null || this.parent.notRecursive(details);
    }

    /**
     * Check if this pattern has one of its outputs as input. If that's the case, update {@code limitQty} to make sure
     * we simulate this pattern one by one. Also check for container items.
     */
    private void updateLimitQty(boolean checkContainerItems) {
        for (final IAEItemStack part : details.getInputs()) {
            final ItemStack g = part.createItemStack();

            boolean isAnInput = false;
            for (final IAEItemStack a : details.getOutputs()) {
                if (!g.isEmpty() && a != null && a.equals(g)) {
                    isAnInput = true;
                }
            }

            if (isAnInput) {
                this.limitQty = true;
            }

            if (checkContainerItems && g.getItem().hasContainerItem(g)) {
                this.limitQty = this.containerItems = true;
            }
        }
    }

    long getTimes(final long remaining, final long stackSize) {
        if (this.limitQty) {
            return 1;
        }
        return remaining / stackSize + (remaining % stackSize != 0 ? 1 : 0);
    }

    void request(final CraftingSimulationState<IAEItemStack> inv, final long times)
            throws CraftBranchFailure, InterruptedException {
        this.job.handlePausing();

        // request and remove inputs...
        for (final Entry<CraftingTreeNode, Long> entry : this.nodes.entrySet()) {
            final IAEItemStack item = entry.getKey().getStackWithSize(entry.getValue());
            final IAEItemStack stack = entry.getKey().request(inv, item.getStackSize() * times);

            if (this.containerItems) {
                final IAEItemStack o = Platform.getContainerItem(stack);
                if (o != null) {
                    inv.addBytes(1);
                    inv.injectItems(o, Actionable.MODULATE);
                }
            }
        }

        // by now we must have succeeded, otherwise an exception would have been thrown by request() above

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

    IAEItemStack getAmountCrafted(IAEItemStack what2) {
        for (final IAEItemStack is : this.details.getOutputs()) {
            if (is.equals(what2)) {
                return what2.copyWithStackSize(is.getStackSize());
            }
        }

        // more fuzzy!
        for (final IAEItemStack is : this.details.getOutputs()) {
            if (is.getItem() == what2.getItem()
                    && (!is.getItem().canBeDepleted() || is.getItemDamage() == what2.getItemDamage())) {
                // TODO: why doesn't this respect the stack size? in case there are multiple matching fuzzy outputs?
                return is.copy();
            }
        }

        throw new IllegalStateException("Crafting Tree construction failed.");
    }
}
