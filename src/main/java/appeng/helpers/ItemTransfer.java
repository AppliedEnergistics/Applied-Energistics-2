package appeng.helpers;

import java.util.function.Predicate;

import net.minecraft.world.item.ItemStack;

import appeng.api.config.FuzzyMode;
import appeng.api.inventories.InternalInventory;
import appeng.util.Platform;

public final class ItemTransfer {

    private ItemTransfer() {
    }

    public static ItemStack removeItems(InternalInventory inv, int amount, ItemStack filter,
            Predicate<ItemStack> destination) {
        int slots = inv.size();
        ItemStack rv = ItemStack.EMPTY;

        for (int slot = 0; slot < slots && amount > 0; slot++) {
            final ItemStack is = inv.getStackInSlot(slot);
            if (is.isEmpty() || !filter.isEmpty() && !Platform.itemComparisons().isSameItem(is, filter)) {
                continue;
            }

            if (destination != null) {
                ItemStack extracted = inv.extractItem(slot, amount, true);
                if (extracted.isEmpty()) {
                    continue;
                }

                if (!destination.test(extracted)) {
                    continue;
                }
            }

            // Attempt extracting it
            ItemStack extracted = inv.extractItem(slot, amount, false);

            if (extracted.isEmpty()) {
                continue;
            }

            if (rv.isEmpty()) {
                // Use the first stack as a template for the result
                rv = extracted;
                filter = extracted;
            } else {
                // Subsequent stacks will just increase the extracted size
                rv.grow(extracted.getCount());
            }
            amount -= extracted.getCount();
        }

        return rv;
    }

    public static ItemStack simulateRemove(InternalInventory inv, int amount, ItemStack filter,
            Predicate<ItemStack> destination) {
        int slots = inv.size();
        ItemStack rv = ItemStack.EMPTY;

        for (int slot = 0; slot < slots && amount > 0; slot++) {
            final ItemStack is = inv.getStackInSlot(slot);
            if (!is.isEmpty() && (filter.isEmpty() || Platform.itemComparisons().isSameItem(is, filter))) {
                ItemStack extracted = inv.extractItem(slot, amount, true);

                if (extracted.isEmpty()) {
                    continue;
                }

                if (destination != null && !destination.test(extracted)) {
                    continue;
                }

                if (rv.isEmpty()) {
                    // Use the first stack as a template for the result
                    rv = extracted.copy();
                    filter = extracted;
                } else {
                    // Subsequent stacks will just increase the extracted size
                    rv.grow(extracted.getCount());
                }
                amount -= extracted.getCount();
            }
        }

        return rv;
    }

    /**
     * For fuzzy extract, we will only ever extract one slot, since we're afraid of merging two item stacks with
     * different damage values.
     */
    public static ItemStack removeSimilarItems(InternalInventory inv, int amount, ItemStack filter, FuzzyMode fuzzyMode,
            Predicate<ItemStack> destination) {
        int slots = inv.size();
        ItemStack extracted = ItemStack.EMPTY;

        for (int slot = 0; slot < slots && extracted.isEmpty(); slot++) {
            final ItemStack is = inv.getStackInSlot(slot);
            if (is.isEmpty()
                    || !filter.isEmpty() && !Platform.itemComparisons().isFuzzyEqualItem(is, filter, fuzzyMode)) {
                continue;
            }

            if (destination != null) {
                ItemStack simulated = inv.extractItem(slot, amount, true);
                if (simulated.isEmpty()) {
                    continue;
                }

                if (!destination.test(simulated)) {
                    continue;
                }
            }

            // Attempt extracting it
            extracted = inv.extractItem(slot, amount, false);
        }

        return extracted;
    }

    public static ItemStack simulateSimilarRemove(InternalInventory inv, int amount, ItemStack filter,
            FuzzyMode fuzzyMode,
            Predicate<ItemStack> destination) {
        int slots = inv.size();
        ItemStack extracted = ItemStack.EMPTY;

        for (int slot = 0; slot < slots && extracted.isEmpty(); slot++) {
            final ItemStack is = inv.getStackInSlot(slot);
            if (is.isEmpty()
                    || !filter.isEmpty() && !Platform.itemComparisons().isFuzzyEqualItem(is, filter, fuzzyMode)) {
                continue;
            }

            // Attempt extracting it
            extracted = inv.extractItem(slot, amount, true);

            if (!extracted.isEmpty() && destination != null && !destination.test(extracted)) {
                extracted = ItemStack.EMPTY; // Keep on looking...
            }
        }

        return extracted;
    }

    public static ItemStack addItems(InternalInventory inv, ItemStack toBeAdded) {
        return inv.addItems(toBeAdded, false);
    }

    public static ItemStack simulateAdd(InternalInventory inv, ItemStack toBeSimulated) {
        return inv.addItems(toBeSimulated, true);
    }

    private ItemStack addItems(InternalInventory inv, ItemStack stack, final boolean simulate) {
        int remaining = stack.getCount();
        for (int i = 0; remaining > 0 && i < inv.size(); i++) {
            if (!inv.isItemValid(i, stack)) {
                continue;
            }

            var inSlot = inv.getStackInSlot(i);
            if (inSlot.isEmpty()) {
                if (!simulate) {
                    inv.setItemDirect(i, stack);
                }
                return ItemStack.EMPTY;
            } else if (Platform.itemComparisons().isSameItem(inSlot, stack)) {
                int freeSpace = inSlot.getMaxStackSize() - inSlot.getCount();
                if (!simulate) {
                    inSlot.grow(Math.min(remaining, freeSpace));
                }
                remaining -= freeSpace;
            }
        }

        if (remaining <= 0) {
            return ItemStack.EMPTY;
        } else {
            var r = stack.copy();
            r.setCount(remaining);
            return r;
        }
    }

}
