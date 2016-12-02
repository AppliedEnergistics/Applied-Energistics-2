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


import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

import appeng.api.features.IGrinderRecipe;


class GrinderRecipeHandler implements IRecipeHandler<IGrinderRecipe>
{

	@Override
	public Class<IGrinderRecipe> getRecipeClass()
	{
		return IGrinderRecipe.class;
	}

	@Override
	public String getRecipeCategoryUid()
	{
		return GrinderRecipeCategory.UID;
	}

	@Override
	public String getRecipeCategoryUid( IGrinderRecipe recipe )
	{
		return GrinderRecipeCategory.UID;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper( IGrinderRecipe recipe )
	{
		return new GrinderRecipeWrapper( recipe );
	}

	@Override
	public boolean isRecipeValid( IGrinderRecipe recipe )
	{
		return true;
	}
}
