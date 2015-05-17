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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.ICraftingHandler;
import codechicken.nei.recipe.IUsageHandler;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;


public final class NEIWorldCraftingHandler implements ICraftingHandler, IUsageHandler
{

	private final Map<IItemDefinition, String> details = new HashMap<IItemDefinition, String>();
	private final List<IItemDefinition> offsets = new LinkedList<IItemDefinition>();
	private final List<PositionedStack> outputs = new LinkedList<PositionedStack>();

	private ItemStack target;

	@Override
	public final String getRecipeName()
	{
		return GuiText.InWorldCrafting.getLocal();
	}

	@Override
	public final int numRecipes()
	{
		return this.offsets.size();
	}

	@Override
	public final void drawBackground( int recipe )
	{
		GL11.glColor4f( 1, 1, 1, 1 );// nothing.
	}

	@Override
	public final void drawForeground( int recipe )
	{
		if( this.outputs.size() > recipe )
		{
			// PositionedStack cr = this.outputs.get( recipe );
			String details = this.details.get( this.offsets.get( recipe ) );

			FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
			fr.drawSplitString( details, 10, 25, 150, 0 );
		}
	}

	@Override
	public final List<PositionedStack> getIngredientStacks( int recipeIndex )
	{
		return new ArrayList<PositionedStack>();
	}

	@Override
	public final List<PositionedStack> getOtherStacks( int recipeIndex )
	{
		return new ArrayList<PositionedStack>();
	}

	@Override
	public final PositionedStack getResultStack( int recipe )
	{
		return this.outputs.get( recipe );
	}

	@Override
	public final void onUpdate()
	{

	}

	@Override
	public final boolean hasOverlay( GuiContainer gui, Container container, int recipe )
	{
		return false;
	}

	@Override
	public final IRecipeOverlayRenderer getOverlayRenderer( GuiContainer gui, int recipe )
	{
		return null;
	}

	@Override
	public final IOverlayHandler getOverlayHandler( GuiContainer gui, int recipe )
	{
		return null;
	}

	@Override
	public final int recipiesPerPage()
	{
		return 1;
	}

	@Override
	public final List<String> handleTooltip( GuiRecipe gui, List<String> currentToolTip, int recipe )
	{
		return currentToolTip;
	}

	@Override
	public final List<String> handleItemTooltip( GuiRecipe gui, ItemStack stack, List<String> currentToolTip, int recipe )
	{
		return currentToolTip;
	}

	@Override
	public final boolean keyTyped( GuiRecipe gui, char keyChar, int keyCode, int recipe )
	{
		return false;
	}

	@Override
	public final boolean mouseClicked( GuiRecipe gui, int button, int recipe )
	{
		return false;
	}

	@Override
	public final IUsageHandler getUsageHandler( String inputId, Object... ingredients )
	{
		return this;
	}

	@Override
	public final ICraftingHandler getRecipeHandler( String outputId, Object... results )
	{
		NEIWorldCraftingHandler g = this.newInstance();
		if( results.length > 0 && results[0] instanceof ItemStack )
		{
			g.target = (ItemStack) results[0];
			g.addRecipes();
			return g;
		}
		return this;
	}

	public final NEIWorldCraftingHandler newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch( InstantiationException e )
		{
			throw new IllegalStateException( e );
		}
		catch( IllegalAccessException e )
		{
			throw new IllegalStateException( e );
		}
	}

	private void addRecipes()
	{
		final IDefinitions definitions = AEApi.instance().definitions();
		final IMaterials materials = definitions.materials();

		final String message;
		if( AEConfig.instance.isFeatureEnabled( AEFeature.CertusQuartzWorldGen ) )
		{
			message = GuiText.ChargedQuartz.getLocal() + "\n\n" + GuiText.ChargedQuartzFind.getLocal();
		}
		else
		{
			message = GuiText.ChargedQuartzFind.getLocal();
		}

		this.addRecipe( materials.certusQuartzCrystalCharged(), message );

		if( AEConfig.instance.isFeatureEnabled( AEFeature.MeteoriteWorldGen ) )
		{
			this.addRecipe( materials.logicProcessorPress(), GuiText.inWorldCraftingPresses.getLocal() );
			this.addRecipe( materials.calcProcessorPress(), GuiText.inWorldCraftingPresses.getLocal() );
			this.addRecipe( materials.engProcessorPress(), GuiText.inWorldCraftingPresses.getLocal() );
		}

		if( AEConfig.instance.isFeatureEnabled( AEFeature.inWorldFluix ) )
		{
			this.addRecipe( materials.fluixCrystal(), GuiText.inWorldFluix.getLocal() );
		}

		if( AEConfig.instance.isFeatureEnabled( AEFeature.inWorldSingularity ) )
		{
			this.addRecipe( materials.qESingularity(), GuiText.inWorldSingularity.getLocal() );
		}

		if( AEConfig.instance.isFeatureEnabled( AEFeature.inWorldPurification ) )
		{
			this.addRecipe( materials.purifiedCertusQuartzCrystal(), GuiText.inWorldPurificationCertus.getLocal() );
			this.addRecipe( materials.purifiedNetherQuartzCrystal(), GuiText.inWorldPurificationNether.getLocal() );
			this.addRecipe( materials.purifiedFluixCrystal(), GuiText.inWorldPurificationFluix.getLocal() );
		}
	}

	private void addRecipe( IItemDefinition def, String msg )
	{
		for( ItemStack definitionStack : def.maybeStack( 1 ).asSet() )
		{
			if( NEIServerUtils.areStacksSameTypeCrafting( definitionStack, this.target ) )
			{
				this.offsets.add( def );
				this.outputs.add( new PositionedStack( definitionStack, 75, 4 ) );
				this.details.put( def, msg );
			}
		}
	}
}