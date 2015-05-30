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

import com.google.common.base.Optional;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import cpw.mods.fml.relauncher.ReflectionHelper;
import uristqwerty.CraftGuide.CraftGuideLog;
import uristqwerty.CraftGuide.DefaultRecipeTemplate;
import uristqwerty.CraftGuide.RecipeGeneratorImplementation;
import uristqwerty.CraftGuide.api.ChanceSlot;
import uristqwerty.CraftGuide.api.CraftGuideAPIObject;
import uristqwerty.CraftGuide.api.ExtraSlot;
import uristqwerty.CraftGuide.api.ItemSlot;
import uristqwerty.CraftGuide.api.RecipeGenerator;
import uristqwerty.CraftGuide.api.RecipeProvider;
import uristqwerty.CraftGuide.api.RecipeTemplate;
import uristqwerty.CraftGuide.api.Slot;
import uristqwerty.CraftGuide.api.SlotType;
import uristqwerty.gui_craftguide.texture.DynamicTexture;
import uristqwerty.gui_craftguide.texture.TextureClip;

import appeng.api.AEApi;
import appeng.api.IAppEngApi;
import appeng.api.definitions.IBlocks;
import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.features.IGrinderEntry;
import appeng.api.features.IInscriberRecipe;
import appeng.api.recipes.IIngredient;
import appeng.integration.IIntegrationModule;
import appeng.recipes.game.ShapedRecipe;
import appeng.recipes.game.ShapelessRecipe;


public class CraftGuide extends CraftGuideAPIObject implements IIntegrationModule, RecipeProvider
{

	public static CraftGuide instance;

	private final Slot[] shapelessCraftingSlots = new ItemSlot[] { new ItemSlot( 3, 3, 16, 16 ), new ItemSlot( 21, 3, 16, 16 ), new ItemSlot( 39, 3, 16, 16 ), new ItemSlot( 3, 21, 16, 16 ), new ItemSlot( 21, 21, 16, 16 ), new ItemSlot( 39, 21, 16, 16 ), new ItemSlot( 3, 39, 16, 16 ), new ItemSlot( 21, 39, 16, 16 ), new ItemSlot( 39, 39, 16, 16 ), new ItemSlot( 59, 21, 16, 16, true ).setSlotType( SlotType.OUTPUT_SLOT ), };

	private final Slot[] craftingSlotsOwnBackground = new ItemSlot[] { new ItemSlot( 3, 3, 16, 16 ).drawOwnBackground(), new ItemSlot( 21, 3, 16, 16 ).drawOwnBackground(), new ItemSlot( 39, 3, 16, 16 ).drawOwnBackground(), new ItemSlot( 3, 21, 16, 16 ).drawOwnBackground(), new ItemSlot( 21, 21, 16, 16 ).drawOwnBackground(), new ItemSlot( 39, 21, 16, 16 ).drawOwnBackground(), new ItemSlot( 3, 39, 16, 16 ).drawOwnBackground(), new ItemSlot( 21, 39, 16, 16 ).drawOwnBackground(), new ItemSlot( 39, 39, 16, 16 ).drawOwnBackground(), new ItemSlot( 59, 21, 16, 16, true ).setSlotType( SlotType.OUTPUT_SLOT ).drawOwnBackground(), };

	private final Slot[] smallCraftingSlotsOwnBackground = new ItemSlot[] { new ItemSlot( 12, 12, 16, 16 ).drawOwnBackground(), new ItemSlot( 30, 12, 16, 16 ).drawOwnBackground(), new ItemSlot( 12, 30, 16, 16 ).drawOwnBackground(), new ItemSlot( 30, 30, 16, 16 ).drawOwnBackground(), new ItemSlot( 59, 21, 16, 16, true ).setSlotType( SlotType.OUTPUT_SLOT ).drawOwnBackground(), };

	private final Slot[] craftingSlots = new ItemSlot[] { new ItemSlot( 3, 3, 16, 16 ), new ItemSlot( 21, 3, 16, 16 ), new ItemSlot( 39, 3, 16, 16 ), new ItemSlot( 3, 21, 16, 16 ), new ItemSlot( 21, 21, 16, 16 ), new ItemSlot( 39, 21, 16, 16 ), new ItemSlot( 3, 39, 16, 16 ), new ItemSlot( 21, 39, 16, 16 ), new ItemSlot( 39, 39, 16, 16 ), new ItemSlot( 59, 21, 16, 16, true ).setSlotType( SlotType.OUTPUT_SLOT ), };

	private final Slot[] smallCraftingSlots = new ItemSlot[] { new ItemSlot( 12, 12, 16, 16 ), new ItemSlot( 30, 12, 16, 16 ), new ItemSlot( 12, 30, 16, 16 ), new ItemSlot( 30, 30, 16, 16 ), new ItemSlot( 59, 21, 16, 16, true ).setSlotType( SlotType.OUTPUT_SLOT ), };

	private static final Slot[] grinderSlots = new ItemSlot[] {
			new ItemSlot( 3, 21, 16, 16 ).drawOwnBackground(),
			new ItemSlot( 41, 21, 16, 16, true ).drawOwnBackground().setSlotType( SlotType.OUTPUT_SLOT ),
			new ChanceSlot( 59, 12, 16, 16, true).setRatio( 10000 ).setFormatString( " (%1$.2f%% chance)" ).drawOwnBackground().setSlotType( SlotType.OUTPUT_SLOT ),
			new ChanceSlot( 59, 30, 16, 16, true).setRatio( 10000 ).setFormatString( " (%1$.2f%% chance)" ).drawOwnBackground().setSlotType( SlotType.OUTPUT_SLOT ),
			new ItemSlot( 22, 12, 16, 16 ).setSlotType( SlotType.MACHINE_SLOT ),
			new ItemSlot( 22, 30, 16, 16 ).setSlotType( SlotType.MACHINE_SLOT ) };

	private static final Slot[] inscriberSlots = new ItemSlot[] {
			new ItemSlot( 12, 21, 16, 16 ).drawOwnBackground(),
			new ItemSlot( 21, 3, 16, 16 ).drawOwnBackground(),
			new ItemSlot( 21, 39, 16, 16 ).drawOwnBackground(),
			new ItemSlot( 50, 21, 16, 16, true ).drawOwnBackground().setSlotType( SlotType.OUTPUT_SLOT ),
			new ItemSlot( 31, 21, 16, 16 ).setSlotType( SlotType.MACHINE_SLOT ) };

	@Override
	public void generateRecipes( RecipeGenerator generator )
	{
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

		this.addCraftingRecipes( craftingTemplate, smallTemplate, shapelessTemplate, generator );

		final IAppEngApi api = AEApi.instance();
		final IBlocks aeBlocks = api.definitions().blocks();
		Optional<ItemStack> grindstone = aeBlocks.grindStone().maybeStack(1);
		Optional<ItemStack> inscriber = aeBlocks.inscriber().maybeStack(1);

		if( grindstone.isPresent() )
		{
			this.addGrinderRecipes( api, grindstone.get(), generator );
		}

		if( inscriber.isPresent() )
		{
			this.addInscriberRecipes( api, inscriber.get(), generator );
		}
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

				Object[] items = this.getCraftingRecipe( recipe, true );

				if( items == null )
				{
					continue;
				}
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
					CraftGuideLog.log( "AppEng CraftGuide integration: Stack trace limit reached, further stack traces from this invocation will not be logged to the console. They will still be logged to (.minecraft)/config/CraftGuide/CraftGuide.log", true );
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

	private void addGrinderRecipes( IAppEngApi api, ItemStack grindstone, RecipeGenerator generator )
	{
		ItemStack handle = api.definitions().blocks().crankHandle().maybeStack(1).orNull();
		RecipeTemplate grinderTemplate = generator.createRecipeTemplate( grinderSlots, grindstone );

		for( IGrinderEntry recipe : api.registries().grinder().getRecipes() )
		{
			generator.addRecipe( grinderTemplate, new Object[] {
					recipe.getInput(),
					recipe.getOutput(),
					new Object[] { recipe.getOptionalOutput(), (int)(recipe.getOptionalChance() * 10000) },
					new Object[] { recipe.getSecondOptionalOutput(), (int)(recipe.getOptionalChance() * 10000) },
					handle,
					grindstone
			});
		}
	}

	private void addInscriberRecipes( IAppEngApi api, ItemStack inscriber, RecipeGenerator generator )
	{
		RecipeTemplate inscriberTemplate = generator.createRecipeTemplate( inscriberSlots, inscriber );

		for( IInscriberRecipe recipe : api.registries().inscriber().getRecipes() )
		{
			generator.addRecipe( inscriberTemplate, new Object[] {
					recipe.getInputs(),
					recipe.getTopOptional().orNull(),
					recipe.getBottomOptional().orNull(),
					recipe.getOutput(),
					inscriber });
		}
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
	}

	@Override
	public void postInit()
	{

	}
}
