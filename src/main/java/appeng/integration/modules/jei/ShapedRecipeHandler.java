package appeng.integration.modules.jei;


import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

import appeng.recipes.game.ShapedRecipe;


class ShapedRecipeHandler implements IRecipeHandler<ShapedRecipe>
{

	@Override
	public Class<ShapedRecipe> getRecipeClass()
	{
		return ShapedRecipe.class;
	}

	@Override
	public String getRecipeCategoryUid()
	{
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	public String getRecipeCategoryUid( ShapedRecipe recipe )
	{
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper( ShapedRecipe recipe )
	{
		return new ShapedRecipeWrapper( recipe );
	}

	@Override
	public boolean isRecipeValid( ShapedRecipe recipe )
	{
		return recipe.isEnabled();
	}
}
