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


import appeng.api.AEApi;
import appeng.api.features.IGrinderEntry;
import appeng.client.gui.implementations.GuiGrinder;
import appeng.core.localization.GuiText;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.recipe.TemplateRecipeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class NEIGrinderRecipeHandler extends TemplateRecipeHandler
{

	@Override
	public void loadTransferRects()
	{
		this.transferRects.add( new TemplateRecipeHandler.RecipeTransferRect( new Rectangle( 84, 23, 24, 18 ), "grindstone" ) );
	}

	@Override
	public void loadCraftingRecipes( final String outputId, final Object... results )
	{
		if( ( outputId.equals( "grindstone" ) ) && ( this.getClass() == NEIGrinderRecipeHandler.class ) )
		{
			for( final IGrinderEntry recipe : AEApi.instance().registries().grinder().getRecipes() )
			{
				final CachedGrindStoneRecipe cachedRecipe = new CachedGrindStoneRecipe( recipe );
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
	public void loadCraftingRecipes( final ItemStack result )
	{
		for( final IGrinderEntry recipe : AEApi.instance().registries().grinder().getRecipes() )
		{
			if( NEIServerUtils.areStacksSameTypeCrafting( recipe.getOutput(), result ) )
			{
				final CachedGrindStoneRecipe cachedRecipe = new CachedGrindStoneRecipe( recipe );
				cachedRecipe.computeVisuals();
				this.arecipes.add( cachedRecipe );
			}
		}
	}

	@Override
	public void loadUsageRecipes( final ItemStack ingredient )
	{
		for( final IGrinderEntry recipe : AEApi.instance().registries().grinder().getRecipes() )
		{
			final CachedGrindStoneRecipe cachedRecipe = new CachedGrindStoneRecipe( recipe );

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
		final ResourceLocation loc = new ResourceLocation( "appliedenergistics2", "textures/guis/grinder.png" );

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
	public void drawBackground( final int recipe )
	{
		GL11.glColor4f( 1, 1, 1, 1 );
		GuiDraw.changeTexture( this.getGuiTexture() );
		GuiDraw.drawTexturedModalRect( 40, 10, 75, 16 + 10, 90, 66 );
	}

	@Override
	public void drawForeground( final int recipe )
	{
		super.drawForeground( recipe );
		if( this.arecipes.size() > recipe )
		{
			final CachedRecipe cr = this.arecipes.get( recipe );
			if( cr instanceof CachedGrindStoneRecipe )
			{
				final CachedGrindStoneRecipe cachedRecipe = (CachedGrindStoneRecipe) cr;
				if( cachedRecipe.hasOptional )
				{
					final FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
					final int width = fr.getStringWidth( cachedRecipe.displayChance );
					fr.drawString( cachedRecipe.displayChance, ( 168 - width ) / 2, 5, 0 );
				}
				else
				{
					final FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
					final int width = fr.getStringWidth( GuiText.NoSecondOutput.getLocal() );
					fr.drawString( GuiText.NoSecondOutput.getLocal(), ( 168 - width ) / 2, 5, 0 );
				}
			}
		}
	}

	@Override
	public boolean hasOverlay( final GuiContainer gui, final Container container, final int recipe )
	{
		return false;
	}

	@Override
	public IRecipeOverlayRenderer getOverlayRenderer( final GuiContainer gui, final int recipe )
	{
		return null;
	}

	@Override
	public IOverlayHandler getOverlayHandler( final GuiContainer gui, final int recipe )
	{
		return null;
	}

	@Override
	public String getRecipeName()
	{
		return GuiText.GrindStone.getLocal();
	}

	private class CachedGrindStoneRecipe extends TemplateRecipeHandler.CachedRecipe
	{
		private final List<PositionedStack> ingredients;
		private final PositionedStack result;
		private String displayChance;
		private boolean hasOptional = false;

		public CachedGrindStoneRecipe( final IGrinderEntry recipe )
		{
			this.result = new PositionedStack( recipe.getOutput(), -30 + 107, 47 );
			this.ingredients = new ArrayList<PositionedStack>();

			final ItemStack optionalOutput = recipe.getOptionalOutput();
			final int optionalChancePercent = (int) ( recipe.getOptionalChance() * 100 );
			if( optionalOutput != null )
			{
				this.hasOptional = true;
				this.displayChance = String.format( GuiText.OfSecondOutput.getLocal(), optionalChancePercent );
				this.ingredients.add( new PositionedStack( optionalOutput, -30 + 107 + 18, 47 ) );
			}

			final ItemStack secondOptionalOutput = recipe.getSecondOptionalOutput();
			final int secondOptionalChancePercent = (int) ( recipe.getSecondOptionalChance() * 100 );
			if( secondOptionalOutput != null )
			{
				this.hasOptional = true;
				this.displayChance = String.format( GuiText.MultipleOutputs.getLocal(), optionalChancePercent, secondOptionalChancePercent );
				this.ingredients.add( new PositionedStack( secondOptionalOutput, -30 + 107 + 18 + 18, 47 ) );
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

		private void computeVisuals()
		{
			for( final PositionedStack p : this.ingredients )
			{
				p.generatePermutations();
			}
			this.result.generatePermutations();
		}
	}
}