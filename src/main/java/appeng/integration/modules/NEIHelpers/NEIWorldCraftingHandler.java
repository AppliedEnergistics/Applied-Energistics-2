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
import appeng.api.definitions.IMaterials;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.ICraftingHandler;
import codechicken.nei.recipe.IUsageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.*;


public class NEIWorldCraftingHandler implements ICraftingHandler, IUsageHandler
{

	private final Map<IItemDefinition, String> details = new HashMap<IItemDefinition, String>();
	private final List<IItemDefinition> offsets = new LinkedList<IItemDefinition>();
	private final List<PositionedStack> outputs = new LinkedList<PositionedStack>();

	private ItemStack target;

	@Override
	public String getRecipeName()
	{
		return GuiText.InWorldCrafting.getLocal();
	}

	@Override
	public int numRecipes()
	{
		return this.offsets.size();
	}

	@Override
	public void drawBackground( final int recipe )
	{
		GL11.glColor4f( 1, 1, 1, 1 );// nothing.
	}

	@Override
	public void drawForeground( final int recipe )
	{
		if( this.outputs.size() > recipe )
		{
			// PositionedStack cr = this.outputs.get( recipe );
			final String details = this.details.get( this.offsets.get( recipe ) );

			final FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
			fr.drawSplitString( details, 10, 25, 150, 0 );
		}
	}

	@Override
	public List<PositionedStack> getIngredientStacks( final int recipeIndex )
	{
		return new ArrayList<PositionedStack>();
	}

	@Override
	public List<PositionedStack> getOtherStacks( final int recipeIndex )
	{
		return new ArrayList<PositionedStack>();
	}

	@Override
	public PositionedStack getResultStack( final int recipe )
	{
		return this.outputs.get( recipe );
	}

	@Override
	public void onUpdate()
	{

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
	public int recipiesPerPage()
	{
		return 1;
	}

	@Override
	public List<String> handleTooltip( final GuiRecipe gui, final List<String> currentToolTip, final int recipe )
	{
		return currentToolTip;
	}

	@Override
	public List<String> handleItemTooltip( final GuiRecipe gui, final ItemStack stack, final List<String> currentToolTip, final int recipe )
	{
		return currentToolTip;
	}

	@Override
	public boolean keyTyped( final GuiRecipe gui, final char keyChar, final int keyCode, final int recipe )
	{
		return false;
	}

	@Override
	public boolean mouseClicked( final GuiRecipe gui, final int button, final int recipe )
	{
		return false;
	}

	@Override
	public IUsageHandler getUsageHandler( final String inputId, final Object... ingredients )
	{
		return this;
	}

	@Override
	public ICraftingHandler getRecipeHandler( final String outputId, final Object... results )
	{
		final NEIWorldCraftingHandler g = this.newInstance();
		if( results.length > 0 && results[0] instanceof ItemStack )
		{
			g.target = (ItemStack) results[0];
			g.addRecipes();
			return g;
		}
		return this;
	}

	private NEIWorldCraftingHandler newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch( final InstantiationException e )
		{
			throw new IllegalStateException( e );
		}
		catch( final IllegalAccessException e )
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

		if( AEConfig.instance.isFeatureEnabled( AEFeature.InWorldFluix ) )
		{
			this.addRecipe( materials.fluixCrystal(), GuiText.inWorldFluix.getLocal() );
		}

		if( AEConfig.instance.isFeatureEnabled( AEFeature.InWorldSingularity ) )
		{
			this.addRecipe( materials.qESingularity(), GuiText.inWorldSingularity.getLocal() );
		}

		if( AEConfig.instance.isFeatureEnabled( AEFeature.InWorldPurification ) )
		{
			this.addRecipe( materials.purifiedCertusQuartzCrystal(), GuiText.inWorldPurificationCertus.getLocal() );
			this.addRecipe( materials.purifiedNetherQuartzCrystal(), GuiText.inWorldPurificationNether.getLocal() );
			this.addRecipe( materials.purifiedFluixCrystal(), GuiText.inWorldPurificationFluix.getLocal() );
		}
	}

	private void addRecipe( final IItemDefinition def, final String msg )
	{
		for( final ItemStack definitionStack : def.maybeStack( 1 ).asSet() )
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