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
import appeng.api.stacks.AEKey;

/**
 * Controls whether keys are shown in ME storage terminals.
 * <p>
 * This is a presentation-only filter. Returning {@code false} does not prevent a key from being stored, inserted,
 * extracted or otherwise accessed through the ME network. It must not be used as an access-control or security
 * mechanism.
 * <p>
 * Filters are evaluated on the server and may be called frequently. Implementations should be fast, side-effect free
 * and deterministic for their current state. When state that affects the result changes while a terminal is open, use
 * {@link IStorageTerminalMenu#requestFullInventoryUpdate()} to refresh affected terminal menus.
 */
@FunctionalInterface
public interface TerminalKeyFilter {
    /**
     * Tests whether a key should be shown in a storage terminal.
     *
     * @param grid The grid backing the terminal, or {@code null} for terminals that are not connected to a grid.
     * @param key  The key being considered for display.
     * @return {@code true} if the key should be shown.
     */
    boolean isVisible(@Nullable IGrid grid, AEKey key);
}
