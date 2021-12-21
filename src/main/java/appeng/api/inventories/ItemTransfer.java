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

import java.util.function.Predicate;

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

    ItemStack addItems(ItemStack stack, boolean simulate);

    /**
     * Heuristically determine if this transfer object allows inserting or extracting items, i.e. if it's a slot based
     * inventory and has no slots, it probably doesn't allow insertion or extraction.
     */
    boolean mayAllowTransfer();
}
