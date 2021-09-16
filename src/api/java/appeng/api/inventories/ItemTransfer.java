package appeng.api.inventories;

import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;

import appeng.api.config.FuzzyMode;

/**
 * Models item transfer that lets the target inventory handle where items are placed into or extracted from.
 */
public interface ItemTransfer {
    ItemStack removeItems(int amount, ItemStack filter, @Nullable Predicate<ItemStack> destination);

    ItemStack simulateRemove(int amount, ItemStack filter, Predicate<ItemStack> destination);

    /**
     * For fuzzy extract, we will only ever extract one slot, since we're afraid of merging two item stacks with
     * different damage values.
     */
    ItemStack removeSimilarItems(int amount, ItemStack filter, FuzzyMode fuzzyMode, Predicate<ItemStack> destination);

    ItemStack simulateSimilarRemove(int amount, ItemStack filter, FuzzyMode fuzzyMode,
            Predicate<ItemStack> destination);

    /**
     * Attempts to insert as much of the given item into this inventory as possible.
     *
     * @param stack The stack to insert. Will not be mutated.
     * @return The overflow, which can be the same object as stack.
     */
    default ItemStack addItems(ItemStack stack) {
        return addItems(stack, false);
    }

    default ItemStack simulateAdd(ItemStack stack) {
        return addItems(stack, true);
    }

    /**
     * Attempts to insert as much of the given item into this inventory as possible.
     *
     * @param stack The stack to insert. Will not be mutated.
     * @return The overflow, which can be the same object as stack.
     */
    @Nonnull
    ItemStack addItems(ItemStack stack, boolean simulate);

    /**
     * Heuristically determine if this transfer object allows inserting or extracting items, i.e. if it's a slot based
     * inventory and has no slots, it probably doesn't allow insertion or extraction.
     */
    boolean mayAllowTransfer();
}
