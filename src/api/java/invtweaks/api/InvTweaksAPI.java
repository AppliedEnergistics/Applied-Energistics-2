/*
 * Copyright (c) 2013 Andrew Crocker
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package invtweaks.api;

import net.minecraft.item.ItemStack;

/**
 * Interface to access functions exposed by Inventory Tweaks
 * <p/>
 * The main @Mod instance of the mod implements this interface, so a refernce to it can
 * be obtained via @Instance("inventorytweaks") or methods in cpw.mods.fml.common.Loader
 * <p/>
 * All of these functions currently have no effect if called on a dedicated server.
 */
public interface InvTweaksAPI {
    /**
     * Add a listener for ItemTree load events
     *
     * @param listener
     */
    void addOnLoadListener(IItemTreeListener listener);

    /**
     * Remove a listener for ItemTree load events
     *
     * @param listener
     * @return true if the listener was previously added
     */
    boolean removeOnLoadListener(IItemTreeListener listener);

    /**
     * Toggle sorting shortcut state.
     *
     * @param enabled
     */
    void setSortKeyEnabled(boolean enabled);

    /**
     * Toggle sorting shortcut supression.
     * Unlike setSortKeyEnabled, this flag is automatically cleared when GUIs are closed.
     *
     * @param enabled
     */
    void setTextboxMode(boolean enabled);

    /**
     * Compare two items using the default (non-rule based) algorithm,
     * sutable for an implementation of Comparator&lt;ItemStack&gt;.
     *
     * @param i
     * @param j
     * @return A value with a sign representing the relative order of the item stacks
     */
    int compareItems(ItemStack i, ItemStack j);
}
