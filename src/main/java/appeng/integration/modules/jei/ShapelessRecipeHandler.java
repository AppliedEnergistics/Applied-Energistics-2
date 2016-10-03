package appeng.integration.modules.jei;


import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

import appeng.recipes.game.ShapelessRecipe;


class ShapelessRecipeHandler implements IRecipeHandler<ShapelessRecipe>
{

	@Override
	public Class<ShapelessRecipe> getRecipeClass()
	{
		return ShapelessRecipe.class;
	}

	@Override
	public String getRecipeCategoryUid()
	{
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	public String getRecipeCategoryUid( ShapelessRecipe recipe )
	{
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper( ShapelessRecipe recipe )
	{
		return new ShapelessRecipeWrapper( recipe );
	}

	@Override
	public boolean isRecipeValid( ShapelessRecipe recipe )
	{
		return recipe.isEnabled();
	}
}
