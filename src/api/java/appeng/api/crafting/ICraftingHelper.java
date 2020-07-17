/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 TeamAppliedEnergistics
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.world.World;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;

public interface ICraftingHelper {

    /**
     * Checks that the given item stack is an encoded pattern.
     */
    boolean isEncodedPattern(@Nullable IAEItemStack item);

    /**
     * Checks that the given item stack is an encoded pattern.
     */
    boolean isEncodedPattern(ItemStack item);

    /**
     * Encodes a processing pattern which represents the ability to convert the
     * given inputs into the given outputs using some process external to the ME
     * system.
     *
     * @param stack If null, a new item will be created to hold the encoded pattern.
     *              Otherwise the given item must already contains an encoded
     *              pattern that will be overwritten.
     * @return A new encoded pattern, or the given stack with the pattern encoded in
     *         it.
     */
    ItemStack encodeProcessingPattern(@Nullable ItemStack stack, ItemStack[] in, ItemStack[] out);

    /**
     * Encodes a crafting pattern which represents a Vanilla crafting recipe.
     *
     * @param stack            If null, a new item will be created to hold the
     *                         encoded pattern. Otherwise the given item must
     *                         already contains an encoded pattern that will be
     *                         overwritten.
     * @param recipe           The Vanilla crafting recipe to be encoded.
     * @param in               The items in the crafting grid, which are used to
     *                         determine what items are supplied from the ME system
     *                         to craft using this pattern.
     * @param out              What is to be expected as the result of this crafting
     *                         operation by the ME system.
     * @param allowSubstitutes Controls whether the ME system will allow the use of
     *                         equivalent items to craft this recipe.
     */
    ItemStack encodeCraftingPattern(@Nullable ItemStack stack, ICraftingRecipe recipe, ItemStack[] in, ItemStack out,
            boolean allowSubstitutes);

    /**
     * Same as {@link #decodePattern(ItemStack, World, boolean)} with no auto
     * recovery of changed recipe ids.
     */
    @Nullable
    default ICraftingPatternDetails decodePattern(@Nonnull ItemStack itemStack, @Nonnull World world) {
        return decodePattern(itemStack, world, false);
    }

    /**
     * Decodes an encoded crafting pattern and returns the pattern details.
     * <p>
     * The item backing the {@link ItemStack} needs to be an item returned by the
     * encode methods of this class.
     *
     * @param itemStack    pattern
     * @param world        world used to access the
     *                     {@link net.minecraft.item.crafting.RecipeManager}.
     * @param autoRecovery If true, the method will try to recover from changed
     *                     recipe ids by searching the entire recipe manager for a
     *                     recipe matching the inputs. If this is successful, the
     *                     given item stack will be changed to reflect the new
     *                     recipe id.
     * @return The pattern details if the pattern could be decoded. Otherwise null.
     */
    @Nullable
    ICraftingPatternDetails decodePattern(@Nonnull ItemStack itemStack, @Nonnull World world, boolean autoRecovery);

}
