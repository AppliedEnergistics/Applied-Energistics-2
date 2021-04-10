package com.jaquadro.minecraft.storagedrawers.api.capabilities;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * An interface for treating an inventory as a slotless, central repository of items.
 *
 * For all operations that accept a predicate, if a predicate is supplied, a stored ItemStack must pass the predicate
 * in order to be considered for the given operation.
 *
 * An IItemRepository implementation MAY relax or eliminate its own internal tests when a predicate is supplied.  If
 * the predicate is derived from DefaultPredicate, then the implementation MUST apply any tests it would have applied
 * had no predicate been provided at all, in addition to testing the predicate itself.
 */
public interface IItemRepository
{
    /**
     * Gets a list of all items in the inventory.  The same item may appear multiple times with varying counts.

     * @return A list of zero or more items in the inventory.
     */
    @Nonnull
    NonNullList<ItemRecord> getAllItems ();

    /**
     * Inserts an ItemStack into the inventory and returns the remainder.
     *
     * @param stack     ItemStack to insert.
     * @param simulate  If true, the insertion is only simulated
     * @param predicate See interface notes about predicates.  Passing null specifies default matching.
     * @return The remaining ItemStack that was not inserted.  If the entire stack was accepted, returns
     * ItemStack.EMPTY instead.
     */
    @Nonnull
    ItemStack insertItem (@Nonnull ItemStack stack, boolean simulate, Predicate<ItemStack> predicate);

    @Nonnull
    default ItemStack insertItem (@Nonnull ItemStack stack, boolean simulate) {
        return insertItem(stack, simulate, null);
    }

    /**
     * Tries to extract the given ItemStack from the inventory.  The returned value will be a matching ItemStack
     * with a stack size equal to or less than amount, or the empty ItemStack if the item could not be found at all.
     * The returned stack size may exceed the ItemStack's getMaxStackSize() value.

     * @param stack     The item to extract.  The stack size is ignored.
     * @param amount    Amount to extract (may be greater than the stacks max limit)
     * @param simulate  If true, the extraction is only simulated
     * @param predicate See interface notes about predicates.  Passing null specifies default matching.
     * @return ItemStack extracted from the inventory, or ItemStack.EMPTY if nothing could be extracted.
     */
    @Nonnull
    ItemStack extractItem (@Nonnull ItemStack stack, int amount, boolean simulate, Predicate<ItemStack> predicate);

    @Nonnull
    default ItemStack extractItem (@Nonnull ItemStack stack, int amount, boolean simulate) {
        return extractItem(stack, amount, simulate, null);
    }

    /**
     * Gets the number of items matching the given ItemStack stored by the inventory.

     * @param stack     ItemStack to query.
     * @param predicate See interface notes about predicates.  Passing null specifies default matching.
     * @return The number of stored matching items.  A value of Integer.MAX_VALUE may indicate an infinite item source.
     */
    default int getStoredItemCount (@Nonnull ItemStack stack, Predicate<ItemStack> predicate) {
        ItemStack amount = extractItem(stack, Integer.MAX_VALUE, true, predicate);
        return amount.getCount();
    }

    default int getStoredItemCount (@Nonnull ItemStack stack) {
        return getStoredItemCount(stack, null);
    }

    /**
     * Gets the number items matching the given ItemStack that additionally still be stored by the inventory.
     * Remaining capacity may include space that is internally empty or unassigned to any given item.
     *
     * @param stack     ItemStack to query.
     * @param predicate See interface notes about predicates.  Passing null specifies default matching.
     * @return The available remaining space for matching items.
     */
    default int getRemainingItemCapacity (@Nonnull ItemStack stack, Predicate<ItemStack> predicate) {
        stack = stack.copy();
        stack.setCount(Integer.MAX_VALUE);
        ItemStack remainder = insertItem(stack, true, predicate);
        return Integer.MAX_VALUE - remainder.getCount();
    }

    default int getRemainingItemCapacity (@Nonnull ItemStack stack) {
        return getRemainingItemCapacity(stack, null);
    }

    /**
     * Gets the total inventory capacity for items matching the given ItemStack.
     * Total capacity may include space that is internally empty or unassigned to any given item.
     *
     * @param stack     ItemStack to query.
     * @param predicate See interface notes about predicates.  Passing null specifies default matching.
     * @return The total capacity for matching items.
     */
    default int getItemCapacity (@Nonnull ItemStack stack, Predicate<ItemStack> predicate) {
        long capacity = getStoredItemCount(stack, predicate) + getRemainingItemCapacity(stack, predicate);
        if (capacity > Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        return (int)capacity;
    }

    default int getItemCapacity (@Nonnull ItemStack stack) {
        return getItemCapacity(stack, null);
    }

    /**
     * An item record representing an item and the amount stored.
     *
     * The ItemStack held by itemPrototype always reports a stack size of 1.
     * IT IS IMPORTANT THAT YOU NEVER MODIFY itemPrototype.
     */
    class ItemRecord
    {
        @Nonnull
        public final ItemStack itemPrototype;
        public final int count;

        public ItemRecord (@Nonnull ItemStack itemPrototype, int count) {
            this.itemPrototype = itemPrototype;
            this.count = count;
        }
    }

    /**
     * A variant of the standard Predicate interface that when passed to IItemRepository functions, will ask the
     * internal default predicate to be tested in addition to the custom predicate.  An IItemRepository function
     * may choose to enforce its own predicate regardless.
     */
    interface DefaultPredicate<T> extends Predicate<T> { }
}
