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

import java.util.Iterator;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.config.FuzzyMode;
import appeng.api.lookup.AEApis;

public interface InternalInventory extends Iterable<ItemStack>, ItemTransfer {

    @Nullable
    static ItemTransfer wrapExternal(@Nullable BlockEntity be, @Nonnull Direction side) {
        var storage = AEApis.ITEMS.find(be, side);
        if (storage != null) {
            return new PlatformInventoryWrapper(storage);
        }
        return null;
    }

    static InternalInventory empty() {
        return EmptyInternalInventory.INSTANCE;
    }

    default boolean isEmpty() {
        return !iterator().hasNext();
    }

    default Storage<ItemVariant> toStorage() {
        return new InternalInventoryStorage(this);
    }

    default Container toContainer() {
        return new ContainerAdapter(this);
    }

    int size();

    default int getSlotLimit(int slot) {
        return Container.LARGE_MAX_STACK_SIZE;
    }

    ItemStack getStackInSlot(int slotIndex);

    /**
     * Puts the given stack in the given slot and circumvents any potential filters.
     */
    void setItemDirect(int slotIndex, @Nonnull ItemStack stack);

    default boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    default InternalInventory getSubInventory(int fromSlotInclusive, int toSlotExclusive) {
        return new SubInventoryProxy(this, fromSlotInclusive, toSlotExclusive);
    }

    default InternalInventory getSlotInv(int slotIndex) {
        Preconditions.checkArgument(slotIndex >= 0 && slotIndex < size(), "slot out of range");
        return new SubInventoryProxy(this, slotIndex, slotIndex + 1);
    }

    /**
     * @return The redstone signal indicating how full this container is in the [0-15] range.
     */
    default int getRedstoneSignal() {
        var adapter = new ContainerAdapter(this);
        return AbstractContainerMenu.getRedstoneSignalFromContainer(adapter);
    }

    @Nonnull
    @Override
    default Iterator<ItemStack> iterator() {
        return new InternalInventoryIterator(this);
    }

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
    default ItemStack addItems(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // Heuristically use a faster one-pass approach to fill inventories that we consider "large",
        // i.e. external storage drawer inventories that might be exposed as hundreds of slots.
        // 54 is the size of a double chest and will include the player inventory as well as our sky chest
        if (size() <= 54) {
            return addItemSlow(stack, simulate);
        } else {
            return addItemFast(stack, simulate);
        }
    }

    /**
     * This version of {@link #addItems} will try to stack items before adding them to empty slots.
     */
    private ItemStack addItemSlow(ItemStack stack, boolean simulate) {
        var remainder = stack.copy();

        for (int pass = 0; pass < 2; pass++) {
            boolean fillEmptySlots = pass == 1;

            for (int slot = 0; slot < size(); slot++) {
                if (getStackInSlot(slot).isEmpty() == fillEmptySlots) {
                    remainder = insertItem(slot, remainder, simulate);
                }
                if (remainder.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        return remainder;
    }

    /**
     * This version of {@link #addItems} will try to add items to the first suitable slots in the inventory.
     */
    private ItemStack addItemFast(ItemStack stack, boolean simulate) {
        var remainder = stack.copy();

        for (int slot = 0; slot < size(); slot++) {
            remainder = insertItem(slot, remainder, simulate);
            if (remainder.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        return remainder;
    }

    default ItemStack removeItems(int amount, ItemStack filter, @Nullable Predicate<ItemStack> destination) {
        int slots = size();
        ItemStack rv = ItemStack.EMPTY;

        for (int slot = 0; slot < slots && amount > 0; slot++) {
            final ItemStack is = getStackInSlot(slot);
            if (is.isEmpty() || !filter.isEmpty() && !ItemStack.isSameItemSameTags(is, filter)) {
                continue;
            }

            if (destination != null) {
                ItemStack extracted = extractItem(slot, amount, true);
                if (extracted.isEmpty()) {
                    continue;
                }

                if (!destination.test(extracted)) {
                    continue;
                }
            }

            // Attempt extracting it
            ItemStack extracted = extractItem(slot, amount, false);

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

    default ItemStack simulateRemove(int amount, ItemStack filter, Predicate<ItemStack> destination) {
        int slots = size();
        ItemStack rv = ItemStack.EMPTY;

        for (int slot = 0; slot < slots && amount > 0; slot++) {
            final ItemStack is = getStackInSlot(slot);
            if (!is.isEmpty() && (filter.isEmpty() || ItemStack.isSameItemSameTags(is, filter))) {
                ItemStack extracted = extractItem(slot, amount, true);

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
    default ItemStack removeSimilarItems(int amount, ItemStack filter, FuzzyMode fuzzyMode,
            Predicate<ItemStack> destination) {
        int slots = size();
        ItemStack extracted = ItemStack.EMPTY;

        for (int slot = 0; slot < slots && extracted.isEmpty(); slot++) {
            final ItemStack is = getStackInSlot(slot);
            if (is.isEmpty() || !filter.isEmpty() && !isFuzzyEqualItem(is, filter, fuzzyMode)) {
                continue;
            }

            if (destination != null) {
                ItemStack simulated = extractItem(slot, amount, true);
                if (simulated.isEmpty()) {
                    continue;
                }

                if (!destination.test(simulated)) {
                    continue;
                }
            }

            // Attempt extracting it
            extracted = extractItem(slot, amount, false);
        }

        return extracted;
    }

    default ItemStack simulateSimilarRemove(int amount, ItemStack filter,
            FuzzyMode fuzzyMode,
            Predicate<ItemStack> destination) {
        int slots = size();
        ItemStack extracted = ItemStack.EMPTY;

        for (int slot = 0; slot < slots && extracted.isEmpty(); slot++) {
            final ItemStack is = getStackInSlot(slot);
            if (is.isEmpty() || !filter.isEmpty() && !isFuzzyEqualItem(is, filter, fuzzyMode)) {
                continue;
            }

            // Attempt extracting it
            extracted = extractItem(slot, amount, true);

            if (!extracted.isEmpty() && destination != null && !destination.test(extracted)) {
                extracted = ItemStack.EMPTY; // Keep on looking...
            }
        }

        return extracted;
    }

    /**
     * Similar to {@link ItemStack#isSameItemSameTags}, but it can further check, if both are equal considering a
     * {@link FuzzyMode}.
     *
     * @param mode how to compare the two {@link ItemStack}s
     * @return true, if both are matching the mode
     */
    private boolean isFuzzyEqualItem(ItemStack a, ItemStack b, FuzzyMode mode) {
        if (a.isEmpty() && b.isEmpty()) {
            return true;
        }

        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }

        // test damageable items..
        if (a.getItem() == b.getItem() && a.getItem().canBeDepleted()) {
            if (mode == FuzzyMode.IGNORE_ALL) {
                return true;
            } else if (mode == FuzzyMode.PERCENT_99) {
                return a.getDamageValue() > 1 == b.getDamageValue() > 1;
            } else {
                final float percentDamagedOfA = (float) a.getDamageValue() / a.getMaxDamage();
                final float percentDamagedOfB = (float) b.getDamageValue() / b.getMaxDamage();

                return percentDamagedOfA > mode.breakPoint == percentDamagedOfB > mode.breakPoint;
            }
        }

        return a.sameItem(b);
    }

    /**
     * @return The overflow
     */
    @Nonnull
    default ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        Preconditions.checkArgument(slot >= 0 && slot < size(), "slot out of range");

        if (stack.isEmpty() || !isItemValid(slot, stack)) {
            return stack;
        }

        var inSlot = getStackInSlot(slot);
        if (!inSlot.isEmpty() && !ItemStack.isSameItemSameTags(inSlot, stack)) {
            return stack;
        }

        // Calculate how much free space there is in the targeted slot, considering
        // an item-dependent maximum stack size, as well as a potential slot-based limit
        int maxSpace = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        int freeSpace = maxSpace - inSlot.getCount();
        if (freeSpace <= 0) {
            return stack;
        }

        var insertAmount = Math.min(stack.getCount(), freeSpace);
        if (!simulate) {
            var newItem = inSlot.isEmpty() ? stack.copy() : inSlot.copy();
            newItem.setCount(inSlot.getCount() + insertAmount);
            setItemDirect(slot, newItem);
        }

        if (freeSpace >= stack.getCount()) {
            return ItemStack.EMPTY;
        } else {
            var r = stack.copy();
            r.shrink(insertAmount);
            return r;
        }
    }

    @Nonnull
    default ItemStack extractItem(int slot, int amount, boolean simulate) {
        var item = getStackInSlot(slot);
        if (item.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (amount >= item.getCount()) {
            if (!simulate) {
                setItemDirect(slot, ItemStack.EMPTY);
                return item;
            } else {
                return item.copy();
            }
        } else {
            var result = item.copy();
            result.setCount(amount);

            if (!simulate) {
                var reduced = item.copy();
                reduced.shrink(amount);
                setItemDirect(slot, reduced);
            }
            return result;
        }
    }

    @Override
    default boolean mayAllowTransfer() {
        return size() > 0;
    }

}
