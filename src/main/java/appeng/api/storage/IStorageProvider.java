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

import appeng.api.networking.IGridNodeService;
import appeng.api.networking.IManagedGridNode;

/**
 * Allows you to provide storage to the grid's storage system. Implementations that are attached as grid node services
 * will be automatically picked up by the {@link appeng.api.networking.storage.IStorageService} when the node joins or
 * leaves the grid.
 * <p/>
 * {@link appeng.api.networking.storage.IStorageService#addGlobalStorageProvider(IStorageProvider)} can be used to add
 * additional cell providers to a grid. This is useful for storage provided grid-wide by a grid service, rather than an
 * individual machine.
 */
public interface IStorageProvider extends IGridNodeService {
    /**
     * Allow the cell provider to make inventories available to the network by mounting them.
     */
    void mountInventories(IStorageMounts storageMounts);

    /**
     * This convenience method can be used to request an update of the mounted storage by the storage provider. This
     * only works if the given managed grid node provides this service.
     */
    static void requestUpdate(IManagedGridNode managedNode) {
        var node = managedNode.getNode();
        if (node != null) {
            node.getGrid().getStorageService().refreshNodeStorageProvider(node);
        }
    }
}
