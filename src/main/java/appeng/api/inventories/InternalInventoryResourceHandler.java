/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.inventories;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import appeng.core.definitions.AEItems;

class InternalInventoryResourceHandler extends SnapshotJournal<InternalInventoryResourceHandler.Snapshot>
        implements ResourceHandler<ItemResource>, IndexModifier<ItemResource> {
    private final InternalInventory inventory;
    @Nullable
    private Snapshot lastReleasedSnapshot;

    public InternalInventoryResourceHandler(InternalInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public void set(int index, ItemResource resource, int amount) {
        inventory.setItemDirect(index, resource.toStack(amount));
    }

    @Override
    public int insert(ItemResource resource, int maxAmount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, maxAmount);

        var stack = resource.toStack(maxAmount);

        updateSnapshots(transaction);

        var overflow = inventory.addItems(stack);
        return maxAmount - overflow.getCount();
    }

    @Override
    public int extract(ItemResource resource, int maxAmount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, maxAmount);

        // Do not allow extraction of wrapped fluid stacks because they're an internal detail
        if (resource.getItem() == AEItems.WRAPPED_GENERIC_STACK.asItem()) {
            return 0;
        }

        updateSnapshots(transaction);

        ItemStack extracted = inventory.removeItems(maxAmount, resource.toStack(), null);

        return extracted.getCount();
    }

    @Override
    public int insert(int index, ItemResource resource, int maxAmount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, maxAmount);

        updateSnapshots(transaction);

        var overflow = inventory.insertItem(index, resource.toStack(maxAmount), false).getCount();
        return maxAmount - overflow;
    }

    @Override
    public int extract(int index, ItemResource resource, int maxAmount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, maxAmount);

        // Do not allow extraction of wrapped fluid stacks because they're an internal detail
        if (resource.getItem() == AEItems.WRAPPED_GENERIC_STACK.asItem()) {
            return 0;
        }

        updateSnapshots(transaction);

        return inventory.extractItem(index, maxAmount, false).getCount();
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return inventory.isItemValid(index, resource.toStack());
    }

    @Override
    public ItemResource getResource(int index) {
        return ItemResource.of(inventory.getStackInSlot(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return inventory.getStackInSlot(index).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return inventory.getSlotLimit(index);
    }

    @Override
    protected Snapshot createSnapshot() {
        Snapshot snapshot;
        if (this.lastReleasedSnapshot != null && this.lastReleasedSnapshot.items.length == inventory.size()) {
            snapshot = this.lastReleasedSnapshot;
            this.lastReleasedSnapshot = null;
        } else {
            snapshot = new Snapshot();
        }

        for (int i = 0; i < inventory.size(); i++) {
            var stack = inventory.getStackInSlot(i);
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
            // We do not restore NBT since the Storage API does not give access to the original NBT and the inventory
            // doesn't mutate it itself
            if (stack.getCount() != counts[i]) {
                stack.setCount(counts[i]);
            }
            inventory.setItemDirect(i, stack);
        }
    }

    @Override
    protected void releaseSnapshot(Snapshot snapshot) {
        this.lastReleasedSnapshot = snapshot;
    }

    public class Snapshot {
        ItemStack[] items;
        int[] counts;

        public Snapshot() {
            this.items = new ItemStack[inventory.size()];
            this.counts = new int[inventory.size()];
        }
    }

    @Override
    public void onRootCommit(Snapshot original) {
        // Diff the last snapshot against the inventory to collect change notifications
        Preconditions.checkState(lastReleasedSnapshot != null, "There should have been at least one snapshot");

        for (int i = 0; i < lastReleasedSnapshot.items.length; i++) {
            var current = inventory.getStackInSlot(i);
            if (current != lastReleasedSnapshot.items[i] || current.getCount() != lastReleasedSnapshot.counts[i]) {
                inventory.sendChangeNotification(i);
            }
        }
    }
}
