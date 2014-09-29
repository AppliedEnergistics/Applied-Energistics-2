package appeng.integration.modules.NEIHelpers;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.core.localization.GuiText;
import appeng.items.parts.ItemFacade;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.DefaultOverlayRenderer;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.api.IStackPositioner;
import codechicken.nei.recipe.RecipeInfo;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class NEIFacadeRecipeHandler extends TemplateRecipeHandler
{

	final ItemFacade ifa = (ItemFacade) AEApi.instance().items().itemFacade.item();
	final ItemStack cable_anchor = AEApi.instance().parts().partCableAnchor.stack( 1 );

	@Override
	public void loadTransferRects()
	{
		this.transferRects.add( new TemplateRecipeHandler.RecipeTransferRect( new Rectangle( 84, 23, 24, 18 ), "crafting", new Object[0] ) );
	}

	@Override
	public Class<? extends GuiContainer> getGuiClass()
	{
		return GuiCrafting.class;
	}

	@Override
	public String getRecipeName()
	{
		return GuiText.FacadeCrafting.getLocal();
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if ( (outputId.equals( "crafting" )) && (getClass() == NEIFacadeRecipeHandler.class) )
		{
			ItemFacade ifa = (ItemFacade) AEApi.instance().items().itemFacade.item();
			List<ItemStack> facades = ifa.getFacades();
			for (ItemStack is : facades)
			{
				CachedShapedRecipe recipe = new CachedShapedRecipe( is );
				recipe.computeVisuals();
				arecipes.add( recipe );
			}
		}
		else
		{
			super.loadCraftingRecipes( outputId, results );
		}
	}

	@Override
	public void loadCraftingRecipes(ItemStack result)
	{
		if ( result.getItem() == ifa )
		{
			CachedShapedRecipe recipe = new CachedShapedRecipe( result );
			recipe.computeVisuals();
			arecipes.add( recipe );
		}
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		List<ItemStack> facades = ifa.getFacades();
		for (ItemStack is : facades)
		{
			CachedShapedRecipe recipe = new CachedShapedRecipe( is );

			if ( recipe.contains( recipe.ingredients, ingredient.getItem() ) )
			{
				recipe.computeVisuals();
				if ( recipe.contains( recipe.ingredients, ingredient ) )
				{
					recipe.setIngredientPermutation( recipe.ingredients, ingredient );
					arecipes.add( recipe );
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
	public boolean hasOverlay(GuiContainer gui, Container container, int recipe)
	{
		return (super.hasOverlay( gui, container, recipe )) || ((isRecipe2x2( recipe )) && (RecipeInfo.hasDefaultOverlay( gui, "crafting2x2" )));
	}

	@Override
	public IRecipeOverlayRenderer getOverlayRenderer(GuiContainer gui, int recipe)
	{
		IRecipeOverlayRenderer renderer = super.getOverlayRenderer( gui, recipe );
		if ( renderer != null )
			return renderer;

		IStackPositioner positioner = RecipeInfo.getStackPositioner( gui, "crafting2x2" );
		if ( positioner == null )
			return null;

		return new DefaultOverlayRenderer( getIngredientStacks( recipe ), positioner );
	}

	@Override
	public IOverlayHandler getOverlayHandler(GuiContainer gui, int recipe)
	{
		IOverlayHandler handler = super.getOverlayHandler( gui, recipe );
		if ( handler != null )
			return handler;

		return RecipeInfo.getOverlayHandler( gui, "crafting2x2" );
	}

	public boolean isRecipe2x2(int recipe)
	{
		for (PositionedStack stack : getIngredientStacks( recipe ))
		{
			if ( (stack.relx > 43) || (stack.rely > 24) )
				return false;
		}
		return true;
	}

	public class CachedShapedRecipe extends TemplateRecipeHandler.CachedRecipe
	{

		public ArrayList<PositionedStack> ingredients;
		public PositionedStack result;

		public CachedShapedRecipe(ItemStack output) {
			output.stackSize = 4;
			result = new PositionedStack( output, 119, 24 );
			ingredients = new ArrayList<PositionedStack>();
			ItemStack in = ifa.getTextureItem( output );
			setIngredients( 3, 3, new Object[] { null, cable_anchor, null, cable_anchor, in, cable_anchor, null, cable_anchor, null } );
		}

		public void setIngredients(int width, int height, Object[] items)
		{
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					if ( items[(y * width + x)] != null )
					{
						ItemStack is = (ItemStack) items[(y * width + x)];
						PositionedStack stack = new PositionedStack( is, 25 + x * 18, 6 + y * 18, false );
						stack.setMaxSize( 1 );
						this.ingredients.add( stack );
					}
				}
			}
		}

		@Override
		public List<PositionedStack> getIngredients()
		{
			return getCycledIngredients( cycleticks / 20, this.ingredients );
		}

		@Override
		public PositionedStack getResult()
		{
			return this.result;
		}

		public void computeVisuals()
		{
			for (PositionedStack p : this.ingredients)
			{
				p.generatePermutations();
			}
			this.result.generatePermutations();
		}
	}
}