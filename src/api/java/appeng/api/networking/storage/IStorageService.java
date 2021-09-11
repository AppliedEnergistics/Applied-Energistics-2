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

import appeng.api.networking.IGridService;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.cells.ICellProvider;
import appeng.api.storage.data.IAEStack;

/**
 * Grid-wide storage services for all {@link IStorageChannel}.
 */
public interface IStorageService extends IGridService, IStorageMonitorable {

    /**
     * Used to inform the network of alterations to the storage system that fall outside of the standard Network
     * operations, Examples, ME Chest inputs from the world, or a Storage Bus detecting modifications made to the chest
     * by an outside force.
     *
     * Expects the input to have either a negative or a positive stack size to correspond to the injection, or
     * extraction operation.
     *
     * @param input injected items
     */
    <T extends IAEStack> void postAlterationOfStoredItems(IStorageChannel<T> chan,
            Iterable<T> input,
            IActionSource src);

    /**
     * Used to add an additional cell provider to the storage system, i.e. for adding global providers from grid
     * services.
     * <p/>
     * THIS IT NOT FOR USE BY {@link appeng.api.networking.IGridNode NODES} THAT PROVIDE THE {@link ICellProvider}
     * SERVICE. Those are automatically handled by the storage system.
     *
     * @param cc to be added cell provider
     */
    void registerAdditionalCellProvider(ICellProvider cc);

    /**
     * remove a provider added with {@link #registerAdditionalCellProvider(ICellProvider)}.
     */
    void unregisterAdditionalCellProvider(ICellProvider cc);
}
