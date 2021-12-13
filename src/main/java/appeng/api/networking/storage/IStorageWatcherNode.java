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

import appeng.api.networking.IGridNodeService;
import appeng.api.networking.IStackWatcher;
import appeng.api.stacks.AEKey;

/**
 * A node that is notified of changes to the currently stored items in the network. Implementors should store the
 * watcher passed to {@link #updateWatcher} and use it to configure the watched stacks.
 */
public interface IStorageWatcherNode extends IGridNodeService {

    /**
     * provides the watcher for this host, for the current network, is called when the hot changes networks. You do not
     * need to clear your old watcher, its already been removed by the time this gets called.
     *
     * @param newWatcher stack watcher
     */
    void updateWatcher(IStackWatcher newWatcher);

    /**
     * Called when a watched item changes amounts.
     * 
     * @param what   What has changed
     * @param amount New amount in the network
     */
    void onStackChange(AEKey what, long amount);
}
