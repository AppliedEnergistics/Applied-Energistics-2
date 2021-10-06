/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
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

package appeng.api.crafting;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;

import appeng.api.storage.data.IAEStack;

public interface IPatternDetailsHelper {
    void registerDecoder(IPatternDetailsDecoder decoder);

    boolean isEncodedPattern(ItemStack stack);

    @Nullable
    default IPatternDetails decodePattern(ItemStack stack, Level level) {
        return decodePattern(stack, level, false);
    }

    @Nullable
    IPatternDetails decodePattern(ItemStack stack, Level level, boolean autoRecovery);

    /**
     * Encodes a processing pattern which represents the ability to convert the given inputs into the given outputs
     * using some process external to the ME system.
     *
     * @param stack If null, a new item will be created to hold the encoded pattern. Otherwise the given item must
     *              already contains an encoded pattern that will be overwritten.
     * @param out   The first element is considered the primary output and must be present
     * @throws IllegalArgumentException If either in or out contain only empty ItemStacks, or no primary output
     * @return A new encoded pattern, or the given stack with the pattern encoded in it.
     */
    ItemStack encodeProcessingPattern(@Nullable ItemStack stack, IAEStack[] in, IAEStack[] out);

    /**
     * Encodes a crafting pattern which represents a Vanilla crafting recipe.
     *
     * @param stack            If null, a new item will be created to hold the encoded pattern. Otherwise the given item
     *                         must already contains an encoded pattern that will be overwritten.
     * @param recipe           The Vanilla crafting recipe to be encoded.
     * @param in               The items in the crafting grid, which are used to determine what items are supplied from
     *                         the ME system to craft using this pattern.
     * @param out              What is to be expected as the result of this crafting operation by the ME system.
     * @param allowSubstitutes Controls whether the ME system will allow the use of equivalent items to craft this
     *                         recipe.
     * @throws IllegalArgumentException If either in or out contain only empty ItemStacks.
     */
    ItemStack encodeCraftingPattern(@Nullable ItemStack stack, CraftingRecipe recipe, ItemStack[] in, ItemStack out,
            boolean allowSubstitutes);
}
