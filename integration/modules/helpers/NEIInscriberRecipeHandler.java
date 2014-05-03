package appeng.integration.modules.helpers;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import appeng.client.gui.implementations.GuiInscriber;
import appeng.core.localization.GuiText;
import appeng.recipes.handlers.Inscribe;
import appeng.recipes.handlers.Inscribe.InscriberRecipe;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class NEIInscriberRecipeHandler extends TemplateRecipeHandler
{

	public void drawBackground(int recipe)
	{
		GL11.glColor4f( 1, 1, 1, 1 );
		changeTexture( getGuiTexture() );
		drawTexturedModalRect( 0, 0, 5, 11, 166, 75 );
	}

	public void loadTransferRects()
	{
		this.transferRects.add( new TemplateRecipeHandler.RecipeTransferRect( new Rectangle( 84, 23, 24, 18 ), "inscriber", new Object[0] ) );
	}

	public Class<? extends GuiContainer> getGuiClass()
	{
		return GuiInscriber.class;
	}

	@Override
	public String getRecipeName()
	{
		return GuiText.Inscriber.getLocal();
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if ( (outputId.equals( "inscriber" )) && (getClass() == NEIInscriberRecipeHandler.class) )
		{
			for (InscriberRecipe irecipe : Inscribe.recipes)
			{
				CachedInscriberRecipe recipe = new CachedInscriberRecipe( irecipe );
				if ( recipe != null )
				{
					recipe.computeVisuals();
					this.arecipes.add( recipe );
				}
			}
		}
		else
		{
			super.loadCraftingRecipes( outputId, results );
		}
	}

	public void loadCraftingRecipes(ItemStack result)
	{
		for (InscriberRecipe irecipe : Inscribe.recipes)
		{
			if ( NEIServerUtils.areStacksSameTypeCrafting( irecipe.output, result ) )
			{
				CachedInscriberRecipe recipe = new CachedInscriberRecipe( irecipe );
				recipe.computeVisuals();
				this.arecipes.add( recipe );
			}
		}
	}

	public void loadUsageRecipes(ItemStack ingredient)
	{
		for (InscriberRecipe irecipe : Inscribe.recipes)
		{
			CachedInscriberRecipe recipe = new CachedInscriberRecipe( irecipe );

			if ( (recipe != null) && (recipe.contains( recipe.ingredients, ingredient.getItem() )) )
			{
				recipe.computeVisuals();
				if ( recipe.contains( recipe.ingredients, ingredient ) )
				{
					recipe.setIngredientPermutation( recipe.ingredients, ingredient );
					this.arecipes.add( recipe );
				}
			}
		}
	}

	public String getGuiTexture()
	{
		ResourceLocation loc = new ResourceLocation( "appliedenergistics2", "textures/guis/inscriber.png" );
		String f = loc.toString();
		return f;
	}

	public String getOverlayIdentifier()
	{
		return "inscriber";
	}

	@Override
	public boolean hasOverlay(GuiContainer gui, Container container, int recipe)
	{
		return false;
	}

	@Override
	public IRecipeOverlayRenderer getOverlayRenderer(GuiContainer gui, int recipe)
	{
		return null;
	}

	@Override
	public IOverlayHandler getOverlayHandler(GuiContainer gui, int recipe)
	{
		return null;
	}

	public class CachedInscriberRecipe extends TemplateRecipeHandler.CachedRecipe
	{

		public ArrayList<PositionedStack> ingredients;
		public PositionedStack result;

		public CachedInscriberRecipe(InscriberRecipe irecipe) {
			result = new PositionedStack( irecipe.output, 108, 29 );
			ingredients = new ArrayList<PositionedStack>();

			if ( irecipe.plateA != null )
				ingredients.add( new PositionedStack( irecipe.plateA, 40, 5 ) );

			if ( irecipe.imprintable != null )
				ingredients.add( new PositionedStack( irecipe.imprintable, 40 + 18, 28 ) );

			if ( irecipe.plateB != null )
				ingredients.add( new PositionedStack( irecipe.plateB, 40, 51 ) );
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