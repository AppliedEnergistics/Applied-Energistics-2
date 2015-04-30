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

package appeng.integration.modules;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import codechicken.nei.api.IStackPositioner;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerTooltipHandler;

import appeng.client.gui.AEBaseMEGui;
import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.integration.BaseModule;
import appeng.integration.abstraction.INEI;
import appeng.integration.modules.NEIHelpers.NEIAEShapedRecipeHandler;
import appeng.integration.modules.NEIHelpers.NEIAEShapelessRecipeHandler;
import appeng.integration.modules.NEIHelpers.NEICraftingHandler;
import appeng.integration.modules.NEIHelpers.NEIFacadeRecipeHandler;
import appeng.integration.modules.NEIHelpers.NEIGrinderRecipeHandler;
import appeng.integration.modules.NEIHelpers.NEIInscriberRecipeHandler;
import appeng.integration.modules.NEIHelpers.NEIWorldCraftingHandler;
import appeng.integration.modules.NEIHelpers.TerminalCraftingSlotFinder;


public class NEI extends BaseModule implements INEI, IContainerTooltipHandler
{

	public static NEI instance;

	Class<?> API;

	// recipe handler...
	Method registerRecipeHandler;
	Method registerUsageHandler;

	public NEI() throws ClassNotFoundException
	{
		this.testClassExistence( GuiContainerManager.class );
		this.testClassExistence( codechicken.nei.recipe.ICraftingHandler.class );
		this.testClassExistence( codechicken.nei.recipe.IUsageHandler.class );
		this.API = Class.forName( "codechicken.nei.api.API" );
	}

	@Override
	public void init() throws Throwable
	{
		this.registerRecipeHandler = this.API.getDeclaredMethod( "registerRecipeHandler", codechicken.nei.recipe.ICraftingHandler.class );
		this.registerUsageHandler = this.API.getDeclaredMethod( "registerUsageHandler", codechicken.nei.recipe.IUsageHandler.class );

		this.registerRecipeHandler( new NEIAEShapedRecipeHandler() );
		this.registerRecipeHandler( new NEIAEShapelessRecipeHandler() );
		this.registerRecipeHandler( new NEIInscriberRecipeHandler() );
		this.registerRecipeHandler( new NEIWorldCraftingHandler() );
		this.registerRecipeHandler( new NEIGrinderRecipeHandler() );

		if( AEConfig.instance.isFeatureEnabled( AEFeature.Facades ) && AEConfig.instance.isFeatureEnabled( AEFeature.enableFacadeCrafting ) )
		{
			this.registerRecipeHandler( new NEIFacadeRecipeHandler() );
		}

		// large stack tooltips
		GuiContainerManager.addTooltipHandler( this );

		// crafting terminal...
		Method registerGuiOverlay = this.API.getDeclaredMethod( "registerGuiOverlay", Class.class, String.class, IStackPositioner.class );
		Class IOverlayHandler = Class.forName( "codechicken.nei.api.IOverlayHandler" );
		Class DefaultOverlayHandler = NEICraftingHandler.class;

		Method registerGuiOverlayHandler = this.API.getDeclaredMethod( "registerGuiOverlayHandler", Class.class, IOverlayHandler, String.class );
		registerGuiOverlay.invoke( this.API, GuiCraftingTerm.class, "crafting", new TerminalCraftingSlotFinder() );
		registerGuiOverlay.invoke( this.API, GuiPatternTerm.class, "crafting", new TerminalCraftingSlotFinder() );

		Constructor DefaultOverlayHandlerConstructor = DefaultOverlayHandler.getConstructor( int.class, int.class );
		registerGuiOverlayHandler.invoke( this.API, GuiCraftingTerm.class, DefaultOverlayHandlerConstructor.newInstance( 6, 75 ), "crafting" );
		registerGuiOverlayHandler.invoke( this.API, GuiPatternTerm.class, DefaultOverlayHandlerConstructor.newInstance( 6, 75 ), "crafting" );
	}

	public void registerRecipeHandler( Object o ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		this.registerRecipeHandler.invoke( this.API, o );
		this.registerUsageHandler.invoke( this.API, o );
	}

	@Override
	public void postInit()
	{

	}

	@Override
	public void drawSlot( Slot s )
	{
		if( s == null )
		{
			return;
		}

		ItemStack stack = s.getStack();

		if( stack == null )
		{
			return;
		}

		Minecraft mc = Minecraft.getMinecraft();
		FontRenderer fontRenderer = mc.fontRenderer;
		int x = s.xDisplayPosition;
		int y = s.yDisplayPosition;

		GuiContainerManager.drawItems.renderItemAndEffectIntoGUI( fontRenderer, mc.getTextureManager(), stack, x, y );
		GuiContainerManager.drawItems.renderItemOverlayIntoGUI( fontRenderer, mc.getTextureManager(), stack, x, y, String.valueOf( stack.stackSize ) );
	}

	@Override
	public RenderItem setItemRender( RenderItem renderItem )
	{
		try
		{
			RenderItem ri = GuiContainerManager.drawItems;
			GuiContainerManager.drawItems = renderItem;
			return ri;
		}
		catch( Throwable t )
		{
			throw new IllegalStateException( "Invalid version of NEI, please update", t );
		}
	}

	@Override
	public List<String> handleTooltip( GuiContainer arg0, int arg1, int arg2, List<String> current )
	{
		return current;
	}

	@Override
	public List<String> handleItemDisplayName( GuiContainer arg0, ItemStack arg1, List<String> current )
	{
		return current;
	}

	@Override
	public List<String> handleItemTooltip( GuiContainer guiScreen, ItemStack stack, int mouseX, int mouseY, List<String> currentToolTip )
	{
		if( guiScreen instanceof AEBaseMEGui )
		{
			return ( (AEBaseMEGui) guiScreen ).handleItemTooltip( stack, mouseX, mouseY, currentToolTip );
		}

		return currentToolTip;
	}
}
