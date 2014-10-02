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

	@Override
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
				CachedGrindStoneRecipe cachedRecipe = (CachedGrindStoneRecipe) cr;
				if ( cachedRecipe.hasOptional )
				{
					FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
					int width = fr.getStringWidth( cachedRecipe.Chance );
					fr.drawString( cachedRecipe.Chance, (168 - width) / 2, 5, 0 );
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

	@Override
	public void loadTransferRects()
	{
		this.transferRects.add( new TemplateRecipeHandler.RecipeTransferRect( new Rectangle( 84, 23, 24, 18 ), "grindstone" ) );
	}

	@Override
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
			for (IGrinderEntry recipe : AEApi.instance().registries().grinder().getRecipes())
			{
				CachedGrindStoneRecipe cachedRecipe = new CachedGrindStoneRecipe( recipe );
				cachedRecipe.computeVisuals();
				this.arecipes.add( cachedRecipe );
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
		for (IGrinderEntry recipe : AEApi.instance().registries().grinder().getRecipes())
		{
			if ( NEIServerUtils.areStacksSameTypeCrafting( recipe.getOutput(), result ) )
			{
				CachedGrindStoneRecipe cachedRecipe = new CachedGrindStoneRecipe( recipe );
				cachedRecipe.computeVisuals();
				this.arecipes.add( cachedRecipe );
			}
		}
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		for (IGrinderEntry recipe : AEApi.instance().registries().grinder().getRecipes())
		{
			CachedGrindStoneRecipe cachedRecipe = new CachedGrindStoneRecipe( recipe );

			if ( (cachedRecipe.contains( cachedRecipe.ingredients, ingredient.getItem() )) )
			{
				cachedRecipe.computeVisuals();
				if ( cachedRecipe.contains( cachedRecipe.ingredients, ingredient ) )
				{
					cachedRecipe.setIngredientPermutation( cachedRecipe.ingredients, ingredient );
					this.arecipes.add( cachedRecipe );
				}
			}
		}
	}

	@Override
	public String getGuiTexture()
	{
		ResourceLocation loc = new ResourceLocation( "appliedenergistics2", "textures/guis/grinder.png" );
		return loc.toString();
	}

	@Override
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

		public final ArrayList<PositionedStack> ingredients;
		public final PositionedStack result;

		boolean hasOptional = false;
		public String Chance;

		public CachedGrindStoneRecipe(IGrinderEntry recipe) {
			result = new PositionedStack( recipe.getOutput(), -30 + 107, 47 );
			ingredients = new ArrayList<PositionedStack>();

			if ( recipe.getOptionalOutput() != null )
			{
				hasOptional = true;
				Chance = ((int) (recipe.getOptionalChance() * 100)) + GuiText.OfSecondOutput.getLocal();
				ingredients.add( new PositionedStack( recipe.getOptionalOutput(), -30 + 107 + 18, 47 ) );
			}

			if ( recipe.getInput() != null )
				ingredients.add( new PositionedStack( recipe.getInput(), 45, 24 ) );
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