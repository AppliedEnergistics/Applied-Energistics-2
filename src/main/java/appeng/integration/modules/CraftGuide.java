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

package appeng.integration.modules;


import appeng.api.AEApi;
import appeng.api.IAppEngApi;
import appeng.api.definitions.IBlocks;
import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.features.IGrinderEntry;
import appeng.api.features.IInscriberRecipe;
import appeng.api.recipes.IIngredient;
import appeng.helpers.Reflected;
import appeng.integration.IIntegrationModule;
import appeng.integration.IntegrationHelper;
import appeng.recipes.game.ShapedRecipe;
import appeng.recipes.game.ShapelessRecipe;
import com.google.common.base.Optional;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import uristqwerty.CraftGuide.CraftGuideLog;
import uristqwerty.CraftGuide.DefaultRecipeTemplate;
import uristqwerty.CraftGuide.RecipeGeneratorImplementation;
import uristqwerty.CraftGuide.api.*;
import uristqwerty.gui_craftguide.texture.DynamicTexture;
import uristqwerty.gui_craftguide.texture.TextureClip;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;


public final class CraftGuide extends CraftGuideAPIObject implements IIntegrationModule, RecipeProvider
{
	private static final int SLOT_SIZE = 16;
	private static final int TEXTURE_WIDTH = 79;
	private static final int TEXTURE_HEIGHT = 58;
	private static final int GRINDER_RATIO = 10000;

	private static final Slot[] GRINDER_SLOTS = {
			new ItemSlot( 3, 21, SLOT_SIZE, SLOT_SIZE ).drawOwnBackground(),
			new ItemSlot( 41, 21, SLOT_SIZE, SLOT_SIZE, true ).drawOwnBackground().setSlotType( SlotType.OUTPUT_SLOT ),
			new ChanceSlot( 59, 12, SLOT_SIZE, SLOT_SIZE, true ).setRatio( GRINDER_RATIO ).setFormatString( " (%1$.2f%% chance)" ).drawOwnBackground().setSlotType( SlotType.OUTPUT_SLOT ),
			new ChanceSlot( 59, 30, SLOT_SIZE, SLOT_SIZE, true ).setRatio( GRINDER_RATIO ).setFormatString( " (%1$.2f%% chance)" ).drawOwnBackground().setSlotType( SlotType.OUTPUT_SLOT ),
			new ItemSlot( 22, 12, SLOT_SIZE, SLOT_SIZE ).setSlotType( SlotType.MACHINE_SLOT ),
			new ItemSlot( 22, 30, SLOT_SIZE, SLOT_SIZE ).setSlotType( SlotType.MACHINE_SLOT )
	};
	private static final Slot[] INSCRIBER_SLOTS = {
			new ItemSlot( 12, 21, SLOT_SIZE, SLOT_SIZE ).drawOwnBackground(),
			new ItemSlot( 21, 3, SLOT_SIZE, SLOT_SIZE ).drawOwnBackground(),
			new ItemSlot( 21, 39, SLOT_SIZE, SLOT_SIZE ).drawOwnBackground(),
			new ItemSlot( 50, 21, SLOT_SIZE, SLOT_SIZE, true ).drawOwnBackground().setSlotType( SlotType.OUTPUT_SLOT ),
			new ItemSlot( 31, 21, SLOT_SIZE, SLOT_SIZE ).setSlotType( SlotType.MACHINE_SLOT )
	};
	private static final Slot[] SHAPELESS_CRAFTING_SLOTS = {
			new ItemSlot( 3, 3, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 21, 3, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 39, 3, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 3, 21, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 21, 21, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 39, 21, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 3, 39, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 21, 39, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 39, 39, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 59, 21, SLOT_SIZE, SLOT_SIZE, true ).setSlotType( SlotType.OUTPUT_SLOT ),
	};
	private static final Slot[] CRAFTING_SLOTS_OWN_BG = {
			new ItemSlot( 3, 3, SLOT_SIZE, SLOT_SIZE ).drawOwnBackground(),
			new ItemSlot( 21, 3, SLOT_SIZE, SLOT_SIZE ).drawOwnBackground(),
			new ItemSlot( 39, 3, SLOT_SIZE, SLOT_SIZE ).drawOwnBackground(),
			new ItemSlot( 3, 21, SLOT_SIZE, SLOT_SIZE ).drawOwnBackground(),
			new ItemSlot( 21, 21, SLOT_SIZE, SLOT_SIZE ).drawOwnBackground(),
			new ItemSlot( 39, 21, SLOT_SIZE, SLOT_SIZE ).drawOwnBackground(),
			new ItemSlot( 3, 39, SLOT_SIZE, SLOT_SIZE ).drawOwnBackground(),
			new ItemSlot( 21, 39, SLOT_SIZE, SLOT_SIZE ).drawOwnBackground(),
			new ItemSlot( 39, 39, SLOT_SIZE, SLOT_SIZE ).drawOwnBackground(),
			new ItemSlot( 59, 21, SLOT_SIZE, SLOT_SIZE, true ).setSlotType( SlotType.OUTPUT_SLOT ).drawOwnBackground(),
	};
	private static final Slot[] SMALL_CRAFTING_SLOTS_OWN_BG = {
			new ItemSlot( 12, 12, SLOT_SIZE, SLOT_SIZE ).drawOwnBackground(),
			new ItemSlot( 30, 12, SLOT_SIZE, SLOT_SIZE ).drawOwnBackground(),
			new ItemSlot( 12, 30, SLOT_SIZE, SLOT_SIZE ).drawOwnBackground(),
			new ItemSlot( 30, 30, SLOT_SIZE, SLOT_SIZE ).drawOwnBackground(),
			new ItemSlot( 59, 21, SLOT_SIZE, SLOT_SIZE, true ).setSlotType( SlotType.OUTPUT_SLOT ).drawOwnBackground(),
	};
	private static final Slot[] CRTAFTING_SLOTS = {
			new ItemSlot( 3, 3, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 21, 3, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 39, 3, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 3, 21, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 21, 21, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 39, 21, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 3, 39, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 21, 39, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 39, 39, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 59, 21, SLOT_SIZE, SLOT_SIZE, true ).setSlotType( SlotType.OUTPUT_SLOT ),
	};
	private static final Slot[] SMALL_CRAFTING_SLOTS = {
			new ItemSlot( 12, 12, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 30, 12, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 12, 30, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 30, 30, SLOT_SIZE, SLOT_SIZE ),
			new ItemSlot( 59, 21, SLOT_SIZE, SLOT_SIZE, true ).setSlotType( SlotType.OUTPUT_SLOT ),
	};

	@Reflected
	public static CraftGuide instance;

	public CraftGuide()
	{
		IntegrationHelper.testClassExistence( this, uristqwerty.CraftGuide.CraftGuideLog.class );
		IntegrationHelper.testClassExistence( this, uristqwerty.CraftGuide.DefaultRecipeTemplate.class );
		IntegrationHelper.testClassExistence( this, uristqwerty.CraftGuide.RecipeGeneratorImplementation.class );
		IntegrationHelper.testClassExistence( this, uristqwerty.CraftGuide.api.ChanceSlot.class );
		IntegrationHelper.testClassExistence( this, uristqwerty.CraftGuide.api.CraftGuideAPIObject.class );
		IntegrationHelper.testClassExistence( this, uristqwerty.CraftGuide.api.ItemSlot.class );
		IntegrationHelper.testClassExistence( this, uristqwerty.CraftGuide.api.RecipeGenerator.class );
		IntegrationHelper.testClassExistence( this, uristqwerty.CraftGuide.api.RecipeProvider.class );
		IntegrationHelper.testClassExistence( this, uristqwerty.CraftGuide.api.RecipeTemplate.class );
		IntegrationHelper.testClassExistence( this, uristqwerty.CraftGuide.api.Slot.class );
		IntegrationHelper.testClassExistence( this, uristqwerty.CraftGuide.api.SlotType.class );
		IntegrationHelper.testClassExistence( this, uristqwerty.gui_craftguide.texture.DynamicTexture.class );
		IntegrationHelper.testClassExistence( this, uristqwerty.gui_craftguide.texture.TextureClip.class );
	}

	@Override
	public void init() throws Throwable
	{
	}

	@Override
	public void postInit()
	{
	}

	@Override
	public void generateRecipes( final RecipeGenerator generator )
	{
		final RecipeTemplate craftingTemplate;
		final RecipeTemplate smallTemplate;

		if( uristqwerty.CraftGuide.CraftGuide.newerBackgroundStyle )
		{
			craftingTemplate = generator.createRecipeTemplate( CRAFTING_SLOTS_OWN_BG, null );
			smallTemplate = generator.createRecipeTemplate( SMALL_CRAFTING_SLOTS_OWN_BG, null );
		}
		else
		{
			final TextureClip craftingBG = new TextureClip( DynamicTexture.instance( "recipe_backgrounds" ), 1, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT );
			final TextureClip craftingSelected = new TextureClip( DynamicTexture.instance( "recipe_backgrounds" ), 82, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT );
			craftingTemplate = new DefaultRecipeTemplate( CRTAFTING_SLOTS, RecipeGeneratorImplementation.workbench, craftingBG, craftingSelected );

			final TextureClip smallBG = new TextureClip( DynamicTexture.instance( "recipe_backgrounds" ), 1, 61, TEXTURE_WIDTH, TEXTURE_HEIGHT );
			final TextureClip smallSelected = new TextureClip( DynamicTexture.instance( "recipe_backgrounds" ), 82, 61, TEXTURE_WIDTH, TEXTURE_HEIGHT );
			smallTemplate = new DefaultRecipeTemplate( SMALL_CRAFTING_SLOTS, RecipeGeneratorImplementation.workbench, smallBG, smallSelected );
		}

		final TextureClip shapelessBG = new TextureClip( DynamicTexture.instance( "recipe_backgrounds" ), 1, 121, TEXTURE_WIDTH, TEXTURE_HEIGHT );
		final TextureClip shapelessSelected = new TextureClip( DynamicTexture.instance( "recipe_backgrounds" ), 82, 121, TEXTURE_WIDTH, TEXTURE_HEIGHT );
		final RecipeTemplate shapelessTemplate = new DefaultRecipeTemplate( SHAPELESS_CRAFTING_SLOTS, RecipeGeneratorImplementation.workbench, shapelessBG, shapelessSelected );

		this.addCraftingRecipes( craftingTemplate, smallTemplate, shapelessTemplate, generator );

		final IAppEngApi api = AEApi.instance();
		final IBlocks aeBlocks = api.definitions().blocks();
		final Optional<ItemStack> grindstone = aeBlocks.grindStone().maybeStack( 1 );
		final Optional<ItemStack> inscriber = aeBlocks.inscriber().maybeStack( 1 );

		if( grindstone.isPresent() )
		{
			this.addGrinderRecipes( api, grindstone.get(), generator );
		}

		if( inscriber.isPresent() )
		{
			this.addInscriberRecipes( api, inscriber.get(), generator );
		}
	}

	@SuppressWarnings( "unchecked" )
	private List<IRecipe> getUncheckedRecipes()
	{
		return (List<IRecipe>) CraftingManager.getInstance().getRecipeList();
	}

	private void addCraftingRecipes( final RecipeTemplate template, final RecipeTemplate templateSmall, final RecipeTemplate templateShapeless, final RecipeGenerator generator )
	{
		final List<IRecipe> recipes = this.getUncheckedRecipes();

		int errCount = 0;

		for( final IRecipe recipe : recipes )
		{
			try
			{
				final Object[] items = this.getCraftingRecipe( recipe, true );

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
			catch( final Exception e )
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

	private void addGrinderRecipes( final IAppEngApi api, final ItemStack grindstone, final RecipeGenerator generator )
	{
		final ItemStack handle = api.definitions().blocks().crankHandle().maybeStack( 1 ).orNull();
		final RecipeTemplate grinderTemplate = generator.createRecipeTemplate( GRINDER_SLOTS, grindstone );

		for( final IGrinderEntry recipe : api.registries().grinder().getRecipes() )
		{
			generator.addRecipe( grinderTemplate, new Object[] {
					recipe.getInput(),
					recipe.getOutput(),
					new Object[] {
							recipe.getOptionalOutput(),
							(int) ( recipe.getOptionalChance() * GRINDER_RATIO )
					},
					new Object[] {
							recipe.getSecondOptionalOutput(),
							(int) ( recipe.getOptionalChance() * GRINDER_RATIO )
					},
					handle,
					grindstone
			} );
		}
	}

	private void addInscriberRecipes( final IAppEngApi api, final ItemStack inscriber, final RecipeGenerator generator )
	{
		final RecipeTemplate inscriberTemplate = generator.createRecipeTemplate( INSCRIBER_SLOTS, inscriber );

		for( final IInscriberRecipe recipe : api.registries().inscriber().getRecipes() )
		{
			generator.addRecipe( inscriberTemplate, new Object[] {
					recipe.getInputs(),
					recipe.getTopOptional().orNull(),
					recipe.getBottomOptional().orNull(),
					recipe.getOutput(),
					inscriber
			} );
		}
	}

	private Object[] getCraftingShapelessRecipe( final List<?> items, final ItemStack recipeOutput )
	{
		final Object[] output = new Object[10];

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
				catch( final RegistrationError ignored )
				{

				}
				catch( final MissingIngredientError ignored )
				{

				}
			}
		}

		output[9] = recipeOutput;

		return output;
	}

	private Object[] getSmallShapedRecipe( final int width, final int height, final Object[] items, final ItemStack recipeOutput )
	{
		final Object[] output = new Object[5];

		for( int y = 0; y < height; y++ )
		{
			for( int x = 0; x < width; x++ )
			{
				final int i = y * 2 + x;
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
					catch( final RegistrationError ignored )
					{

					}
					catch( final MissingIngredientError ignored )
					{

					}
				}
			}
		}

		output[4] = recipeOutput;

		return output;
	}

	private Object[] getCraftingShapedRecipe( final int width, final int height, final Object[] items, final ItemStack recipeOutput )
	{
		final Object[] output = new Object[10];

		for( int y = 0; y < height; y++ )
		{
			for( int x = 0; x < width; x++ )
			{
				final int i = y * 3 + x;
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
					catch( final RegistrationError ignored )
					{

					}
					catch( final MissingIngredientError ignored )
					{

					}
				}
			}
		}

		output[9] = recipeOutput;

		return output;
	}

	private Object toCG( final ItemStack[] itemStackSet )
	{
		final List<ItemStack> list = Arrays.asList( itemStackSet );

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

	@Nullable
	private Object[] getCraftingRecipe( final IRecipe recipe, final boolean allowSmallGrid )
	{
		if( recipe instanceof ShapelessRecipe )
		{
			final List<Object> items = ReflectionHelper.getPrivateValue( ShapelessRecipe.class, (ShapelessRecipe) recipe, "input" );

			return this.getCraftingShapelessRecipe( items, recipe.getRecipeOutput() );
		}
		else if( recipe instanceof ShapedRecipe )
		{
			final int width = ReflectionHelper.getPrivateValue( ShapedRecipe.class, (ShapedRecipe) recipe, "width" );
			final int height = ReflectionHelper.getPrivateValue( ShapedRecipe.class, (ShapedRecipe) recipe, "height" );
			final Object[] items = ReflectionHelper.getPrivateValue( ShapedRecipe.class, (ShapedRecipe) recipe, "input" );

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
}
