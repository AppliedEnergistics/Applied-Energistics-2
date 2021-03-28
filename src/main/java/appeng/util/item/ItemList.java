/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2020, AlgorithmX2, All rights reserved.
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

package appeng.util.item;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.minecraft.item.Item;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

public final class ItemList implements IItemList<IAEItemStack> {

    private final Reference2ObjectMap<Item, IItemList<IAEItemStack>> records = new Reference2ObjectOpenHashMap<>();

    @Override
    public IAEItemStack findPrecise(final IAEItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }

        IItemList<IAEItemStack> record = this.records.get(itemStack.getItem());
        return record != null ? record.findPrecise(itemStack) : null;
    }

    @Override
    public Collection<IAEItemStack> findFuzzy(final IAEItemStack filter, final FuzzyMode fuzzy) {
        if (filter == null) {
            return Collections.emptyList();
        }

        IItemList<IAEItemStack> record = this.records.get(filter.getItem());
        return record != null ? record.findFuzzy(filter, fuzzy) : Collections.emptyList();
    }

    @Override
    public boolean isEmpty() {
        return !this.iterator().hasNext();
    }

    @Override
    public void add(final IAEItemStack itemStack) {
        if (itemStack == null) {
            return;
        }

        this.getOrCreateRecord(itemStack.getItem()).add(itemStack);
    }

    @Override
    public void addStorage(final IAEItemStack itemStack) {
        if (itemStack == null) {
            return;
        }

        this.getOrCreateRecord(itemStack.getItem()).addStorage(itemStack);
    }

    @Override
    public void addCrafting(final IAEItemStack itemStack) {
        if (itemStack == null) {
            return;
        }

        this.getOrCreateRecord(itemStack.getItem()).addCrafting(itemStack);
    }

    @Override
    public void addRequestable(final IAEItemStack itemStack) {
        if (itemStack == null) {
            return;
        }

        this.getOrCreateRecord(itemStack.getItem()).addRequestable(itemStack);
    }

    @Override
    public IAEItemStack getFirstItem() {
        for (final IAEItemStack stackType : this) {
            return stackType;
        }

        return null;
    }

    @Override
    public int size() {
        int size = 0;
        for (IItemList<IAEItemStack> entry : records.values()) {
            size += entry.size();
        }

        return size;
    }

    @Override
    public Iterator<IAEItemStack> iterator() {
        return new ChainedIterator(this.records.values().iterator());
    }

    @Override
    public void resetStatus() {
        for (final IAEItemStack i : this) {
            i.reset();
        }
    }

    private IItemList<IAEItemStack> getOrCreateRecord(Item item) {
        return this.records.computeIfAbsent(item, this::makeRecordMap);
    }

    private IItemList<IAEItemStack> makeRecordMap(Item item) {
        if (item.isDamageable()) {
            return new FuzzyItemList();
        } else {
            return new StrictItemList();
        }
    }

    /**
     * Iterates over multiple item lists as if they were one list.
     */
    private static class ChainedIterator implements Iterator<IAEItemStack> {

        private final Iterator<IItemList<IAEItemStack>> parent;
        private Iterator<IAEItemStack> next;

        public ChainedIterator(Iterator<IItemList<IAEItemStack>> iterator) {
            this.parent = iterator;
            this.ensureItems();
        }

        @Override
        public boolean hasNext() {
            return next != null && next.hasNext();
        }

        @Override
        public IAEItemStack next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }

            IAEItemStack result = this.next.next();
            this.ensureItems();
            return result;
        }

        private void ensureItems() {
            if (hasNext()) {
                return; // Still items left in the current one
            }

            // Find the next iterator willing to return some items...
            while (this.parent.hasNext()) {
                this.next = this.parent.next().iterator();

                if (this.next.hasNext()) {
                    return; // Found one!
                }
            }

            // No more items
            this.next = null;
        }
    }
}
