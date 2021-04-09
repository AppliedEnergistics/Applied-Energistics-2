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

package appeng.api.networking.crafting;

import appeng.api.crafting.ICraftingHelper;
import appeng.api.storage.data.IAEItemStack;

/**
 * Passed to a ICraftingProvider as a interface to manipulate the available crafting jobs.
 */
public interface ICraftingProviderHelper {

    /**
     * Add new Pattern to AE's crafting cache.
     * 
     * This will only accept instances created by
     * {@link ICraftingHelper#decodePattern(net.minecraft.item.ItemStack, net.minecraft.world.World)}
     */
    void addCraftingOption(ICraftingMedium medium, ICraftingPatternDetails api);

    /**
     * Set the passed stack to be "emittable".
     * The crafting system does not attempt to craft emittable items,
     * and assumes that they will be provided to the system when their crafting is requested,
     * i.e. when {@link ICraftingGrid#isRequesting} is {@code true}.
     *
     * This is used by level emitters when the "Emit redstone to craft item" option is enabled.
     */
    void setEmitable(IAEItemStack what);
}
