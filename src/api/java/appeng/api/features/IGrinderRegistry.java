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

package appeng.api.features;


import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;


/**
 * Lets you manipulate Grinder Recipes, by adding or editing existing ones.
 */
public interface IGrinderRegistry
{

	/**
	 * An immutable list of the currently registered recipes.
	 *
	 * @return currentlyRegisteredRecipes
	 */
	@Nonnull
	Collection<IGrinderRecipe> getRecipes();

	/**
	 * Add a new recipe with a single input and output and how many turns it requires.
	 * 
	 * Will ignore duplicate recipes with the same input item.
	 *
	 * @param in The {@link ItemStack} to grind.
	 * @param out The {@link ItemStack} to output.
	 * @param turns Amount of turns to turn the input into the output, with turns > 0.
	 */
	void addRecipe( @Nonnull ItemStack in, @Nonnull ItemStack out, int turns );

	/**
	 * Add a new recipe with an input, output and a single optional output.
	 * 
	 * Will ignore duplicate recipes with the same input item.
	 *
	 * @param in The {@link ItemStack} to grind.
	 * @param out The {@link ItemStack} to output.
	 * @param optional The optional {@link ItemStack} to output of a certain chance.
	 * @param chance Chance to get the optional output within 0.0 - 1.0
	 * @param turns Amount of turns to turn the input into the output, with turns > 0.
	 */
	void addRecipe( @Nonnull ItemStack in, @Nonnull ItemStack out, @Nonnull ItemStack optional, float chance, int turns );

	/**
	 * add a new recipe with optional outputs, duplicates will not be added.
	 * 
	 * Will ignore duplicate recipes with the same input item.
	 *
	 * @param in The {@link ItemStack} to grind.
	 * @param out The {@link ItemStack} to output.
	 * @param optional The first optional {@link ItemStack} to output of a certain chance.
	 * @param chance Chance to get the first optional output within 0.0 - 1.0
	 * @param optional2 The second optional {@link ItemStack} to output of a certain chance.
	 * @param chance2 chance to get the second optional output within 0.0 - 1.0
	 * @param turns Amount of turns to turn the input into the output, with turns > 0.
	 * 
	 */
	void addRecipe( @Nonnull ItemStack in, @Nonnull ItemStack out, @Nonnull ItemStack optional, float chance, @Nonnull ItemStack optional2, float chance2, int turns );

	/**
	 * Remove the specific from the recipe list.
	 * 
	 * @param recipe The recipe to be removed.
	 * @return true, if it was removed
	 */
	boolean removeRecipe( @Nonnull IGrinderRecipe recipe );

	/**
	 * Searches for a recipe for a given input, and returns it.
	 *
	 * @param input The {@link ItemStack} to be grinded.
	 *
	 * @return identified recipe or null
	 */
	@Nullable
	IGrinderRecipe getRecipeForInput( @Nonnull ItemStack input );

	/**
	 * Allows do add a custom ratio from an ore to dust when being grinded.
	 * 
	 * The default ratio is 1 ore to 2 dusts.
	 * 
	 * These have to be added before any recipe is registered. Otherwise it will use the default value.
	 * 
	 * @param oredictName The name of the ore;
	 * @param ratio The amount, must be > 0;
	 */
	void addDustRatio( @Nonnull String oredictName, int ratio );

	/**
	 * Remove a custom ratio for a specific ore name.
	 * 
	 * Will use the default of 2 value afterwards.
	 * 
	 * @param oredictName The name of the ore;
	 */
	boolean removeDustRatio( @Nonnull String oredictName );

}
