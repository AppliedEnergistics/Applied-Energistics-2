/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.integration.modules.jei;


import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import mezz.jei.api.ingredients.IIngredients;

import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.IIngredient;
import appeng.recipes.game.ShapelessRecipe;


class ShapelessRecipeWrapper implements IRecipeWrapper
{

	private final ShapelessRecipe recipe;

	public ShapelessRecipeWrapper( ShapelessRecipe recipe )
	{
		this.recipe = recipe;
	}

	@Override
	public void getIngredients( IIngredients ingredients )
	{
		List<Object> recipeInput = this.recipe.getInput();
		List<List<ItemStack>> inputs = new ArrayList<>( recipeInput.size() );

		for( Object inputObj : recipeInput )
		{
			if( inputObj instanceof IIngredient )
			{
				IIngredient ingredient = (IIngredient) inputObj;
				try
				{
					inputs.add( Lists.newArrayList( ingredient.getItemStackSet() ) );
				}
				catch( RegistrationError | MissingIngredientError registrationError )
				{
					throw new RuntimeException( "Unable to register recipe with JEI" );
				}
			}
		}

		ingredients.setInputLists( ItemStack.class, inputs );
		ingredients.setOutput( ItemStack.class, this.recipe.getRecipeOutput() );
	}
}
