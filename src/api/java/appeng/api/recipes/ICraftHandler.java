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

package appeng.api.recipes;


import java.util.List;

import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RecipeError;
import appeng.api.exceptions.RegistrationError;


public interface ICraftHandler
{

	/**
	 * Called when your recipe handler receives a newly parsed list of inputs/outputs.
	 *
	 * @param input  parsed inputs
	 * @param output parsed outputs
	 *
	 * @throws RecipeError
	 */
	void setup( List<List<IIngredient>> input, List<List<IIngredient>> output ) throws RecipeError;

	/**
	 * called when all recipes are parsed, and your required to register your recipe.
	 *
	 * @throws RegistrationError
	 * @throws MissingIngredientError
	 */
	void register() throws RegistrationError, MissingIngredientError;
}
