/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
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

package appeng.api.storage;

import java.util.Objects;

import com.google.common.base.Preconditions;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;

/**
 * AE's Equivalent to IInventory, used to reading contents, and manipulating contents of ME Inventories.
 * <p>
 * Implementations should COMPLETELY ignore stack size limits from an external view point, Meaning that you can inject
 * Integer.MAX_VALUE items and it should work as defined, or be able to extract Integer.MAX_VALUE and have it work as
 * defined, Translations to MC's max stack size are external to the AE API.
 * <p/>
 * If you want to request at most a stack of an item, you need to use {@link ItemStack#getMaxStackSize()} before
 * extracting from this inventory.
 */
public interface MEStorage {
    /**
     * Returns whether this inventory is the preferred storage location for the given stack when being compared to other
     * inventories of the same overall priority.
     * <p/>
     * If for example an inventory already contains some amount of an item, it should be preferred over other
     * inventories that don't when trying to store more of the item.
     *
     * @param source The source trying to find storage for stacks.
     */
    default boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return false;
    }

    /**
     * Store new items, or simulate the addition of new items into the ME Inventory.
     *
     * @param what   what to insert
     * @param amount how much of it to insert. must not be negative
     * @param mode   action type
     * @return returns the number of items inserted.
     */
    default long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        return 0;
    }

    /**
     * Extract the specified item from the ME Inventory
     *
     * @param what   what to extract
     * @param amount how much of it to extract (at most)
     * @param mode   simulate, or perform action?
     * @return returns the number of items extracted
     */
    default long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        return 0;
    }

    /**
     * request a full report of all available items, storage.
     *
     * @param out The amounts for all available keys will be added to this tally.
     */
    default void getAvailableStacks(KeyCounter out) {
    }

    /**
     * @return The type of storage represented by this object.
     */
    Component getDescription();

    /**
     * request a full report of all available items, storage.
     *
     * @return a new list of this inventories content
     */
    default KeyCounter getAvailableStacks() {
        var result = new KeyCounter();
        getAvailableStacks(result);
        return result;
    }

    static void checkPreconditions(AEKey what, long amount, Actionable mode, IActionSource source) {
        Objects.requireNonNull(what, "Cannot pass a null key");
        Objects.requireNonNull(mode, "Cannot pass a null mode");
        Objects.requireNonNull(source, "Cannot pass a null source");
        Preconditions.checkArgument(amount >= 0, "Cannot pass a negative amount");
    }

}
