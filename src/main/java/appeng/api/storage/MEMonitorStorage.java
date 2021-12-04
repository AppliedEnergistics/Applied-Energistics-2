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

import appeng.api.stacks.KeyCounter;

/**
 * A {@link MEStorage} that allows listening for storage changes.
 */
public interface MEMonitorStorage extends MEStorage {
    /**
     * add a new Listener to the monitor, be sure to properly remove yourself when your done.
     */
    void addListener(IMEMonitorListener l, Object verificationToken);

    /**
     * remove a Listener to the monitor.
     */
    void removeListener(IMEMonitorListener l);

    /**
     * This method is discouraged when accessing data via a IMEMonitor
     */
    @Override
    @Deprecated
    void getAvailableStacks(KeyCounter out);

    /**
     * Returns the cached list of stacks available from this inventory, which will be equal to the result of
     * {@link #getAvailableStacks()} if the cache isn't stale.
     *
     * @return The cached contents of this monitor. Does not return a copy. <strong>Do not modify!</strong>
     */
    default KeyCounter getCachedAvailableStacks() {
        return getAvailableStacks();
    }
}
