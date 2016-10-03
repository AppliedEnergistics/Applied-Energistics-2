package appeng.integration.modules.jei;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;

import appeng.api.features.IInscriberRecipe;


class InscriberRecipeWrapper extends BlankRecipeWrapper
{

	private final IInscriberRecipe recipe;

	public InscriberRecipeWrapper( IInscriberRecipe recipe )
	{
		this.recipe = recipe;
	}

	@Override
	public void getIngredients( IIngredients ingredients )
	{
		List<List<ItemStack>> inputSlots = new ArrayList<>( 3 );
		inputSlots.add( Collections.singletonList( recipe.getTopOptional().orElse( null ) ) );
		inputSlots.add( recipe.getInputs() );
		inputSlots.add( Collections.singletonList( recipe.getBottomOptional().orElse( null ) ) );
		ingredients.setInputLists( ItemStack.class, inputSlots );

		ingredients.setOutput( ItemStack.class, recipe.getOutput() );
	}
}

