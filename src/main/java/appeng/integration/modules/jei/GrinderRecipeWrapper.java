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


import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;

import appeng.api.features.IGrinderRecipe;


class GrinderRecipeWrapper extends BlankRecipeWrapper
{

	private final IGrinderRecipe recipe;

	GrinderRecipeWrapper( IGrinderRecipe recipe )
	{
		this.recipe = recipe;
	}

	@Override
	public void getIngredients( IIngredients ingredients )
	{
		ingredients.setInput( ItemStack.class, recipe.getInput() );
		List<ItemStack> outputs = new ArrayList<>( 3 );
		outputs.add( recipe.getOutput() );
		recipe.getOptionalOutput().ifPresent( itemStack -> outputs.add( itemStack ) );
		recipe.getSecondOptionalOutput().ifPresent( itemStack -> outputs.add( itemStack ) );
		ingredients.setOutputs( ItemStack.class, outputs );
	}

	@Override
	public void drawInfo( Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY )
	{

		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

		int x = 118;

		final float scale = 0.85f;
		final float invScale = 1 / scale;
		GlStateManager.scale( scale, scale, 1 );

		if( recipe.getOptionalOutput() != null )
		{
			String text = String.format( "%d%%", (int) ( recipe.getOptionalChance() * 100 ) );
			float width = fr.getStringWidth( text ) * scale;
			int xScaled = (int) Math.round( ( x + ( 18 - width ) / 2 ) * invScale );
			fr.drawString( text, xScaled, (int) ( 65 * invScale ), Color.gray.getRGB() );
			x += 18;
		}

		if( recipe.getSecondOptionalOutput() != null )
		{
			String text = String.format( "%d%%", (int) ( recipe.getSecondOptionalChance() * 100 ) );
			float width = fr.getStringWidth( text ) * scale;
			int xScaled = (int) Math.round( ( x + ( 18 - width ) / 2 ) * invScale );
			fr.drawString( text, xScaled, (int) ( 65 * invScale ), Color.gray.getRGB() );
		}

		GlStateManager.scale( invScale, invScale, 1 );
	}
}
