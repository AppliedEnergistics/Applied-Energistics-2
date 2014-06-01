package appeng.integration.modules.NEIHelpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import appeng.api.AEApi;
import appeng.api.util.AEItemDefinition;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.ICraftingHandler;
import codechicken.nei.recipe.IUsageHandler;

public class NEIWorldCraftingHandler implements ICraftingHandler, IUsageHandler
{

	HashMap<AEItemDefinition, String> details = new HashMap<AEItemDefinition, String>();
	List<AEItemDefinition> offsets = new LinkedList();
	List<PositionedStack> outputs = new LinkedList();

	ItemStack target;

	private void addRecipe(AEItemDefinition def, String msg)
	{
		if ( NEIServerUtils.areStacksSameTypeCrafting( def.stack( 1 ), target ) )
		{
			offsets.add( def );
			outputs.add( new PositionedStack( def.stack( 1 ), 75, 4 ) );
			details.put( def, msg );
		}
	}

	private void addRecipes()
	{
		if ( AEConfig.instance.isFeatureEnabled( AEFeature.inWorldFluix ) )
			addRecipe( AEApi.instance().materials().materialFluixCrystal, GuiText.inWorldFluix.getLocal() );

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.inWorldSingularity ) )
			addRecipe( AEApi.instance().materials().materialQESingularity, GuiText.inWorldSingularity.getLocal() );

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.inWorldPurification ) )
		{
			addRecipe( AEApi.instance().materials().materialPureifiedCertusQuartzCrystal, GuiText.inWorldPurificationCertus.getLocal() );
			addRecipe( AEApi.instance().materials().materialPureifiedNetherQuartzCrystal, GuiText.inWorldPurificationNether.getLocal() );
			addRecipe( AEApi.instance().materials().materialPureifiedFluixCrystal, GuiText.inWorldPurificationFluix.getLocal() );
		}
	}

	@Override
	public String getRecipeName()
	{
		return GuiText.InWorldCrafting.getLocal();
	}

	@Override
	public int numRecipes()
	{
		return offsets.size();
	}

	public void drawBackground(int recipe)
	{
		GL11.glColor4f( 1, 1, 1, 1 );// nothing.
	}

	@Override
	public void drawForeground(int recipe)
	{
		if ( this.outputs.size() > recipe )
		{
			// PositionedStack cr = this.outputs.get( recipe );
			String details = this.details.get( this.offsets.get( recipe ) );

			FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
			fr.drawSplitString( details, 10, 25, 150, 0 );
		}
	}

	@Override
	public List<PositionedStack> getIngredientStacks(int recipe)
	{
		return new ArrayList<PositionedStack>();
	}

	@Override
	public List<PositionedStack> getOtherStacks(int recipetype)
	{
		return new ArrayList<PositionedStack>();
	}

	@Override
	public PositionedStack getResultStack(int recipe)
	{
		return outputs.get( recipe );
	}

	@Override
	public void onUpdate()
	{

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

	@Override
	public int recipiesPerPage()
	{
		return 1;
	}

	@Override
	public List<String> handleTooltip(GuiRecipe gui, List<String> currenttip, int recipe)
	{
		return currenttip;
	}

	@Override
	public List<String> handleItemTooltip(GuiRecipe gui, ItemStack stack, List<String> currenttip, int recipe)
	{
		return currenttip;
	}

	@Override
	public boolean keyTyped(GuiRecipe gui, char keyChar, int keyCode, int recipe)
	{
		return false;
	}

	@Override
	public boolean mouseClicked(GuiRecipe gui, int button, int recipe)
	{
		return false;
	}

	public NEIWorldCraftingHandler newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch (Exception e)
		{
			throw new RuntimeException( e );
		}
	}

	@Override
	public IUsageHandler getUsageHandler(String inputId, Object... ingredients)
	{
		return this;
	}

	@Override
	public ICraftingHandler getRecipeHandler(String outputId, Object... results)
	{
		NEIWorldCraftingHandler g = newInstance();
		if ( results.length > 0 && results[0] instanceof ItemStack )
		{
			g.target = (ItemStack) results[0];
			g.addRecipes();
			return g;
		}
		return this;
	}

}