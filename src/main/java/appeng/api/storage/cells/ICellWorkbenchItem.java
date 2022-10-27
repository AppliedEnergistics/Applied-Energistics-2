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

import net.minecraft.world.item.ItemStack;

import appeng.api.config.FuzzyMode;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.util.ConfigInventory;

public interface ICellWorkbenchItem extends IUpgradeableItem {
    /**
     * Determines whether or not the item should be treated as a cell and allow for configuration via a cell workbench.
     * By default, any such item with either a filtering or upgrade inventory is thus assumed to be editable.
     *
     * @param is item
     * @return true if the item should be editable in the cell workbench.
     */
    default boolean isEditable(ItemStack is) {
        return getConfigInventory(is).size() > 0 || getUpgrades(is).size() > 0;
    }

    /**
     * Used to extract, or mirror the contents of the work bench onto the cell.
     * <p>
     * This should not exceed 63 slots. Any more than that might cause issues.
     * <p>
     * onInventoryChange will be called when saving is needed.
     */
    default ConfigInventory getConfigInventory(ItemStack is) {
        return ConfigInventory.EMPTY_TYPES;
    }

    /**
     * @return the current fuzzy status.
     */
    FuzzyMode getFuzzyMode(ItemStack is);

    /**
     * sets the setting on the cell.
     */
    void setFuzzyMode(ItemStack is, FuzzyMode fzMode);
}
