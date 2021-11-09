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

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;

/**
 * Implementations of this interface provide AE2 with a way to interact with storage cells that may be represented by
 * arbitrary {@link ItemStack}
 *
 * @see appeng.api.storage.StorageCells
 */
public interface ICellHandler {

    /**
     * return true if the provided item is handled by your cell handler. ( AE May choose to skip this method, and just
     * request a handler )
     *
     * @param is to be checked item
     * @return return true, if getCellHandler will not return null.
     */
    boolean isCell(ItemStack is);

    /**
     * Returns the cell's inventory or null if the item stack is not handled by this handler, or it currently doesn't
     * want to act as a storage cell.
     *
     * @param is   a storage cell item.
     * @param host anytime the contents of your storage cell changes it should use this to request a save, please note,
     *             this value can be null. If provided, the host is responsible for persisting the cell content.
     * @return The cell inventory or null if the stack is not a cell supported by this handler.
     */
    @Nullable
    ICellInventory<?> getCellInventory(ItemStack is, @Nullable ISaveProvider host);

}
