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

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.hooks.BasicEventHooks;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.core.Api;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.Platform;

public class CraftingTreeProcess {

    private final CraftingTreeNode parent;
    final ICraftingPatternDetails details;
    private final CraftingJob job;
    private final Map<CraftingTreeNode, Long> nodes = new HashMap<>();
    private final int depth;
    boolean possible = true;
    private final World world;
    private long crafts = 0;
    private boolean containerItems;
    private boolean limitQty;
    /**
     * If true, this means that this crafting recipe needs to be re-simulate every time because it's too custom
     * for us to assume anything about it.
     */
    private boolean fullSimulation;
    private long bytes = 0;

    public CraftingTreeProcess(final ICraftingGrid cc, final CraftingJob job, final ICraftingPatternDetails details,
            final CraftingTreeNode craftingTreeNode, final int depth) {
        this.parent = craftingTreeNode;
        this.details = details;
        this.job = job;
        this.depth = depth;
        this.world = job.getWorld();

        if (details.isCraftable()) {
            gatherCraftingChildren(cc);
        } else {
            gatherProcessingChildren(cc);
        }
    }

    private void gatherCraftingChildren(ICraftingGrid cc) {
        final IAEItemStack[] list = details.getSparseInputs();

        // Fill the inventory with inputs, and throw the crafting event to detect handlers tampering with the inputs.
        // FIXME CRAFTING this looks hacky and is probably broken with custom recipe types
        final CraftingInventory ic = new CraftingInventory(new ContainerNull(), 3, 3);
        final IAEItemStack[] is = details.getSparseInputs();
        for (int x = 0; x < ic.getSizeInventory(); x++) {
            ic.setInventorySlotContents(x, is[x] == null ? ItemStack.EMPTY : is[x].createItemStack());
        }

        BasicEventHooks.firePlayerCraftingEvent(Platform.getPlayer((ServerWorld) world),
                details.getOutput(ic, world), ic);

        // Enable full simulation if the handler tampered with the inputs (I think).
        // FIXME CRAFTING I wonder why exactly this was added, and if it's necessary to support it at all.
        for (int x = 0; x < ic.getSizeInventory(); x++) {
            final ItemStack g = ic.getStackInSlot(x);
            if (!g.isEmpty() && g.getCount() > 1) {
                this.fullSimulation = true;
            }
        }

        // Inspect pattern for limitQty and containerItems
        updateLimitQuantityAndContainerItems();

        // Add children
        // FIXME CRAFTING what's the difference between these two branches?
        if (this.containerItems) {
            for (int x = 0; x < list.length; x++) {
                final IAEItemStack part = list[x];
                if (part != null) {
                    this.nodes.put(new CraftingTreeNode(cc, job, part.copy(), this, x, depth + 1),
                            part.getStackSize());
                }
            }
        } else {
            // this is minor different then below, this slot uses the pattern, but kinda
            // fudges it.
            for (final IAEItemStack part : details.getInputs()) {
                for (int x = 0; x < list.length; x++) {
                    final IAEItemStack comparePart = list[x];
                    if (part != null && part.equals(comparePart)) {
                        // use the first slot...
                        this.nodes.put(new CraftingTreeNode(cc, job, part.copy(), this, x, depth + 1),
                                part.getStackSize());
                        break;
                    }
                }
            }
        }
    }

    private void gatherProcessingChildren(ICraftingGrid cc) {
        updateLimitQuantityAndContainerItems();

        for (final IAEItemStack part : details.getInputs()) {
            this.nodes.put(new CraftingTreeNode(cc, job, part.copy(), this, -1, depth + 1), part.getStackSize());
        }
    }

    /**
     * Set {@link #limitQty} to true if some inputs are also outputs.
     * For crafting recipes, also update {@link #limitQty} and {@link #containerItems} if this pattern contains
     * container items.
     */
    private void updateLimitQuantityAndContainerItems() {
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

            if (details.isCraftable()) {
                // Also check for container items if this is a crafting pattern
                if (g.getItem().hasContainerItem(g)) {
                    this.limitQty = this.containerItems = true;
                }
            }
        }
    }

    /**
     * @see CraftingTreeNode#notRecursive
     */
    boolean notRecursive(final ICraftingPatternDetails details) {
        return this.parent == null || this.parent.notRecursive(details);
    }

    long getTimes(final long remaining, final long stackSize) {
        if (this.limitQty || this.fullSimulation) {
            return 1;
        }
        return (remaining / stackSize) + (remaining % stackSize != 0 ? 1 : 0);
    }

    void request(final MECraftingInventory inv, final long i, final IActionSource src)
            throws CraftBranchFailure, InterruptedException {
        this.job.handlePausing();

        if (this.fullSimulation) {
            final CraftingInventory ic = new CraftingInventory(new ContainerNull(), 3, 3);

            for (final Entry<CraftingTreeNode, Long> entry : this.nodes.entrySet()) {
                final IAEItemStack item = entry.getKey().getStack(entry.getValue());
                final IAEItemStack stack = entry.getKey().request(inv, item.getStackSize(), src);

                ic.setInventorySlotContents(entry.getKey().getSlot(), stack.createItemStack());
            }

            // FIXME CRAFTING world was null here because it was never set and it worked,
            // FIXME CRAFTING so is passing the world useful?
            BasicEventHooks.firePlayerCraftingEvent(Platform.getPlayer((ServerWorld) this.world),
                    this.details.getOutput(ic, this.world), ic);

            for (int x = 0; x < ic.getSizeInventory(); x++) {
                ItemStack is = ic.getStackInSlot(x);
                is = Platform.getContainerItem(is);

                final IAEItemStack o = Api.instance().storage().getStorageChannel(IItemStorageChannel.class)
                        .createStack(is);
                if (o != null) {
                    this.bytes++;
                    inv.injectItems(o, Actionable.MODULATE, src);
                }
            }
        } else {
            // request and remove inputs...
            for (final Entry<CraftingTreeNode, Long> entry : this.nodes.entrySet()) {
                final IAEItemStack item = entry.getKey().getStack(entry.getValue());
                final IAEItemStack stack = entry.getKey().request(inv, item.getStackSize() * i, src);

                if (this.containerItems) {
                    final ItemStack is = Platform.getContainerItem(stack.createItemStack());
                    final IAEItemStack o = Api.instance().storage().getStorageChannel(IItemStorageChannel.class)
                            .createStack(is);
                    if (o != null) {
                        this.bytes++;
                        inv.injectItems(o, Actionable.MODULATE, src);
                    }
                }
            }
        }

        // assume its possible.

        // add crafting results..
        for (final IAEItemStack out : this.details.getOutputs()) {
            final IAEItemStack o = out.copy();
            o.setStackSize(o.getStackSize() * i);
            inv.injectItems(o, Actionable.MODULATE, src);
        }

        this.crafts += i;
    }

    void dive(final CraftingJob job) {
        job.addTask(this.getAmountCrafted(this.parent.getStack(1)), this.crafts, this.details, this.depth);
        for (final CraftingTreeNode pro : this.nodes.keySet()) {
            pro.dive(job);
        }

        job.addBytes(8 + this.crafts + this.bytes);
    }

    IAEItemStack getAmountCrafted(IAEItemStack what2) {
        for (final IAEItemStack is : this.details.getOutputs()) {
            if (is.equals(what2)) {
                what2 = what2.copy();
                what2.setStackSize(is.getStackSize());
                return what2;
            }
        }

        // more fuzzy!
        for (final IAEItemStack is : this.details.getOutputs()) {
            if (is.getItem() == what2.getItem()
                    && (is.getItem().isDamageable() || is.getItemDamage() == what2.getItemDamage())) {
                what2 = is.copy();
                what2.setStackSize(is.getStackSize());
                return what2;
            }
        }

        throw new IllegalStateException("Crafting Tree construction failed.");
    }

    void resetForSimulation() {
        this.crafts = 0;
        this.bytes = 0;

        for (final CraftingTreeNode pro : this.nodes.keySet()) {
            pro.resetForSimulation();
        }
    }

    void setJob(final MECraftingInventory storage, final CraftingCPUCluster craftingCPUCluster, final IActionSource src)
            throws CraftBranchFailure {
        craftingCPUCluster.addCrafting(this.details, this.crafts);

        for (final CraftingTreeNode pro : this.nodes.keySet()) {
            pro.setJob(storage, craftingCPUCluster, src);
        }
    }

    void getPlan(final IItemList<IAEItemStack> plan) {
        for (IAEItemStack i : this.details.getOutputs()) {
            i = i.copy();
            i.setCountRequestable(i.getStackSize() * this.crafts);
            plan.addRequestable(i);
        }

        for (final CraftingTreeNode pro : this.nodes.keySet()) {
            pro.getPlan(plan);
        }
    }
}
