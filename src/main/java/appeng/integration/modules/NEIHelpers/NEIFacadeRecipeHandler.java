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
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;
import appeng.core.localization.GuiText;
import appeng.facade.IFacadeItem;
import appeng.items.parts.ItemFacade;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.DefaultOverlayRenderer;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.api.IStackPositioner;
import codechicken.nei.recipe.RecipeInfo;
import codechicken.nei.recipe.TemplateRecipeHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class NEIFacadeRecipeHandler extends TemplateRecipeHandler
{
	private final ItemFacade facade;
	private final IItemDefinition anchorDefinition;

	public NEIFacadeRecipeHandler()
	{
		final IDefinitions definitions = AEApi.instance().definitions();

		this.facade = (ItemFacade) definitions.items().facade().maybeItem().get();
		this.anchorDefinition = definitions.parts().cableAnchor();
	}

	@Override
	public void loadTransferRects()
	{
		this.transferRects.add( new TemplateRecipeHandler.RecipeTransferRect( new Rectangle( 84, 23, 24, 18 ), "crafting" ) );
	}

	@Override
	public void loadCraftingRecipes( final String outputId, final Object... results )
	{
		if( ( outputId.equals( "crafting" ) ) && ( this.getClass() == NEIFacadeRecipeHandler.class ) )
		{
			final List<ItemStack> facades = this.facade.getFacades();
			for( final ItemStack anchorStack : this.anchorDefinition.maybeStack( 1 ).asSet() )
			{
				for( final ItemStack is : facades )
				{
					final CachedShapedRecipe recipe = new CachedShapedRecipe( this.facade, anchorStack, is );
					recipe.computeVisuals();
					this.arecipes.add( recipe );
				}
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
		if( result.getItem() == this.facade )
		{
			for( final ItemStack anchorStack : this.anchorDefinition.maybeStack( 1 ).asSet() )
			{
				final CachedShapedRecipe recipe = new CachedShapedRecipe( this.facade, anchorStack, result );
				recipe.computeVisuals();
				this.arecipes.add( recipe );
			}
		}
	}

	@Override
	public void loadUsageRecipes( final ItemStack ingredient )
	{
		final List<ItemStack> facades = this.facade.getFacades();
		for( final ItemStack anchorStack : this.anchorDefinition.maybeStack( 1 ).asSet() )
		{
			for( final ItemStack is : facades )
			{
				final CachedShapedRecipe recipe = new CachedShapedRecipe( this.facade, anchorStack, is );

				if( recipe.contains( recipe.ingredients, ingredient.getItem() ) )
				{
					recipe.computeVisuals();
					if( recipe.contains( recipe.ingredients, ingredient ) )
					{
						recipe.setIngredientPermutation( recipe.ingredients, ingredient );
						this.arecipes.add( recipe );
					}
				}
			}
		}
	}

	@Override
	public String getGuiTexture()
	{
		return "textures/gui/container/crafting_table.png";
	}

	@Override
	public String getOverlayIdentifier()
	{
		return "crafting";
	}

	@Override
	public Class<? extends GuiContainer> getGuiClass()
	{
		return GuiCrafting.class;
	}

	@Override
	public boolean hasOverlay( final GuiContainer gui, final Container container, final int recipe )
	{
		return ( super.hasOverlay( gui, container, recipe ) ) || ( ( this.isRecipe2x2( recipe ) ) && ( RecipeInfo.hasDefaultOverlay( gui, "crafting2x2" ) ) );
	}

	@Override
	public IRecipeOverlayRenderer getOverlayRenderer( final GuiContainer gui, final int recipe )
	{
		final IRecipeOverlayRenderer renderer = super.getOverlayRenderer( gui, recipe );
		if( renderer != null )
		{
			return renderer;
		}

		final IStackPositioner positioner = RecipeInfo.getStackPositioner( gui, "crafting2x2" );
		if( positioner == null )
		{
			return null;
		}

		return new DefaultOverlayRenderer( this.getIngredientStacks( recipe ), positioner );
	}

	@Override
	public IOverlayHandler getOverlayHandler( final GuiContainer gui, final int recipe )
	{
		final IOverlayHandler handler = super.getOverlayHandler( gui, recipe );
		if( handler != null )
		{
			return handler;
		}

		return RecipeInfo.getOverlayHandler( gui, "crafting2x2" );
	}

	private boolean isRecipe2x2( final int recipe )
	{
		for( final PositionedStack stack : this.getIngredientStacks( recipe ) )
		{
			if( ( stack.relx > 43 ) || ( stack.rely > 24 ) )
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public String getRecipeName()
	{
		return GuiText.FacadeCrafting.getLocal();
	}

	private final class CachedShapedRecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public final List<PositionedStack> ingredients;
		public final PositionedStack result;

		public CachedShapedRecipe( final IFacadeItem facade, final ItemStack anchor, final ItemStack output )
		{
			output.stackSize = 4;
			this.result = new PositionedStack( output, 119, 24 );
			this.ingredients = new ArrayList<PositionedStack>();
			final ItemStack in = facade.getTextureItem( output );
			this.setIngredients( 3, 3, new Object[] { null, anchor, null, anchor, in, anchor, null, anchor, null } );
		}

		public void setIngredients( final int width, final int height, final Object[] items )
		{
			for( int x = 0; x < width; x++ )
			{
				for( int y = 0; y < height; y++ )
				{
					if( items[( y * width + x )] != null )
					{
						final ItemStack is = (ItemStack) items[( y * width + x )];
						final PositionedStack stack = new PositionedStack( is, 25 + x * 18, 6 + y * 18, false );
						stack.setMaxSize( 1 );
						this.ingredients.add( stack );
					}
				}
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
			return this.getCycledIngredients( NEIFacadeRecipeHandler.this.cycleticks / 20, this.ingredients );
		}

		public void computeVisuals()
		{
			for( final PositionedStack p : this.ingredients )
			{
				p.generatePermutations();
			}
			this.result.generatePermutations();
		}
	}
}