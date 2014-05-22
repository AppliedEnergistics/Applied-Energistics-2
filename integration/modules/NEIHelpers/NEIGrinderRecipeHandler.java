package appeng.integration.modules.NEIHelpers;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import appeng.api.AEApi;
import appeng.api.features.IGrinderEntry;
import appeng.client.gui.implementations.GuiGrinder;
import appeng.core.localization.GuiText;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class NEIGrinderRecipeHandler extends TemplateRecipeHandler
{

	public void drawBackground(int recipe)
	{
		GL11.glColor4f( 1, 1, 1, 1 );
		changeTexture( getGuiTexture() );
		drawTexturedModalRect( 40, 10, 75, 16 + 10, 90, 66 );
	}

	@Override
	public void drawForeground(int recipe)
	{
		super.drawForeground( recipe );
		if ( this.arecipes.size() > recipe )
		{
			CachedRecipe cr = this.arecipes.get( recipe );
			if ( cr instanceof CachedGrindStoneRecipe )
			{
				CachedGrindStoneRecipe cgsr = (CachedGrindStoneRecipe) cr;
				if ( cgsr.hasOptional )
				{
					FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
					int width = fr.getStringWidth( cgsr.Chance );
					fr.drawString( cgsr.Chance, (168 - width) / 2, 5, 0 );
				}
				else
				{
					FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
					int width = fr.getStringWidth( GuiText.NoSecondOutput.getLocal() );
					fr.drawString( GuiText.NoSecondOutput.getLocal(), (168 - width) / 2, 5, 0 );
				}
			}
		}
	}

	public void loadTransferRects()
	{
		this.transferRects.add( new TemplateRecipeHandler.RecipeTransferRect( new Rectangle( 84, 23, 24, 18 ), "grindstone", new Object[0] ) );
	}

	public Class<? extends GuiContainer> getGuiClass()
	{
		return GuiGrinder.class;
	}

	@Override
	public String getRecipeName()
	{
		return GuiText.GrindStone.getLocal();
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if ( (outputId.equals( "grindstone" )) && (getClass() == NEIGrinderRecipeHandler.class) )
		{
			for (IGrinderEntry irecipe : AEApi.instance().registries().grinder().getRecipes())
			{
				CachedGrindStoneRecipe recipe = new CachedGrindStoneRecipe( irecipe );
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
		for (IGrinderEntry irecipe : AEApi.instance().registries().grinder().getRecipes())
		{
			if ( NEIServerUtils.areStacksSameTypeCrafting( irecipe.getOutput(), result ) )
			{
				CachedGrindStoneRecipe recipe = new CachedGrindStoneRecipe( irecipe );
				recipe.computeVisuals();
				this.arecipes.add( recipe );
			}
		}
	}

	public void loadUsageRecipes(ItemStack ingredient)
	{
		for (IGrinderEntry irecipe : AEApi.instance().registries().grinder().getRecipes())
		{
			CachedGrindStoneRecipe recipe = new CachedGrindStoneRecipe( irecipe );

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
		ResourceLocation loc = new ResourceLocation( "appliedenergistics2", "textures/guis/grinder.png" );
		String f = loc.toString();
		return f;
	}

	public String getOverlayIdentifier()
	{
		return "grindstone";
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

	public class CachedGrindStoneRecipe extends TemplateRecipeHandler.CachedRecipe
	{

		public ArrayList<PositionedStack> ingredients;
		public PositionedStack result;

		boolean hasOptional = false;
		public String Chance;

		public CachedGrindStoneRecipe(IGrinderEntry irecipe) {
			result = new PositionedStack( irecipe.getOutput(), -30 + 107, 47 );
			ingredients = new ArrayList<PositionedStack>();

			if ( irecipe.getOptionalOutput() != null )
			{
				hasOptional = true;
				Chance = ((int) (irecipe.getOptionalChance() * 100)) + GuiText.OfSecondOutput.getLocal();
				ingredients.add( new PositionedStack( irecipe.getOptionalOutput(), -30 + 107 + 18, 47 ) );
			}

			if ( irecipe.getInput() != null )
				ingredients.add( new PositionedStack( irecipe.getInput(), 45, 24 ) );
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