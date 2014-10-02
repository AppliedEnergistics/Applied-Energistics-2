package appeng.integration.modules.NEIHelpers;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.IIngredient;
import appeng.core.AEConfig;
import appeng.recipes.game.ShapedRecipe;
import appeng.util.Platform;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.DefaultOverlayRenderer;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.api.IStackPositioner;
import codechicken.nei.recipe.RecipeInfo;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class NEIAEShapedRecipeHandler extends TemplateRecipeHandler
{

	@Override
	public void loadTransferRects()
	{
		this.transferRects.add( new TemplateRecipeHandler.RecipeTransferRect( new Rectangle( 84, 23, 24, 18 ), "crafting" ) );
	}

	@Override
	public Class<? extends GuiContainer> getGuiClass()
	{
		return GuiCrafting.class;
	}

	@Override
	public String getRecipeName()
	{
		return NEIClientUtils.translate( "recipe.shaped" );
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if ( (outputId.equals( "crafting" )) && (getClass() == NEIAEShapedRecipeHandler.class) )
		{
			List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
			for (IRecipe recipe : recipes)
			{
				if ( (recipe instanceof ShapedRecipe) )
				{
					if ( ((ShapedRecipe) recipe).isEnabled() )
					{
						CachedShapedRecipe cachedRecipe = new CachedShapedRecipe( (ShapedRecipe) recipe );
						cachedRecipe.computeVisuals();
						arecipes.add( cachedRecipe );
					}
				}
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
		List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
		for (IRecipe recipe : recipes)
		{
			if ( (recipe instanceof ShapedRecipe) )
			{
				if ( ((ShapedRecipe) recipe).isEnabled() && NEIServerUtils.areStacksSameTypeCrafting( recipe.getRecipeOutput(), result ) )
				{
					CachedShapedRecipe cachedRecipe = new CachedShapedRecipe( (ShapedRecipe) recipe );
					cachedRecipe.computeVisuals();
					arecipes.add( cachedRecipe );
				}
			}
		}
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
		for (IRecipe recipe : recipes)
		{
			if ( (recipe instanceof ShapedRecipe) )
			{
				CachedShapedRecipe cachedRecipe = new CachedShapedRecipe( (ShapedRecipe) recipe );

				if ( ((ShapedRecipe) recipe).isEnabled() && cachedRecipe.contains( cachedRecipe.ingredients, ingredient.getItem() ) )
				{
					cachedRecipe.computeVisuals();
					if ( cachedRecipe.contains( cachedRecipe.ingredients, ingredient ) )
					{
						cachedRecipe.setIngredientPermutation( cachedRecipe.ingredients, ingredient );
						arecipes.add( cachedRecipe );
					}
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

		public final ArrayList<PositionedStack> ingredients;
		public final PositionedStack result;

		public CachedShapedRecipe(ShapedRecipe recipe) {
			result = new PositionedStack( recipe.getRecipeOutput(), 119, 24 );
			ingredients = new ArrayList<PositionedStack>();
			setIngredients( recipe.getWidth(), recipe.getHeight(), recipe.getIngredients() );
		}

		public void setIngredients(int width, int height, Object[] items)
		{
			boolean useSingleItems = AEConfig.instance.disableColoredCableRecipesInNEI();
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					if ( items[(y * width + x)] != null )
					{
						IIngredient ing = (IIngredient) items[(y * width + x)];

						try
						{
							ItemStack[] is = ing.getItemStackSet();
							PositionedStack stack = new PositionedStack( useSingleItems ? Platform.findPreferred( is ) : is, 25 + x * 18, 6 + y * 18, false );
							stack.setMaxSize( 1 );
							this.ingredients.add( stack );
						}
						catch (RegistrationError ignored)
						{

						}
						catch (MissingIngredientError ignored)
						{

						}

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