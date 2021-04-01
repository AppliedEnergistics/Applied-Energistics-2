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
import java.util.List;

import net.minecraft.item.ItemStack;

import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.iterators.StackToSlotIterator;

public class AdaptorList extends InventoryAdaptor {

    private final List<ItemStack> i;

    public AdaptorList(final List<ItemStack> s) {
        this.i = s;
    }

    @Override
    public boolean hasSlots() {
        return !this.i.isEmpty();
    }

    @Override
    public ItemStack removeItems(int amount, final ItemStack filter, final IInventoryDestination destination) {
        final int s = this.i.size();
        for (int x = 0; x < s; x++) {
            final ItemStack is = this.i.get(x);
            if (!is.isEmpty() && (filter.isEmpty() || Platform.itemComparisons().isSameItem(is, filter))) {
                if (amount > is.getCount()) {
                    amount = is.getCount();
                }
                if (destination != null && !destination.canInsert(is)) {
                    amount = 0;
                }

                if (amount > 0) {
                    final ItemStack rv = is.copy();
                    rv.setCount(amount);
                    is.grow(-amount);

                    // remove it..
                    if (is.getCount() <= 0) {
                        this.i.remove(x);
                    }

                    return rv;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack simulateRemove(int amount, final ItemStack filter, final IInventoryDestination destination) {
        for (final ItemStack is : this.i) {
            if (!is.isEmpty() && (filter.isEmpty() || Platform.itemComparisons().isSameItem(is, filter))) {
                if (amount > is.getCount()) {
                    amount = is.getCount();
                }
                if (destination != null && !destination.canInsert(is)) {
                    amount = 0;
                }

                if (amount > 0) {
                    final ItemStack rv = is.copy();
                    rv.setCount(amount);
                    return rv;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeSimilarItems(int amount, final ItemStack filter, final FuzzyMode fuzzyMode,
            final IInventoryDestination destination) {
        final int s = this.i.size();
        for (int x = 0; x < s; x++) {
            final ItemStack is = this.i.get(x);
            if (!is.isEmpty()
                    && (filter.isEmpty() || Platform.itemComparisons().isFuzzyEqualItem(is, filter, fuzzyMode))) {
                if (amount > is.getCount()) {
                    amount = is.getCount();
                }
                if (destination != null && !destination.canInsert(is)) {
                    amount = 0;
                }

                if (amount > 0) {
                    final ItemStack rv = is.copy();
                    rv.setCount(amount);
                    is.grow(-amount);

                    // remove it..
                    if (is.getCount() <= 0) {
                        this.i.remove(x);
                    }

                    return rv;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack simulateSimilarRemove(int amount, final ItemStack filter, final FuzzyMode fuzzyMode,
            final IInventoryDestination destination) {
        for (final ItemStack is : this.i) {
            if (!is.isEmpty()
                    && (filter.isEmpty() || Platform.itemComparisons().isFuzzyEqualItem(is, filter, fuzzyMode))) {
                if (amount > is.getCount()) {
                    amount = is.getCount();
                }
                if (destination != null && !destination.canInsert(is)) {
                    amount = 0;
                }

                if (amount > 0) {
                    final ItemStack rv = is.copy();
                    rv.setCount(amount);
                    return rv;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack addItems(final ItemStack toBeAdded) {
        if (toBeAdded.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (toBeAdded.getCount() == 0) {
            return ItemStack.EMPTY;
        }

        final ItemStack left = toBeAdded.copy();

        for (final ItemStack is : this.i) {
            if (ItemStack.areItemsEqualIgnoreDurability(is, left)) {
                is.grow(left.getCount());
                return ItemStack.EMPTY;
            }
        }

        this.i.add(left);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack simulateAdd(final ItemStack toBeSimulated) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean containsItems() {
        for (final ItemStack is : this.i) {
            if (!is.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<ItemSlot> iterator() {
        return new StackToSlotIterator(this.i.iterator());
    }
}
