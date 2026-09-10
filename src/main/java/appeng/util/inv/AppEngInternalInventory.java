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

import java.util.Arrays;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import appeng.api.inventories.BaseInternalInventory;
import appeng.core.definitions.AEItems;
import appeng.util.inv.filter.IAEItemFilter;

public class AppEngInternalInventory extends BaseInternalInventory {
    private boolean enableClientEvents = false;
    private InternalInventoryHost host;
    private final NonNullList<ItemStack> stacks;
    private final int[] maxStack;
    private IAEItemFilter filter;
    private boolean notifyingChanges = false;
    private boolean inTransactionalCode;

    public AppEngInternalInventory(InternalInventoryHost host, int size, int maxStack, IAEItemFilter filter) {
        this.setHost(host);
        this.setFilter(filter);
        this.maxStack = new int[size];
        this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        Arrays.fill(this.maxStack, maxStack);
    }

    public AppEngInternalInventory(@Nullable InternalInventoryHost inventory, int size, int maxStack) {
        this(inventory, size, maxStack, null);
    }

    public AppEngInternalInventory(int size) {
        this(null, size, 64);
    }

    public AppEngInternalInventory(@Nullable InternalInventoryHost inventory, int size) {
        this(inventory, size, 64);
    }

    public void setFilter(IAEItemFilter filter) {
        this.filter = filter;
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.maxStack[slot];
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return stacks.get(slotIndex);
    }

    @Override
    public void setItemDirect(int slot, ItemStack stack) {
        stacks.set(slot, stack);
        notifyContentsChanged(slot);
    }

    private void notifyContentsChanged(int slot) {
        if (!inTransactionalCode) {
            onContentsChanged(slot);
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        Preconditions.checkArgument(slot >= 0 && slot < size(), "slot out of range");

        if (this.filter != null && !this.filter.allowExtract(this, slot, amount)) {
            return ItemStack.EMPTY;
        }

        var stack = stacks.get(slot);

        // This inventory adheres to vanilla stack size limits
        int toExtract = Math.min(stack.getCount(), Math.min(amount, stack.getMaxStackSize()));
        if (toExtract <= 0) {
            return ItemStack.EMPTY;
        }

        if (stack.getCount() <= toExtract) {
            if (!simulate) {
                setItemDirect(slot, ItemStack.EMPTY);
                return stack;
            } else {
                return stack.copy();
            }
        } else {
            var result = stack.copy();

            if (!simulate) {
                stack.shrink(toExtract);
                notifyContentsChanged(slot);
            }

            result.setCount(toExtract);
            return result;
        }
    }

    protected void onContentsChanged(int slot) {
        if (this.host != null && this.eventsEnabled() && !this.notifyingChanges) {
            this.notifyingChanges = true;
            this.host.onChangeInventory(this, slot);
            this.host.saveChangedInventory(this);
            this.notifyingChanges = false;
        }
    }

    protected boolean eventsEnabled() {
        return this.host != null && !this.host.isClientSide() || this.isEnableClientEvents();
    }

    public void setMaxStackSize(int slot, int size) {
        this.maxStack[slot] = size;
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        if (this.maxStack[slot] == 0) {
            return false;
        }
        if (this.filter != null) {
            return this.filter.allowInsert(this, slot, stack);
        }
        return true;
    }

    public ItemContainerContents toItemContainerContents() {
        return ItemContainerContents.fromItems(stacks);
    }

    public void fromItemContainerContents(ItemContainerContents contents) {
        contents.copyInto(stacks);
    }

    public void writeToNBT(ValueOutput output, String name) {
        var list = output.childrenList(name);
        for (int i = 0; i < stacks.size(); i++) {
            var stack = stacks.get(i);
            if (!stack.isEmpty()) {
                var entry = list.addChild();
                entry.store(ItemStack.MAP_CODEC, stack);
                entry.putInt("Slot", i);
            }
        }
    }

    public void readFromNBT(ValueInput input, String name) {
        for (var entry : input.childrenListOrEmpty(name)) {
            int slot = entry.getIntOr("Slot", 0);
            if (slot >= 0 && slot < stacks.size()) {
                stacks.set(slot, entry.read(ItemStack.MAP_CODEC).orElse(ItemStack.EMPTY));
            }
        }
    }

    private boolean isEnableClientEvents() {
        return this.enableClientEvents;
    }

    public void setEnableClientEvents(boolean enableClientEvents) {
        this.enableClientEvents = enableClientEvents;
    }

    @ApiStatus.Internal
    public InternalInventoryHost getHost() {
        return host;
    }

    protected final void setHost(InternalInventoryHost host) {
        this.host = host;
    }

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    protected ResourceHandler<ItemResource> createResourceHandler() {
        return new AppEngInternalInventoryResourceHandler();
    }

    private class AppEngInternalInventoryResourceHandler
            extends SnapshotJournal<AppEngInternalInventoryResourceHandler.Snapshot>
            implements ResourceHandler<ItemResource>, IndexModifier<ItemResource> {
        @Nullable
        private Snapshot lastReleasedSnapshot;

        @Override
        public void set(int index, ItemResource resource, int amount) {
            setItemDirect(index, resource.toStack(amount));
        }

        @Override
        public int insert(ItemResource resource, int maxAmount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, maxAmount);

            var stack = resource.toStack(maxAmount);

            updateSnapshots(transaction);

            var prevInTransactionalCode = inTransactionalCode;
            inTransactionalCode = true;
            try {
                var overflow = addItems(stack);
                return maxAmount - overflow.getCount();
            } finally {
                inTransactionalCode = prevInTransactionalCode;
            }
        }

        @Override
        public int extract(ItemResource resource, int maxAmount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, maxAmount);

            // Do not allow extraction of wrapped fluid stacks because they're an internal detail
            if (resource.getItem() == AEItems.WRAPPED_GENERIC_STACK.asItem()) {
                return 0;
            }

            updateSnapshots(transaction);

            var prevInTransactionalCode = inTransactionalCode;
            inTransactionalCode = true;
            try {
                ItemStack extracted = removeItems(maxAmount, resource.toStack(), null);

                return extracted.getCount();
            } finally {
                inTransactionalCode = prevInTransactionalCode;
            }
        }

        @Override
        public int insert(int index, ItemResource resource, int maxAmount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, maxAmount);

            updateSnapshots(transaction);

            var prevInTransactionalCode = inTransactionalCode;
            inTransactionalCode = true;
            try {
                var overflow = insertItem(index, resource.toStack(maxAmount), false).getCount();
                return maxAmount - overflow;
            } finally {
                inTransactionalCode = prevInTransactionalCode;
            }
        }

        @Override
        public int extract(int index, ItemResource resource, int maxAmount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, maxAmount);

            // Do not allow extraction of wrapped fluid stacks because they're an internal detail
            if (resource.getItem() == AEItems.WRAPPED_GENERIC_STACK.asItem()) {
                return 0;
            }

            updateSnapshots(transaction);

            var prevInTransactionalCode = inTransactionalCode;
            inTransactionalCode = true;
            try {
                return extractItem(index, maxAmount, false).getCount();
            } finally {
                inTransactionalCode = prevInTransactionalCode;
            }
        }

        @Override
        public int size() {
            return AppEngInternalInventory.this.size();
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return AppEngInternalInventory.this.isItemValid(index, resource.toStack());
        }

        @Override
        public ItemResource getResource(int index) {
            return ItemResource.of(AppEngInternalInventory.this.getStackInSlot(index));
        }

        @Override
        public long getAmountAsLong(int index) {
            return AppEngInternalInventory.this.getStackInSlot(index).getCount();
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            if (!resource.isEmpty() && !isValid(index, resource)) {
                return 0;
            }
            return AppEngInternalInventory.this.getSlotLimit(index);
        }

        @Override
        protected Snapshot createSnapshot() {
            Snapshot snapshot;
            if (this.lastReleasedSnapshot != null && this.lastReleasedSnapshot.items.length == size()) {
                snapshot = this.lastReleasedSnapshot;
                this.lastReleasedSnapshot = null;
            } else {
                snapshot = new Snapshot();
            }

            for (int i = 0; i < size(); i++) {
                var stack = stacks.get(i);
                snapshot.items[i] = stack;
                snapshot.counts[i] = stack.getCount();
            }
            return snapshot;
        }

        @Override
        protected void revertToSnapshot(Snapshot snapshot) {
            var items = snapshot.items;
            var counts = snapshot.counts;
            for (int i = 0; i < items.length; i++) {
                var stack = items[i];
                // Restore the previous count as well, the inventory might mutate the stack count for extract/insert
                // We do not restore NBT since the Storage API does not give access to the original NBT and the
                // inventory
                // doesn't mutate it itself
                if (stack.getCount() != counts[i]) {
                    stack.setCount(counts[i]);
                }
                stacks.set(i, stack);
            }
        }

        @Override
        protected void releaseSnapshot(Snapshot snapshot) {
            this.lastReleasedSnapshot = snapshot;
        }

        public class Snapshot {
            final ItemStack[] items;
            final int[] counts;

            public Snapshot() {
                this.items = new ItemStack[size()];
                this.counts = new int[size()];
            }
        }

        @Override
        public void onRootCommit(Snapshot original) {
            for (int i = 0; i < original.items.length; i++) {
                var current = stacks.get(i);
                if (current != original.items[i] || current.getCount() != original.counts[i]) {
                    notifyContentsChanged(i);
                }
            }
        }
    }
}
