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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jetbrains.annotations.Nullable;

import appeng.api.networking.IGrid;
import appeng.api.stacks.AEKey;

/**
 * Registry for additional {@link TerminalKeyFilter terminal key filters}.
 * <p>
 * All registered filters are combined using logical AND. Registration should happen during mod initialization. The
 * filter implementations themselves may use dynamic state, but affected open terminals must be refreshed through
 * {@link IStorageTerminalMenu#requestFullInventoryUpdate()} when that state changes.
 */
public final class TerminalKeyFilters {
    private static final List<TerminalKeyFilter> FILTERS = new CopyOnWriteArrayList<>();

    private TerminalKeyFilters() {
    }

    /**
     * Registers an additional terminal key filter.
     */
    public static void register(TerminalKeyFilter filter) {
        FILTERS.add(Objects.requireNonNull(filter, "filter"));
    }

    /**
     * Tests a key against all registered terminal key filters.
     * <p>
     * Addon terminals may use this method to honor the same filters as AE2's storage terminals.
     */
    public static boolean isVisible(@Nullable IGrid grid, AEKey key) {
        Objects.requireNonNull(key, "key");

        for (var filter : FILTERS) {
            if (!filter.isVisible(grid, key)) {
                return false;
            }
        }
        return true;
    }
}
