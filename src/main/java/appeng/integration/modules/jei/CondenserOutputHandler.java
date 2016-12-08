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


import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

import appeng.api.config.CondenserOutput;
import appeng.core.AppEng;


class CondenserOutputHandler implements IRecipeHandler<CondenserOutput>
{

	private final ItemStack matterBall;
	private final ItemStack singularity;
	private final IDrawable iconButtonMatterBall;
	private final IDrawable iconButtonSingularity;

	public CondenserOutputHandler( IGuiHelper guiHelper, ItemStack matterBall, ItemStack singularity )
	{
		this.matterBall = matterBall;
		this.singularity = singularity;

		ResourceLocation statesLocation = new ResourceLocation( AppEng.MOD_ID, "textures/guis/states.png" );
		this.iconButtonMatterBall = guiHelper.createDrawable( statesLocation, 16, 112, 14, 14, 28, 0, 78, 0 );
		this.iconButtonSingularity = guiHelper.createDrawable( statesLocation, 32, 112, 14, 14, 28, 0, 78, 0 );
	}

	@Override
	public Class<CondenserOutput> getRecipeClass()
	{
		return CondenserOutput.class;
	}

	@Override
	public String getRecipeCategoryUid( CondenserOutput recipe )
	{
		return CondenserCategory.UID;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper( CondenserOutput recipe )
	{
		switch( recipe )
		{
			case MATTER_BALLS:
				return new CondenserOutputWrapper( recipe, matterBall, iconButtonMatterBall );
			case SINGULARITY:
				return new CondenserOutputWrapper( recipe, singularity, iconButtonSingularity );
			default:
				return null;
		}
	}

	@Override
	public boolean isRecipeValid( CondenserOutput recipe )
	{
		switch( recipe )
		{
			case MATTER_BALLS:
				return matterBall != null;
			case SINGULARITY:
				return singularity != null;
			default:
				return false;
		}
	}
}
