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

import org.jetbrains.annotations.Nullable;

import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.IConfigurableObject;

public interface ITerminalHost extends IUpgradeableObject, IConfigurableObject, ISubMenuHost {
    /**
     * Please note that this will only be called <strong>once</strong> when the menu is opened. If the inventory of this
     * terminal host can change during its lifecycle, you need to return a {@link SupplierStorage}.
     */
    MEStorage getInventory();

    /**
     * For hosts that do not have a fixed connection to the grid, this method is used to give feedback to the player
     * about the current connection status.
     */
    ILinkStatus getLinkStatus();

    /**
     * An optional hotkey used to close the terminal while its open.
     *
     * @return Hotkey id as it would be registered by {@link appeng.client.Hotkeys}, or null if there isn't one
     */
    @Nullable
    default String getCloseHotkey() {
        return null;
    }
}
