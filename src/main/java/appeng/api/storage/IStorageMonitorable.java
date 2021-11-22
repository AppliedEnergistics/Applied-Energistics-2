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

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.AEKey;

/**
 * Exposes the monitorable network inventories of a grid node that choses to export them. This interface can only be
 * obtained using Forge capabilities for {@link IStorageMonitorableAccessor}.
 */
public interface IStorageMonitorable {

    IMEMonitor getInventory();

    /**
     * @see MEStorage#insert
     */
    @SuppressWarnings("unchecked")
    default <T extends AEKey> long insert(T what, long amount, Actionable mode, IActionSource source) {
        return getInventory().insert(what, amount, mode, source);
    }

    /**
     * @see MEStorage#extract
     */
    @SuppressWarnings("unchecked")
    default <T extends AEKey> long extract(T what, long amount, Actionable mode, IActionSource source) {
        return getInventory().insert(what, amount, mode, source);
    }

    default long getStoredAmountCached(AEKey key) {
        return getInventory().getCachedAvailableStacks().get(key);
    }
}
