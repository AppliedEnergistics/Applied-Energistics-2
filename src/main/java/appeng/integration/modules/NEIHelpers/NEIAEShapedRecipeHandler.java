/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

import codechicken.nei.NEIClientUtils;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.DefaultOverlayRenderer;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.api.IStackPositioner;
import codechicken.nei.recipe.RecipeInfo;
import codechicken.nei.recipe.TemplateRecipeHandler;

import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.IIngredient;
import appeng.core.AEConfig;
import appeng.recipes.game.ShapedRecipe;
import appeng.util.Platform;


public final class NEIAEShapedRecipeHandler extends TemplateRecipeHandler
{

	@Override
	public final void loadTransferRects()
	{
		this.transferRects.add( new TemplateRecipeHandler.RecipeTransferRect( new Rectangle( 84, 23, 24, 18 ), "crafting" ) );
	}

	@Override
	public final void loadCraftingRecipes( String outputId, Object... results )
	{
		if( ( outputId.equals( "crafting" ) ) && ( this.getClass() == NEIAEShapedRecipeHandler.class ) )
		{
			List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
			for( IRecipe recipe : recipes )
			{
				if( ( recipe instanceof ShapedRecipe ) )
				{
					if( ( (ShapedRecipe) recipe ).isEnabled() )
					{
						CachedShapedRecipe cachedRecipe = new CachedShapedRecipe( (ShapedRecipe) recipe );
						cachedRecipe.computeVisuals();
						this.arecipes.add( cachedRecipe );
					}
				}
			}
		}
		else
		{
			super.loadCraftingRecipes( outputId, results );
		}
	}

	@Override
	public final void loadCraftingRecipes( ItemStack result )
	{
		List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
		for( IRecipe recipe : recipes )
		{
			if( ( recipe instanceof ShapedRecipe ) )
			{
				if( ( (ShapedRecipe) recipe ).isEnabled() && NEIServerUtils.areStacksSameTypeCrafting( recipe.getRecipeOutput(), result ) )
				{
					CachedShapedRecipe cachedRecipe = new CachedShapedRecipe( (ShapedRecipe) recipe );
					cachedRecipe.computeVisuals();
					this.arecipes.add( cachedRecipe );
				}
			}
		}
	}

	@Override
	public final void loadUsageRecipes( ItemStack ingredient )
	{
		List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
		for( IRecipe recipe : recipes )
		{
			if( ( recipe instanceof ShapedRecipe ) )
			{
				CachedShapedRecipe cachedRecipe = new CachedShapedRecipe( (ShapedRecipe) recipe );

				if( ( (ShapedRecipe) recipe ).isEnabled() && cachedRecipe.contains( cachedRecipe.ingredients, ingredient.getItem() ) )
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
	}

	@Override
	public final String getGuiTexture()
	{
		return "textures/gui/container/crafting_table.png";
	}

	@Override
	public final String getOverlayIdentifier()
	{
		return "crafting";
	}

	@Override
	public final Class<? extends GuiContainer> getGuiClass()
	{
		return GuiCrafting.class;
	}

	@Override
	public final boolean hasOverlay( GuiContainer gui, Container container, int recipe )
	{
		return ( super.hasOverlay( gui, container, recipe ) ) || ( ( this.isRecipe2x2( recipe ) ) && ( RecipeInfo.hasDefaultOverlay( gui, "crafting2x2" ) ) );
	}

	@Override
	public final IRecipeOverlayRenderer getOverlayRenderer( GuiContainer gui, int recipe )
	{
		IRecipeOverlayRenderer renderer = super.getOverlayRenderer( gui, recipe );
		if( renderer != null )
		{
			return renderer;
		}

		IStackPositioner positioner = RecipeInfo.getStackPositioner( gui, "crafting2x2" );
		if( positioner == null )
		{
			return null;
		}

		return new DefaultOverlayRenderer( this.getIngredientStacks( recipe ), positioner );
	}

	@Override
	public final IOverlayHandler getOverlayHandler( GuiContainer gui, int recipe )
	{
		IOverlayHandler handler = super.getOverlayHandler( gui, recipe );
		if( handler != null )
		{
			return handler;
		}

		return RecipeInfo.getOverlayHandler( gui, "crafting2x2" );
	}

	public final boolean isRecipe2x2( int recipe )
	{
		for( PositionedStack stack : this.getIngredientStacks( recipe ) )
		{
			if( ( stack.relx > 43 ) || ( stack.rely > 24 ) )
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public final String getRecipeName()
	{
		return NEIClientUtils.translate( "recipe.shaped" );
	}

	public final class CachedShapedRecipe extends TemplateRecipeHandler.CachedRecipe
	{

		public final List<PositionedStack> ingredients;
		public final PositionedStack result;

		public CachedShapedRecipe( ShapedRecipe recipe )
		{
			this.result = new PositionedStack( recipe.getRecipeOutput(), 119, 24 );
			this.ingredients = new ArrayList<PositionedStack>();
			this.setIngredients( recipe.getWidth(), recipe.getHeight(), recipe.getIngredients() );
		}

		public final void setIngredients( int width, int height, Object[] items )
		{
			boolean useSingleItems = AEConfig.instance.disableColoredCableRecipesInNEI();
			for( int x = 0; x < width; x++ )
			{
				for( int y = 0; y < height; y++ )
				{
					if( items[( y * width + x )] != null )
					{
						IIngredient ing = (IIngredient) items[( y * width + x )];

						try
						{
							ItemStack[] is = ing.getItemStackSet();
							PositionedStack stack = new PositionedStack( useSingleItems ? Platform.findPreferred( is ) : is, 25 + x * 18, 6 + y * 18, false );
							stack.setMaxSize( 1 );
							this.ingredients.add( stack );
						}
						catch( RegistrationError ignored )
						{

						}
						catch( MissingIngredientError ignored )
						{

						}
					}
				}
			}
		}

		@Override
		public final PositionedStack getResult()
		{
			return this.result;
		}

		@Override
		public final List<PositionedStack> getIngredients()
		{
			return this.getCycledIngredients( NEIAEShapedRecipeHandler.this.cycleticks / 20, this.ingredients );
		}

		public final void computeVisuals()
		{
			for( PositionedStack p : this.ingredients )
			{
				p.generatePermutations();
			}
			this.result.generatePermutations();
		}
	}
}