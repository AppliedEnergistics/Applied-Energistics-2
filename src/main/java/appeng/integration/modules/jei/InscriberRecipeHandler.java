package appeng.integration.modules.jei;


import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

import appeng.api.features.IInscriberRecipe;


class InscriberRecipeHandler implements IRecipeHandler<IInscriberRecipe>
{

	@Override
	public Class<IInscriberRecipe> getRecipeClass()
	{
		return IInscriberRecipe.class;
	}

	@Override
	public String getRecipeCategoryUid()
	{
		return InscriberRecipeCategory.UID;
	}

	@Override
	public String getRecipeCategoryUid( IInscriberRecipe recipe )
	{
		return InscriberRecipeCategory.UID;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper( IInscriberRecipe recipe )
	{
		return new InscriberRecipeWrapper( recipe );
	}

	@Override
	public boolean isRecipeValid( IInscriberRecipe recipe )
	{
		return true;
	}

}
