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


import java.util.List;

import net.minecraft.item.ItemStack;


/**
 * Lets you manipulate Grinder Recipes, by adding or editing existing ones.
 */
public interface IGrinderRegistry
{

	/**
	 * Current list of registered recipes, you can modify this if you want too.
	 *
	 * @return currentlyRegisteredRecipes
	 */
	List<IGrinderEntry> getRecipes();

	/**
	 * add a new recipe the easy way, in &#8594; out, how many turns., duplicates will not be added.
	 *
	 * @param in    input
	 * @param out   output
	 * @param turns amount of turns to turn the input into the output
	 */
	void addRecipe( ItemStack in, ItemStack out, int turns );

	/**
	 * add a new recipe with optional outputs, duplicates will not be added.
	 *
	 * @param in       input
	 * @param out      output
	 * @param optional optional output
	 * @param chance   chance to get the optional output within 0.0 - 1.0
	 * @param turns    amount of turns to turn the input into the outputs
	 */
	void addRecipe( ItemStack in, ItemStack out, ItemStack optional, float chance, int turns );

	/**
	 * add a new recipe with optional outputs, duplicates will not be added.
	 *
	 * @param in        input
	 * @param out       output
	 * @param optional  optional output
	 * @param chance    chance to get the optional output within 0.0 - 1.0
	 * @param optional2 second optional output
	 * @param chance2   chance to get the second optional output within 0.0 - 1.0
	 * @param turns     amount of turns to turn the input into the outputs
	 */
	void addRecipe( ItemStack in, ItemStack out, ItemStack optional, float chance, ItemStack optional2, float chance2, int turns );

	/**
	 * Searches for a recipe for a given input, and returns it.
	 *
	 * @param input input
	 *
	 * @return identified recipe or null
	 */
	IGrinderEntry getRecipeForInput( ItemStack input );
}
