/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2026 TeamAppliedEnergistics
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

package appeng.api.implementations.menus;

import org.jetbrains.annotations.Nullable;

import appeng.api.networking.IGrid;

/**
 * Exposes storage-terminal operations that are useful to addons without depending on AE2's concrete menu classes.
 */
public interface IStorageTerminalMenu {
    /**
     * @return The grid backing this terminal, or {@code null} on the client or if the terminal is not connected to a
     *         grid.
     */
    @Nullable
    IGrid getGrid();

    /**
     * Requests that the server resend the complete visible terminal inventory on the next menu update.
     * <p>
     * This is primarily useful after state used by a {@link TerminalKeyFilter} changes. Calling it does not modify ME
     * storage or invalidate server-side key serials.
     * <p>
     * A full update can be expensive for large inventories and should only be requested when necessary. This method may
     * only be called from the server thread.
     */
    void requestFullInventoryUpdate();
}
