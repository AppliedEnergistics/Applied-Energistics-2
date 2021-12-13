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

package appeng.api.networking.storage;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;

/**
 * Grid-wide storage services for all {@link AEKeyType}.
 */
public interface IStorageService extends IGridService {

    /**
     * @return The network inventory.
     */
    MEStorage getInventory();

    /**
     * Returns the cached list of stacks available from this inventory, <strong>updated at most once per tick</strong>.
     * Should be used when slightly outdated content is not a big deal. Preferred to
     * {@code getInventory().getAvailableStacks()} for performance reasons.
     *
     * @return The cached stacks of this network. Does not return a copy. <strong>Do not modify!</strong>
     */
    KeyCounter getCachedAvailableStacks();

    /**
     * Adds a {@link IStorageProvider} that is not associated with a specific {@link appeng.api.networking.IGridNode }.
     * This is for adding storage provided by {@link IGridService}s for examples.
     * <p/>
     * THIS IT NOT FOR USE BY {@link appeng.api.networking.IGridNode NODES} THAT PROVIDE THE {@link IStorageProvider}
     * SERVICE. Those are automatically handled by the storage system.
     *
     * @param cc to be added cell provider
     */
    void addGlobalStorageProvider(IStorageProvider cc);

    /**
     * Remove a provider added with {@link #addGlobalStorageProvider(IStorageProvider)}.
     */
    void removeGlobalStorageProvider(IStorageProvider cc);

    /**
     * Refreshes the storage mounts provided by a {@link IGridNode node} through its {@link IStorageProvider}.
     *
     * @throws IllegalArgumentException If the given node is not part of this grid, or did not provide
     *                                  {@link IStorageProvider}.
     */
    void refreshNodeStorageProvider(IGridNode node);

    /**
     * Refreshes the storage mounts provided by a global storage provider.
     *
     * @throws IllegalArgumentException If the given provider has not been {@link #addGlobalStorageProvider registered}.
     */
    void refreshGlobalStorageProvider(IStorageProvider provider);
}
