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


import java.util.Arrays;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

import cpw.mods.fml.relauncher.ReflectionHelper;

import uristqwerty.CraftGuide.CraftGuideLog;
import uristqwerty.CraftGuide.DefaultRecipeTemplate;
import uristqwerty.CraftGuide.RecipeGeneratorImplementation;
import uristqwerty.CraftGuide.api.CraftGuideAPIObject;
import uristqwerty.CraftGuide.api.CraftGuideRecipe;
import uristqwerty.CraftGuide.api.ItemSlot;
import uristqwerty.CraftGuide.api.RecipeGenerator;
import uristqwerty.CraftGuide.api.RecipeProvider;
import uristqwerty.CraftGuide.api.RecipeTemplate;
import uristqwerty.CraftGuide.api.Slot;
import uristqwerty.CraftGuide.api.SlotType;
import uristqwerty.CraftGuide.api.StackInfo;
import uristqwerty.CraftGuide.api.StackInfoSource;
import uristqwerty.gui_craftguide.texture.DynamicTexture;
import uristqwerty.gui_craftguide.texture.TextureClip;

import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.IIngredient;
import appeng.integration.IIntegrationModule;
import appeng.recipes.game.ShapedRecipe;
import appeng.recipes.game.ShapelessRecipe;


public class CraftGuide extends CraftGuideAPIObject implements IIntegrationModule, RecipeProvider, StackInfoSource, RecipeGenerator
{

	public static CraftGuide instance;

	private final Slot[] shapelessCraftingSlots = new ItemSlot[] { new ItemSlot( 3, 3, 16, 16 ), new ItemSlot( 21, 3, 16, 16 ), new ItemSlot( 39, 3, 16, 16 ), new ItemSlot( 3, 21, 16, 16 ), new ItemSlot( 21, 21, 16, 16 ), new ItemSlot( 39, 21, 16, 16 ), new ItemSlot( 3, 39, 16, 16 ), new ItemSlot( 21, 39, 16, 16 ), new ItemSlot( 39, 39, 16, 16 ), new ItemSlot( 59, 21, 16, 16, true ).setSlotType( SlotType.OUTPUT_SLOT ), };

	private final Slot[] craftingSlotsOwnBackground = new ItemSlot[] { new ItemSlot( 3, 3, 16, 16 ).drawOwnBackground(), new ItemSlot( 21, 3, 16, 16 ).drawOwnBackground(), new ItemSlot( 39, 3, 16, 16 ).drawOwnBackground(), new ItemSlot( 3, 21, 16, 16 ).drawOwnBackground(), new ItemSlot( 21, 21, 16, 16 ).drawOwnBackground(), new ItemSlot( 39, 21, 16, 16 ).drawOwnBackground(), new ItemSlot( 3, 39, 16, 16 ).drawOwnBackground(), new ItemSlot( 21, 39, 16, 16 ).drawOwnBackground(), new ItemSlot( 39, 39, 16, 16 ).drawOwnBackground(), new ItemSlot( 59, 21, 16, 16, true ).setSlotType( SlotType.OUTPUT_SLOT ).drawOwnBackground(), };

	private final Slot[] smallCraftingSlotsOwnBackground = new ItemSlot[] { new ItemSlot( 12, 12, 16, 16 ).drawOwnBackground(), new ItemSlot( 30, 12, 16, 16 ).drawOwnBackground(), new ItemSlot( 12, 30, 16, 16 ).drawOwnBackground(), new ItemSlot( 30, 30, 16, 16 ).drawOwnBackground(), new ItemSlot( 59, 21, 16, 16, true ).setSlotType( SlotType.OUTPUT_SLOT ).drawOwnBackground(), };

	private final Slot[] craftingSlots = new ItemSlot[] { new ItemSlot( 3, 3, 16, 16 ), new ItemSlot( 21, 3, 16, 16 ), new ItemSlot( 39, 3, 16, 16 ), new ItemSlot( 3, 21, 16, 16 ), new ItemSlot( 21, 21, 16, 16 ), new ItemSlot( 39, 21, 16, 16 ), new ItemSlot( 3, 39, 16, 16 ), new ItemSlot( 21, 39, 16, 16 ), new ItemSlot( 39, 39, 16, 16 ), new ItemSlot( 59, 21, 16, 16, true ).setSlotType( SlotType.OUTPUT_SLOT ), };

	private final Slot[] smallCraftingSlots = new ItemSlot[] { new ItemSlot( 12, 12, 16, 16 ), new ItemSlot( 30, 12, 16, 16 ), new ItemSlot( 12, 30, 16, 16 ), new ItemSlot( 30, 30, 16, 16 ), new ItemSlot( 59, 21, 16, 16, true ).setSlotType( SlotType.OUTPUT_SLOT ), };

	private final Slot[] furnaceSlots = new ItemSlot[] { new ItemSlot( 13, 21, 16, 16 ), new ItemSlot( 50, 21, 16, 16, true ).setSlotType( SlotType.OUTPUT_SLOT ), };
	RecipeGenerator parent;

	@Override
	public String getInfo( ItemStack itemStack )
	{
		// :P
		return null;
	}

	@Override
	public void generateRecipes( RecipeGenerator generator )
	{
		this.parent = generator;

		RecipeTemplate craftingTemplate;
		RecipeTemplate smallTemplate;

		if( uristqwerty.CraftGuide.CraftGuide.newerBackgroundStyle )
		{
			craftingTemplate = generator.createRecipeTemplate( this.craftingSlotsOwnBackground, null );
			smallTemplate = generator.createRecipeTemplate( this.smallCraftingSlotsOwnBackground, null );
		}
		else
		{
			craftingTemplate = new DefaultRecipeTemplate( this.craftingSlots, RecipeGeneratorImplementation.workbench, new TextureClip( DynamicTexture.instance( "recipe_backgrounds" ), 1, 1, 79, 58 ), new TextureClip( DynamicTexture.instance( "recipe_backgrounds" ), 82, 1, 79, 58 ) );

			smallTemplate = new DefaultRecipeTemplate( this.smallCraftingSlots, RecipeGeneratorImplementation.workbench, new TextureClip( DynamicTexture.instance( "recipe_backgrounds" ), 1, 61, 79, 58 ), new TextureClip( DynamicTexture.instance( "recipe_backgrounds" ), 82, 61, 79, 58 ) );
		}

		RecipeTemplate shapelessTemplate = new DefaultRecipeTemplate( this.shapelessCraftingSlots, RecipeGeneratorImplementation.workbench, new TextureClip( DynamicTexture.instance( "recipe_backgrounds" ), 1, 121, 79, 58 ), new TextureClip( DynamicTexture.instance( "recipe_backgrounds" ), 82, 121, 79, 58 ) );

		RecipeTemplate furnaceTemplate = new DefaultRecipeTemplate( this.furnaceSlots, new ItemStack( Blocks.furnace ), new TextureClip( DynamicTexture.instance( "recipe_backgrounds" ), 1, 181, 79, 58 ), new TextureClip( DynamicTexture.instance( "recipe_backgrounds" ), 82, 181, 79, 58 ) );

		this.addCraftingRecipes( craftingTemplate, smallTemplate, shapelessTemplate, this );
		this.addGrinderRecipes( furnaceTemplate, this );
		this.addInscriberRecipes( furnaceTemplate, this );
	}

	private void addCraftingRecipes( RecipeTemplate template, RecipeTemplate templateSmall, RecipeTemplate templateShapeless, RecipeGenerator generator )
	{
		List recipes = CraftingManager.getInstance().getRecipeList();

		int errCount = 0;

		for( Object o : recipes )
		{
			try
			{
				IRecipe recipe = (IRecipe) o;

				Object[] items = generator.getCraftingRecipe( recipe, true );

				if( items.length == 5 )
				{
					generator.addRecipe( templateSmall, items );
				}
				else if( recipe instanceof ShapelessRecipe )
				{
					generator.addRecipe( templateShapeless, items );
				}
				else
				{
					generator.addRecipe( template, items );
				}
			}
			catch( Exception e )
			{
				if( errCount >= 5 )
				{
					CraftGuideLog.log( "CraftGuide DefaultRecipeProvider: Stack trace limit reached, further stack traces from this invocation will not be logged to the console. They will still be logged to (.minecraft)/config/CraftGuide/CraftGuide.log", true );
					errCount = -1;
				}
				else
				{
					e.printStackTrace();
				}
				errCount++;

				CraftGuideLog.log( e );
			}
		}
	}

	private void addGrinderRecipes( RecipeTemplate template, RecipeGenerator generator )
	{

	}

	private void addInscriberRecipes( RecipeTemplate template, RecipeGenerator generator )
	{

	}

	@Override
	public RecipeTemplate createRecipeTemplate( Slot[] slots, ItemStack craftingType )
	{
		return this.parent.createRecipeTemplate( slots, craftingType );
	}

	@Override
	public RecipeTemplate createRecipeTemplate( Slot[] slots, ItemStack craftingType, String backgroundTexture, int backgroundX, int backgroundY, int backgroundSelectedX, int backgroundSelectedY )
	{
		return this.parent.createRecipeTemplate( slots, craftingType, backgroundTexture, backgroundX, backgroundY, backgroundSelectedX, backgroundSelectedY );
	}

	@Override
	public RecipeTemplate createRecipeTemplate( Slot[] slots, ItemStack craftingType, String bgTexture, int bgX, int bgY, String selectedBGTexture, int selectedBGX, int selectedBGY )
	{
		return this.parent.createRecipeTemplate( slots, craftingType, bgTexture, bgX, bgY, selectedBGTexture, selectedBGX, selectedBGY );
	}

	@Override
	public void addRecipe( RecipeTemplate template, Object[] crafting )
	{
		this.parent.addRecipe( template, crafting );
	}

	@Override
	public void addRecipe( CraftGuideRecipe recipe, ItemStack craftingType )
	{
		this.parent.addRecipe( recipe, craftingType );
	}

	@Override
	public void setDefaultTypeVisibility( ItemStack type, boolean visible )
	{
		this.parent.setDefaultTypeVisibility( type, visible );
	}

	@Override
	public Object[] getCraftingRecipe( IRecipe recipe )
	{
		return this.getCraftingRecipe( recipe, true );
	}

	Object[] getCraftingShapelessRecipe( List items, ItemStack recipeOutput )
	{
		Object[] output = new Object[10];

		for( int i = 0; i < items.size(); i++ )
		{
			output[i] = items.get( i );

			if( output[i] instanceof ItemStack[] )
			{
				output[i] = Arrays.asList( (ItemStack[]) output[i] );
			}

			if( output[i] instanceof IIngredient )
			{
				try
				{
					output[i] = this.toCG( ( (IIngredient) output[i] ).getItemStackSet() );
				}
				catch( RegistrationError ignored )
				{

				}
				catch( MissingIngredientError ignored )
				{

				}
			}
		}

		output[9] = recipeOutput;
		return output;
	}

	Object[] getSmallShapedRecipe( int width, int height, Object[] items, ItemStack recipeOutput )
	{
		Object[] output = new Object[5];

		for( int y = 0; y < height; y++ )
		{
			for( int x = 0; x < width; x++ )
			{
				int i = y * 2 + x;
				output[i] = items[y * width + x];

				if( output[i] instanceof ItemStack[] )
				{
					output[i] = Arrays.asList( (ItemStack[]) output[i] );
				}

				if( output[i] instanceof IIngredient )
				{
					try
					{
						output[i] = this.toCG( ( (IIngredient) output[i] ).getItemStackSet() );
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

		output[4] = recipeOutput;
		return output;
	}

	Object[] getCraftingShapedRecipe( int width, int height, Object[] items, ItemStack recipeOutput )
	{
		Object[] output = new Object[10];

		for( int y = 0; y < height; y++ )
		{
			for( int x = 0; x < width; x++ )
			{
				int i = y * 3 + x;
				output[i] = items[y * width + x];

				if( output[i] instanceof ItemStack[] )
				{
					output[i] = Arrays.asList( (ItemStack[]) output[i] );
				}

				if( output[i] instanceof IIngredient )
				{
					try
					{
						output[i] = this.toCG( ( (IIngredient) output[i] ).getItemStackSet() );
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

		output[9] = recipeOutput;
		return output;
	}

	private Object toCG( ItemStack[] itemStackSet )
	{
		List<ItemStack> list = Arrays.asList( itemStackSet );

		for( int x = 0; x < list.size(); x++ )
		{
			list.set( x, list.get( x ).copy() );
			if( list.get( x ).stackSize == 0 )
			{
				list.get( x ).stackSize = 1;
			}
		}

		return list;
	}

	@Override
	public Object[] getCraftingRecipe( IRecipe recipe, boolean allowSmallGrid )
	{
		if( recipe instanceof ShapelessRecipe )
		{
			List items = ReflectionHelper.getPrivateValue( ShapelessRecipe.class, (ShapelessRecipe) recipe, "input" );
			return this.getCraftingShapelessRecipe( items, recipe.getRecipeOutput() );
		}
		else if( recipe instanceof ShapedRecipe )
		{
			int width = ReflectionHelper.getPrivateValue( ShapedRecipe.class, (ShapedRecipe) recipe, "width" );
			int height = ReflectionHelper.getPrivateValue( ShapedRecipe.class, (ShapedRecipe) recipe, "height" );
			Object[] items = ReflectionHelper.getPrivateValue( ShapedRecipe.class, (ShapedRecipe) recipe, "input" );

			if( allowSmallGrid && width < 3 && height < 3 )
			{
				return this.getSmallShapedRecipe( width, height, items, recipe.getRecipeOutput() );
			}
			else
			{
				return this.getCraftingShapedRecipe( width, height, items, recipe.getRecipeOutput() );
			}
		}

		return null;
	}

	@Override
	public void init() throws Throwable
	{
		StackInfo.addSource( this );
	}

	@Override
	public void postInit()
	{

	}
}
