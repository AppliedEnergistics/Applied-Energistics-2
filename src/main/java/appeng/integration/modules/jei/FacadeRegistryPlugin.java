package appeng.integration.modules.jei;


import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

import appeng.items.parts.ItemFacade;


/**
 * This plugin will dynamically add facade recipes for any item that can be turned into a facade.
 */
class FacadeRegistryPlugin implements IRecipeRegistryPlugin
{

	private final ItemFacade itemFacade;

	private final ItemStack cableAnchor;

	FacadeRegistryPlugin( ItemFacade itemFacade, ItemStack cableAnchor )
	{
		this.itemFacade = itemFacade;
		this.cableAnchor = cableAnchor;
	}

	@Override
	public <V> List<String> getRecipeCategoryUids( IFocus<V> focus )
	{
		if( focus.getMode() == IFocus.Mode.OUTPUT && focus.getValue() instanceof ItemStack )
		{
			// Looking up how a certain facade is crafted
			ItemStack itemStack = (ItemStack) focus.getValue();
			if( itemStack.getItem() instanceof ItemFacade )
			{
				return Collections.singletonList( VanillaRecipeCategoryUid.CRAFTING );
			}
		}
		else if( focus.getMode() == IFocus.Mode.INPUT && focus.getValue() instanceof ItemStack )
		{
			// Looking up if a certain block can be used to make a facade
			ItemStack itemStack = (ItemStack) focus.getValue();

			if( itemFacade.createFacadeForItem( itemStack, true ) != null )
			{
				return Collections.singletonList( VanillaRecipeCategoryUid.CRAFTING );
			}
		}

		return Collections.emptyList();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers( IRecipeCategory<T> recipeCategory, IFocus<V> focus )
	{
		if( !VanillaRecipeCategoryUid.CRAFTING.equals( recipeCategory.getUid() ) )
		{
			return Collections.emptyList();
		}

		if( focus.getMode() == IFocus.Mode.OUTPUT && focus.getValue() instanceof ItemStack )
		{
			// Looking up how a certain facade is crafted
			ItemStack itemStack = (ItemStack) focus.getValue();
			if( itemStack.getItem() instanceof ItemFacade )
			{
				ItemFacade facadeItem = (ItemFacade) itemStack.getItem();
				ItemStack textureItem = facadeItem.getTextureItem( itemStack );
				return Collections.singletonList( (T) new FacadeRecipeWrapper( textureItem, cableAnchor, itemStack ) );
			}
		}
		else if( focus.getMode() == IFocus.Mode.INPUT && focus.getValue() instanceof ItemStack )
		{
			// Looking up if a certain block can be used to make a facade

			ItemStack itemStack = (ItemStack) focus.getValue();
			ItemStack facade = itemFacade.createFacadeForItem( itemStack, false );

			if( facade != null )
			{
				return Collections.singletonList( (T) new FacadeRecipeWrapper( itemStack, cableAnchor, facade ) );
			}
		}

		return Collections.emptyList();
	}
}
