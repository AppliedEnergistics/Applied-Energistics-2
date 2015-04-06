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

package appeng.integration.modules.NEIHelpers;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.recipe.TemplateRecipeHandler;

import appeng.api.AEApi;
import appeng.api.features.IInscriberRecipe;
import appeng.client.gui.implementations.GuiInscriber;
import appeng.core.localization.GuiText;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class NEIInscriberRecipeHandler extends TemplateRecipeHandler
{

	@Override
	public void loadTransferRects()
	{
		this.transferRects.add( new TemplateRecipeHandler.RecipeTransferRect( new Rectangle( 84, 23, 24, 18 ), "inscriber" ) );
	}

	@Override
	public void loadCraftingRecipes( String outputId, Object... results )
	{
		if( ( outputId.equals( "inscriber" ) ) && ( this.getClass() == NEIInscriberRecipeHandler.class ) )
		{
			for( IInscriberRecipe recipe : AEApi.instance().registries().inscriber().getRecipes() )
			{
				CachedInscriberRecipe cachedRecipe = new CachedInscriberRecipe( recipe );
				cachedRecipe.computeVisuals();
				this.arecipes.add( cachedRecipe );
			}
		}
		else
		{
			super.loadCraftingRecipes( outputId, results );
		}
	}

	@Override
	public void loadCraftingRecipes( ItemStack result )
	{
		for( IInscriberRecipe recipe : AEApi.instance().registries().inscriber().getRecipes() )
		{
			if( NEIServerUtils.areStacksSameTypeCrafting( recipe.getOutput(), result ) )
			{
				CachedInscriberRecipe cachedRecipe = new CachedInscriberRecipe( recipe );
				cachedRecipe.computeVisuals();
				this.arecipes.add( cachedRecipe );
			}
		}
	}

	@Override
	public void loadUsageRecipes( ItemStack ingredient )
	{
		for( IInscriberRecipe recipe : AEApi.instance().registries().inscriber().getRecipes() )
		{
			CachedInscriberRecipe cachedRecipe = new CachedInscriberRecipe( recipe );

			if( ( cachedRecipe.contains( cachedRecipe.ingredients, ingredient.getItem() ) ) )
			{
				cachedRecipe.computeVisuals();
				if( cachedRecipe.contains( cachedRecipe.ingredients, ingredient ) )
				{
					cachedRecipe.setIngredientPermutation( cachedRecipe.ingredients, ingredient );
					this.arecipes.add( cachedRecipe );
				}
			}
		}
	}

	@Override
	public String getGuiTexture()
	{
		ResourceLocation loc = new ResourceLocation( "appliedenergistics2", "textures/guis/inscriber.png" );
		return loc.toString();
	}

	@Override
	public String getOverlayIdentifier()
	{
		return "inscriber";
	}

	@Override
	public Class<? extends GuiContainer> getGuiClass()
	{
		return GuiInscriber.class;
	}

	@Override
	public void drawBackground( int recipe )
	{
		GL11.glColor4f( 1, 1, 1, 1 );
		changeTexture( this.getGuiTexture() );
		drawTexturedModalRect( 0, 0, 5, 11, 166, 75 );
	}

	@Override
	public boolean hasOverlay( GuiContainer gui, Container container, int recipe )
	{
		return false;
	}

	@Override
	public IRecipeOverlayRenderer getOverlayRenderer( GuiContainer gui, int recipe )
	{
		return null;
	}

	@Override
	public IOverlayHandler getOverlayHandler( GuiContainer gui, int recipe )
	{
		return null;
	}

	@Override
	public String getRecipeName()
	{
		return GuiText.Inscriber.getLocal();
	}

	public class CachedInscriberRecipe extends TemplateRecipeHandler.CachedRecipe
	{

		public final ArrayList<PositionedStack> ingredients;
		public final PositionedStack result;

		public CachedInscriberRecipe( IInscriberRecipe recipe )
		{
			this.result = new PositionedStack( recipe.getOutput(), 108, 29 );
			this.ingredients = new ArrayList<PositionedStack>();

			for( ItemStack top : recipe.getTopOptional().asSet() )
			{
				this.ingredients.add( new PositionedStack( top, 40, 5 ) );
			}

			this.ingredients.add( new PositionedStack( recipe.getInputs(), 40 + 18, 28 ) );

			for( ItemStack bot : recipe.getBottomOptional().asSet() )
			{
				this.ingredients.add( new PositionedStack( bot, 40, 51 ) );
			}
		}

		@Override
		public PositionedStack getResult()
		{
			return this.result;
		}

		@Override
		public List<PositionedStack> getIngredients()
		{
			return this.getCycledIngredients( NEIInscriberRecipeHandler.this.cycleticks / 20, this.ingredients );
		}

		public void computeVisuals()
		{
			for( PositionedStack p : this.ingredients )
			{
				p.generatePermutations();
			}
			this.result.generatePermutations();
		}
	}
}