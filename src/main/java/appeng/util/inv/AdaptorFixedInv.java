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

package appeng.util.inv;

import java.util.Iterator;

import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ItemStack;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.ItemTransferable;
import alexiil.mc.lib.attributes.item.filter.ItemFilter;

import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;

public class AdaptorFixedInv extends InventoryAdaptor {
    protected final FixedItemInv itemHandler;
    protected final ItemTransferable transferable;

    public AdaptorFixedInv(FixedItemInv itemHandler) {
        this.itemHandler = itemHandler;
        this.transferable = itemHandler.getTransferable();
    }

    @Override
    public boolean hasSlots() {
        return this.itemHandler.getSlotCount() > 0;
    }

    @Override
    public ItemStack removeItems(int amount, ItemStack filter, IInventoryDestination destination) {
        ItemFilter itemFilter = createExactFilter(filter, destination);

        return this.transferable.attemptExtraction(itemFilter, amount, Simulation.ACTION);
    }

    @Override
    public ItemStack simulateRemove(int amount, ItemStack filter, IInventoryDestination destination) {
        ItemFilter itemFilter = createExactFilter(filter, destination);

        return this.transferable.attemptExtraction(itemFilter, amount, Simulation.SIMULATE);
    }

    @NotNull
    private ItemFilter createExactFilter(ItemStack filter, IInventoryDestination destination) {
        ItemFilter itemFilter = destination != null ? destination::canInsert : stack -> true;
        if (!filter.isEmpty()) {
            if (destination != null) {
                itemFilter = stack -> Platform.itemComparisons().isSameItem(stack, filter)
                        && destination.canInsert(stack);
            } else {
                itemFilter = stack -> Platform.itemComparisons().isSameItem(stack, filter);
            }
        }
        return itemFilter;
    }

    @NotNull
    private ItemFilter createFuzzyFilter(ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination) {
        ItemFilter itemFilter = destination != null ? destination::canInsert : stack -> true;
        if (!filter.isEmpty()) {
            if (destination != null) {
                itemFilter = stack -> Platform.itemComparisons().isFuzzyEqualItem(stack, filter, fuzzyMode)
                        && destination.canInsert(stack);
            } else {
                itemFilter = stack -> Platform.itemComparisons().isFuzzyEqualItem(stack, filter, fuzzyMode);
            }
        }
        return itemFilter;
    }

    /**
     * For fuzzy extract, we will only ever extract one slot, since we're afraid of
     * merging two item stacks with different damage values.
     */
    @Override
    public ItemStack removeSimilarItems(int amount, ItemStack filter, FuzzyMode fuzzyMode,
            IInventoryDestination destination) {
        ItemFilter itemFilter = createFuzzyFilter(filter, fuzzyMode, destination);

        return this.transferable.attemptExtraction(itemFilter, amount, Simulation.ACTION);
    }

    @Override
    public ItemStack simulateSimilarRemove(int amount, ItemStack filter, FuzzyMode fuzzyMode,
            IInventoryDestination destination) {
        ItemFilter itemFilter = createFuzzyFilter(filter, fuzzyMode, destination);

        return this.transferable.attemptExtraction(itemFilter, amount, Simulation.SIMULATE);
    }

    @Override
    public ItemStack addItems(ItemStack toBeAdded) {
        return this.addItems(toBeAdded, false);
    }

    @Override
    public ItemStack simulateAdd(ItemStack toBeSimulated) {
        return this.addItems(toBeSimulated, true);
    }

    protected ItemStack addItems(final ItemStack itemsToAdd, final boolean simulate) {
        if (itemsToAdd.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return this.transferable.attemptInsertion(itemsToAdd, simulate ? Simulation.SIMULATE : Simulation.ACTION);
    }

    @Override
    public boolean containsItems() {
        return !transferable.attemptAnyExtraction(1, Simulation.SIMULATE).isEmpty();
    }

    @Override
    public Iterator<ItemSlot> iterator() {
        return new ItemHandlerIterator(this.itemHandler);
    }
}
