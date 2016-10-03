package appeng.integration.modules.jei;


import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;

import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.IIngredient;
import appeng.recipes.game.ShapelessRecipe;


class ShapelessRecipeWrapper extends BlankRecipeWrapper implements ICraftingRecipeWrapper
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
