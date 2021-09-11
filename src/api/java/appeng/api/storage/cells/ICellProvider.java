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

package appeng.api.storage.cells;

import java.util.List;

import javax.annotation.Nonnull;

import appeng.api.networking.IGridNodeService;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;

/**
 * Allows you to provide cells to the grid's storage system. Implementations that are attached as grid node services
 * will be automatically picked up by the {@link appeng.api.networking.storage.IStorageService} when the node joins or
 * leaves the grid.
 * <p/>
 * {@link appeng.api.networking.storage.IStorageService#registerAdditionalCellProvider(ICellProvider)} can be used to
 * add additional cell providers to a grid. This is useful for storage provided grid-wide by a grid service, rather than
 * an individual machine.
 */
public interface ICellProvider extends IGridNodeService {

    /**
     * List of inventories (i.e. one per cell) available for the given storage channel.
     *
     * @return a valid list of handlers
     */
    @Nonnull
    <T extends IAEStack> List<IMEInventoryHandler<T>> getCellArray(IStorageChannel<T> channel);

    /**
     * the storage's priority.
     *
     * Positive and negative are supported
     */
    int getPriority();
}
