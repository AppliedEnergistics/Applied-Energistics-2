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


import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;


/**
 * Builder for a grinder recipe.
 * 
 * The only default value provided are turns at a value of 8.
 *
 * @author yueh
 * @version rv5
 * @since rv5
 */
public interface IGrinderRecipeBuilder
{

	/**
	 * Creates an grinder recipe with inputs.
	 * Needs to be invoked.
	 *
	 * @param input new input for the recipe
	 *
	 * @return currently used builder
	 */
	@Nonnull
	IGrinderRecipeBuilder withInput( @Nonnull ItemStack input );

	/**
	 * Creates an grinder recipe with output.
	 * Needs to be invoked.
	 *
	 * @param output new output for the recipe
	 *
	 * @return currently used builder
	 */
	@Nonnull
	IGrinderRecipeBuilder withOutput( @Nonnull ItemStack output );

	/**
	 * Creates an grinder recipe with the first optional output and its chance.
	 *
	 * @param optional new first optional for the recipe
	 * @param chance chance for the first optional output, must be within 0.0 - 1.0
	 *
	 * @return currently used builder
	 */
	@Nonnull
	IGrinderRecipeBuilder withFirstOptional( @Nonnull ItemStack optional, float chance );

	/**
	 * Creates an grinder recipe with the second optional output and its chance.
	 *
	 * @param optional new second optional for the recipe
	 * @param chance chance for the second optional output, must be within 0.0 - 1.0
	 *
	 * @return currently used builder
	 */
	@Nonnull
	IGrinderRecipeBuilder withSecondOptional( @Nonnull ItemStack optional, float chance );

	/**
	 * Creates an grinder recipe with the amount of turns as cost.
	 * 
	 * Defaults to 8 when not called.
	 *
	 * @param turns new turns for the recipe, must be > 0
	 *
	 * @return currently used builder
	 */
	@Nonnull
	IGrinderRecipeBuilder withTurns( @Nonnegative int turns );

	/**
	 * Finalizes the process of making the recipe.
	 * Needs to be invoked to fetch grinder recipe.
	 *
	 * @return valid grinder recipe
	 *
	 * @throws IllegalStateException when input is not defined
	 * @throws IllegalStateException when input has no size
	 * @throws IllegalStateException when output is not defined
	 * @throws IllegalStateException when both optionals are not defined
	 * @throws IllegalStateException when process type is not defined
	 */
	@Nonnull
	IGrinderRecipe build();
}
