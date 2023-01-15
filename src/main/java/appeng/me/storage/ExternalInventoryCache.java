package appeng.me.storage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;

class ExternalInventoryCache {
    private GenericStack[] cached = new GenericStack[0];
    private final ExternalStorageFacade facade;

    private ExternalInventoryCache(ExternalStorageFacade facade) {
        this.facade = facade;
    }

    public static ExternalInventoryCache of(ExternalStorageFacade facade) {
        return new ExternalInventoryCache(facade);
    }

    public void getAvailableItems(KeyCounter out) {
        for (GenericStack stack : cached) {
            out.add(stack.what(), stack.amount());
        }
    }

    public Set<AEKey> update() {
        var changes = new HashSet<AEKey>();
        final int slots = this.facade.getSlots();

        // Make room for new slots
        if (slots > this.cached.length) {
            this.cached = Arrays.copyOf(this.cached, slots);
        }

        for (int slot = 0; slot < slots; slot++) {
            // Save the old stuff
            var oldGenericStack = this.cached[slot];
            var newGenericStack = facade.getStackInSlot(slot);

            this.handlePossibleSlotChanges(slot, oldGenericStack, newGenericStack, changes);
        }

        // Handle cases where the number of slots actually is lower now than before
        if (slots < this.cached.length) {
            for (int slot = slots; slot < this.cached.length; slot++) {
                final GenericStack aeStack = this.cached[slot];

                if (aeStack != null) {
                    changes.add(aeStack.what());
                }
            }

            // Reduce the cache size
            this.cached = Arrays.copyOf(this.cached, slots);
        }

        return changes;
    }

    private void handlePossibleSlotChanges(int slot, GenericStack oldStack, GenericStack newStack, Set<AEKey> changes) {
        if (oldStack != null && newStack != null && oldStack.what().equals(newStack.what())) {
            handleAmountChanged(slot, oldStack, newStack, changes);
        } else {
            handleItemChanged(slot, oldStack, newStack, changes);
        }
    }

    private void handleAmountChanged(int slot, GenericStack oldStack, GenericStack newStack, Set<AEKey> changes) {
        // Still the same item, but amount might have changed
        if (newStack.amount() != oldStack.amount()) {
            this.cached[slot] = newStack;
            changes.add(newStack.what());
        }
    }

    private void handleItemChanged(int slot, GenericStack oldStack, GenericStack newStack, Set<AEKey> changes) {
        // Completely different item
        this.cached[slot] = newStack;

        // If we had a stack previously in this slot, notify the network about its disappearance
        if (oldStack != null) {
            changes.add(oldStack.what());
        }

        // Notify the network about the new stack
        if (newStack != null) {
            changes.add(newStack.what());
        }
    }

}
