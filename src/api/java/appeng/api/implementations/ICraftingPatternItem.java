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

package appeng.api.implementations;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

import appeng.api.storage.data.IAEItemStack;

/**
 * Implemented by {@link Item} subclasses that represent encoded crafting patterns.
 * 
 */
public interface ICraftingPatternItem {

    /**
     * @param itemStack Providing the actual data.
     * 
     * @return the {@link ResourceLocation} for the related {@link IRecipe}
     */
    @Nullable
    ResourceLocation recipe(ItemStack itemStack);

    /**
     * Get the ingredients for an encoded crafting pattern represented similar to a 3x3 crafting inventory.
     * <p>
     * 
     * This can contain null entries for empty slots.
     * 
     * @param itemStack Providing the actual data.
     * 
     * @return a list of up to 9 {@link ItemStack}s as ingredients for a recipe.
     */
    @Nonnull
    List<IAEItemStack> ingredients(ItemStack itemStack);

    /**
     * As crafting pattern it is limited to 1, but will actually use the
     * {@link IRecipe} output. As no crafting pattern, it is limited to 3 entries
     * and will directly use these.
     * 
     * @param itemStack Providing the actual data.
     * 
     * @return a list of up to 1 or 3 {@link ItemStack}s produces as output
     */
    @Nonnull
    List<IAEItemStack> products(ItemStack itemStack);

    /**
     * If this is a crafting or processing pattern.
     * 
     * Crafting recipes will be validated against an existing {@link IRecipe}.
     * 
     * @param itemStack Providing the actual data.
     * 
     * @return true, if this should be a crafting pattern
     */
    boolean isCrafting(ItemStack itemStack);

    /**
     * This only applies to crafting patters as the {@link IRecipe} is the source
     * for alternative ingredients.
     * 
     * @param itemStack Providing the actual data.
     * 
     * @return true, if alternative ingredients should be considered
     */
    boolean allowsSubstitution(ItemStack itemStack);
}
