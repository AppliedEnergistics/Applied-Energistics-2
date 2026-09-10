/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

import java.util.Objects;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

public class FilteredInternalInventory extends BaseInternalInventory {
    private final InternalInventory delegate;
    private final IAEItemFilter filter;

    public FilteredInternalInventory(InternalInventory delegate, IAEItemFilter filter) {
        this.delegate = Objects.requireNonNull(delegate);
        this.filter = Objects.requireNonNull(filter);
    }

    @Override
    public void setItemDirect(int slot, ItemStack stack) {
        delegate.setItemDirect(slot, stack);
    }

    @Override
    public int size() {
        return this.delegate.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.delegate.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!this.filter.allowInsert(this.delegate, slot, stack)) {
            return stack;
        }

        return this.delegate.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!this.filter.allowExtract(this.delegate, slot, amount)) {
            return ItemStack.EMPTY;
        }

        return this.delegate.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.delegate.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (!this.filter.allowInsert(this.delegate, slot, stack)) {
            return false;
        }
        return this.delegate.isItemValid(slot, stack);
    }

    @Override
    public void sendChangeNotification(int slot) {
        delegate.sendChangeNotification(slot);
    }

    @Override
    protected ResourceHandler<ItemResource> createResourceHandler() {
        return new FilteringResourceHandler();
    }

    class FilteringResourceHandler extends DelegatingResourceHandler<ItemResource> {
        public FilteringResourceHandler() {
            super(FilteredInternalInventory.this.delegate.toResourceHandler());
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return super.isValid(index, resource)
                    && filter.allowInsert(FilteredInternalInventory.this.delegate, index, resource.toStack());
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (!filter.allowInsert(FilteredInternalInventory.this.delegate, index, resource.toStack())) {
                return 0;
            }
            return super.insert(index, resource, amount, transaction);
        }

        @Override
        public int insert(ItemResource resource, int amount, TransactionContext transaction) {
            // This duplicates the default implementation from ResourceHandler, which is inaccessible here
            // We need to check the filter for each index we access, which is impossible if we call the delegates
            // implementation
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

            int inserted = 0;
            int size = size();
            for (int index = 0; index < size; index++) {
                inserted += insert(index, resource, amount - inserted, transaction);
                if (inserted == amount)
                    break;
            }
            return inserted;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (!filter.allowExtract(FilteredInternalInventory.this.delegate, index, amount)) {
                return 0;
            }
            return super.extract(index, resource, amount, transaction);
        }

        @Override
        public int extract(ItemResource resource, int amount, TransactionContext transaction) {
            // This duplicates the default implementation from ResourceHandler, which is inaccessible here
            // We need to check the filter for each index we access, which is impossible if we call the delegates
            // implementation
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

            int extracted = 0;
            int size = size();
            for (int index = 0; index < size; index++) {
                extracted += extract(index, resource, amount - extracted, transaction);
                if (extracted == amount)
                    break;
            }
            return extracted;
        }
    }
}
