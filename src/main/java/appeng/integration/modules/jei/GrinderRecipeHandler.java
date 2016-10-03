package appeng.integration.modules.jei;


import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

import appeng.api.features.IGrinderEntry;


class GrinderRecipeHandler implements IRecipeHandler<IGrinderEntry>
{

	@Override
	public Class<IGrinderEntry> getRecipeClass()
	{
		return IGrinderEntry.class;
	}

	@Override
	public String getRecipeCategoryUid()
	{
		return GrinderRecipeCategory.UID;
	}

	@Override
	public String getRecipeCategoryUid( IGrinderEntry recipe )
	{
		return GrinderRecipeCategory.UID;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper( IGrinderEntry recipe )
	{
		return new GrinderRecipeWrapper( recipe );
	}

	@Override
	public boolean isRecipeValid( IGrinderEntry recipe )
	{
		return true;
	}
}
