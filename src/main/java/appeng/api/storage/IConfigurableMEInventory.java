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

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEStack;

/**
 * Thin logic layer that can be swapped with different IMEInventory implementations, used to handle features related to
 * storage, that are Separate from the storage medium itself.
 *
 * @param <T>
 */
public interface IConfigurableMEInventory<T extends IAEStack> extends IMEInventory<T> {

    /**
     * determine if items can be injected/extracted.
     *
     * @return the access
     */
    default AccessRestriction getAccess() {
        return AccessRestriction.READ_WRITE;
    }

    /**
     * determine if an item can be accepted and stored.
     *
     * @param input - item that might be added
     *
     * @return if the item can be added
     */
    default boolean canAccept(T input) {
        return true;
    }

    /**
     * determine what the priority of the inventory is.
     *
     * @return the priority, zero is default, positive and negative are supported.
     */
    default int getPriority() {
        return 0;
    }

    /**
     * AE will first attempt to insert items into inventories that already have the same type of item. This is done in
     * the first pass (pass=1), and uses a simulated {@link #extractItems(IAEStack, Actionable, IActionSource)
     * extraction} to find inventories with matching items.
     * <p/>
     * Any leftovers will then be inserted in a second pass where the current contents of the inventory are ignored.
     * It's possible to limit an inventory to participate only in the first or second pass by overriding this method.
     *
     * @param pass - pass number ( 1 or 2 )
     *
     * @return true, if this inventory should be considered for inserting items into the network during the given pass.
     */
    default boolean validForPass(int pass) {
        return true;
    }

    /**
     * Convenience method to cast inventory handlers with wildcard generic types to the concrete type used by the given
     * storage channel, but only if the given storage channel is equal to {@link #getChannel()}.
     *
     * @throws IllegalArgumentException If channel is not equal to {@link #getChannel()}.
     */
    @SuppressWarnings("unchecked")
    default <SC extends IAEStack> IConfigurableMEInventory<SC> cast(IStorageChannel<SC> channel) {
        if (getChannel() == channel) {
            return (IConfigurableMEInventory<SC>) this;
        }
        throw new IllegalArgumentException("The inventories storage channel " + getChannel()
                + " is not compatible with " + channel);
    }

}
