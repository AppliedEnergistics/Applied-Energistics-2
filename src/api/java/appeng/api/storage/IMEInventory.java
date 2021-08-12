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

import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

/**
 * AE's Equivalent to IInventory, used to reading contents, and manipulating contents of ME Inventories.
 *
 * Implementations should COMPLETELY ignore stack size limits from an external view point, Meaning that you can inject
 * Integer.MAX_VALUE items and it should work as defined, or be able to extract Integer.MAX_VALUE and have it work as
 * defined, Translations to MC's max stack size are external to the AE API.
 * <p/>
 * If you want to request at most a stack of an item, you need to use {@link ItemStack#getMaxStackSize()} before
 * extracting from this inventory.
 */
public interface IMEInventory<T extends IAEStack<T>> {

    /**
     * Store new items, or simulate the addition of new items into the ME Inventory.
     *
     * @param input item to add.
     * @param type  action type
     * @param src   action source
     *
     * @return returns the number of items not added.
     */
    T injectItems(T input, Actionable type, IActionSource src);

    /**
     * Extract the specified item from the ME Inventory
     *
     * @param request item to request ( with stack size. )
     * @param mode    simulate, or perform action?
     *
     * @return returns the number of items extracted, null
     */
    T extractItems(T request, Actionable mode, IActionSource src);

    /**
     * request a full report of all available items, storage.
     *
     * @param out the IItemList the results will be written too
     *
     * @return returns same list that was passed in, is passed out
     */
    IItemList<T> getAvailableItems(IItemList<T> out);

    /**
     * request a full report of all available items, storage.
     *
     * @return a new list of this inventories content
     */
    default IItemList<T> getAvailableItems() {
        return getAvailableItems(getChannel().createList());
    }

    /**
     * @return the type of channel your handler should be part of
     */
    IStorageChannel<T> getChannel();

    /**
     * Convenience method to cast inventory handlers with wildcard generic types to the concrete type used by the given
     * storage channel, but only if the given storage channel is equal to {@link #getChannel()}.
     *
     * @throws IllegalArgumentException If channel is not equal to {@link #getChannel()}.
     */
    @SuppressWarnings("unchecked")
    default <SC extends IAEStack<SC>> IMEInventory<SC> cast(IStorageChannel<SC> channel) {
        if (getChannel() == channel) {
            return (IMEInventory<SC>) this;
        }
        throw new IllegalArgumentException("This inventories storage channel " + getChannel()
                + " is not compatible with " + channel);
    }

}
