package appeng.integration.modules.jei;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;


/**
 * Acts as a fake facade recipe wrapper, created by {@link FacadeRegistryPlugin}.
 */
class FacadeRecipeWrapper extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper
{

	private final ItemStack textureItem;

	private final ItemStack cableAnchor;

	private final ItemStack facade;

	FacadeRecipeWrapper( ItemStack textureItem, ItemStack cableAnchor, ItemStack facade )
	{
		this.textureItem = textureItem;
		this.cableAnchor = cableAnchor;
		this.facade = facade;
	}

	@Override
	public int getWidth()
	{
		return 3;
	}

	@Override
	public int getHeight()
	{
		return 3;
	}

	@Override
	public void getIngredients( IIngredients ingredients )
	{
		List<ItemStack> input = new ArrayList<>( 9 );

		input.add( null );
		input.add( cableAnchor );
		input.add( null );

		input.add( cableAnchor );
		input.add( textureItem );
		input.add( cableAnchor );

		input.add( null );
		input.add( cableAnchor );
		input.add( null );

		ingredients.setInputs( ItemStack.class, input );
		ingredients.setOutput( ItemStack.class, facade );
	}
}
