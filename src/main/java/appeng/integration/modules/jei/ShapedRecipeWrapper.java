package appeng.integration.modules.jei;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import scala.actors.threadpool.Arrays;

import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.IIngredient;
import appeng.core.AEConfig;
import appeng.recipes.game.ShapedRecipe;
import appeng.util.Platform;


class ShapedRecipeWrapper extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper
{

	private final ShapedRecipe recipe;

	public ShapedRecipeWrapper( ShapedRecipe recipe )
	{
		this.recipe = recipe;
	}

	@Override
	public void getIngredients( IIngredients ingredients )
	{
		final boolean useSingleItems = AEConfig.instance.disableColoredCableRecipesInJEI();

		Object[] items = recipe.getIngredients();
		int width = recipe.getWidth();
		int height = recipe.getHeight();

		List<List<ItemStack>> in = new ArrayList<>( width * height );

		for( int x = 0; x < width; x++ )
		{
			for( int y = 0; y < height; y++ )
			{
				if( items[( x * height + y )] != null )
				{
					final IIngredient ing = (IIngredient) items[( x * height + y )];

					List<ItemStack> slotList = Collections.emptyList();
					try
					{
						ItemStack[] is = ing.getItemStackSet();
						slotList = useSingleItems ? Platform.findPreferred( is ) : Arrays.asList( is );
					}
					catch( final RegistrationError | MissingIngredientError ignored )
					{
					}
					in.add( slotList );
				}
				else
				{
					in.add( Collections.emptyList() );
				}
			}
		}

		ingredients.setInputLists( ItemStack.class, in );
		ingredients.setOutput( ItemStack.class, recipe.getRecipeOutput() );
	}

	@Override
	public int getWidth()
	{
		return recipe.getWidth();
	}

	@Override
	public int getHeight()
	{
		return recipe.getHeight();
	}
}
