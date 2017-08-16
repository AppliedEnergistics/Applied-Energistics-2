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
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import scala.actors.threadpool.Arrays;

import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.IIngredient;
import appeng.core.AEConfig;
import appeng.recipes.game.ShapedRecipe;
import appeng.util.Platform;


class ShapedRecipeWrapper implements IShapedCraftingRecipeWrapper
{

	private final ShapedRecipe recipe;

	public ShapedRecipeWrapper( ShapedRecipe recipe )
	{
		this.recipe = recipe;
	}

	@Override
	public void getIngredients( IIngredients ingredients )
	{
		final boolean useSingleItems = AEConfig.instance().disableColoredCableRecipesInJEI();

		Object[] items = this.recipe.getIIngredients();
		int width = this.recipe.getWidth();
		int height = this.recipe.getHeight();

		List<List<ItemStack>> in = new ArrayList<>( width * height );

		for( int x = 0; x < width; x++ )
		{
			for( int y = 0; y < height; y++ )
			{
				if( items[( x * height + y )] != null )
				{
					final IIngredient ing = (IIngredient) items[( x * height + y )];

					List<ItemStack> slotList = Collections.emptyList();
					try
					{
						ItemStack[] is = ing.getItemStackSet();
						slotList = useSingleItems ? Platform.findPreferred( is ) : Arrays.asList( is );
					}
					catch( final RegistrationError | MissingIngredientError ignored )
					{
					}
					in.add( slotList );
				}
				else
				{
					in.add( Collections.emptyList() );
				}
			}
		}

		ingredients.setInputLists( ItemStack.class, in );
		ingredients.setOutput( ItemStack.class, this.recipe.getRecipeOutput() );
	}

	@Override
	public int getWidth()
	{
		return this.recipe.getWidth();
	}

	@Override
	public int getHeight()
	{
		return this.recipe.getHeight();
	}
}
