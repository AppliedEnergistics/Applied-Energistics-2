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


import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.recipe.TemplateRecipeHandler;

import appeng.api.AEApi;
import appeng.api.features.IGrinderEntry;
import appeng.client.gui.implementations.GuiGrinder;
import appeng.core.localization.GuiText;


public class NEIGrinderRecipeHandler extends TemplateRecipeHandler
{

	@Override
	public void loadTransferRects()
	{
		this.transferRects.add( new TemplateRecipeHandler.RecipeTransferRect( new Rectangle( 84, 23, 24, 18 ), "grindstone" ) );
	}

	@Override
	public void loadCraftingRecipes( String outputId, Object... results )
	{
		if( ( outputId.equals( "grindstone" ) ) && ( this.getClass() == NEIGrinderRecipeHandler.class ) )
		{
			for( IGrinderEntry recipe : AEApi.instance().registries().grinder().getRecipes() )
			{
				CachedGrindStoneRecipe cachedRecipe = new CachedGrindStoneRecipe( recipe );
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
		for( IGrinderEntry recipe : AEApi.instance().registries().grinder().getRecipes() )
		{
			if( NEIServerUtils.areStacksSameTypeCrafting( recipe.getOutput(), result ) )
			{
				CachedGrindStoneRecipe cachedRecipe = new CachedGrindStoneRecipe( recipe );
				cachedRecipe.computeVisuals();
				this.arecipes.add( cachedRecipe );
			}
		}
	}

	@Override
	public void loadUsageRecipes( ItemStack ingredient )
	{
		for( IGrinderEntry recipe : AEApi.instance().registries().grinder().getRecipes() )
		{
			CachedGrindStoneRecipe cachedRecipe = new CachedGrindStoneRecipe( recipe );

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
		ResourceLocation loc = new ResourceLocation( "appliedenergistics2", "textures/guis/grinder.png" );
		return loc.toString();
	}

	@Override
	public String getOverlayIdentifier()
	{
		return "grindstone";
	}

	@Override
	public Class<? extends GuiContainer> getGuiClass()
	{
		return GuiGrinder.class;
	}

	@Override
	public void drawBackground( int recipe )
	{
		GL11.glColor4f( 1, 1, 1, 1 );
		GuiDraw.changeTexture( this.getGuiTexture() );
		GuiDraw.drawTexturedModalRect( 40, 10, 75, 16 + 10, 90, 66 );
	}

	@Override
	public void drawForeground( int recipe )
	{
		super.drawForeground( recipe );
		if( this.arecipes.size() > recipe )
		{
			CachedRecipe cr = this.arecipes.get( recipe );
			if( cr instanceof CachedGrindStoneRecipe )
			{
				CachedGrindStoneRecipe cachedRecipe = (CachedGrindStoneRecipe) cr;
				if( cachedRecipe.hasOptional )
				{
					FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
					int width = fr.getStringWidth( cachedRecipe.displayChance );
					fr.drawString( cachedRecipe.displayChance, ( 168 - width ) / 2, 5, 0 );
				}
				else
				{
					FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
					int width = fr.getStringWidth( GuiText.NoSecondOutput.getLocal() );
					fr.drawString( GuiText.NoSecondOutput.getLocal(), ( 168 - width ) / 2, 5, 0 );
				}
			}
		}
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
		return GuiText.GrindStone.getLocal();
	}

	public class CachedGrindStoneRecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public final List<PositionedStack> ingredients;
		public final PositionedStack result;
		public String displayChance;
		boolean hasOptional = false;

		public CachedGrindStoneRecipe( IGrinderEntry recipe )
		{
			this.result = new PositionedStack( recipe.getOutput(), -30 + 107, 47 );
			this.ingredients = new ArrayList<PositionedStack>();

			if( recipe.getOptionalOutput() != null )
			{
				final int chancePercent = (int) (recipe.getOptionalChance() * 100);

				this.hasOptional = true;
				this.displayChance = String.format( GuiText.OfSecondOutput.getLocal(), chancePercent );
				this.ingredients.add( new PositionedStack( recipe.getOptionalOutput(), -30 + 107 + 18, 47 ) );
			}

			if( recipe.getInput() != null )
			{
				this.ingredients.add( new PositionedStack( recipe.getInput(), 45, 24 ) );
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
			return this.getCycledIngredients( NEIGrinderRecipeHandler.this.cycleticks / 20, this.ingredients );
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