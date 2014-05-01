package appeng.integration.modules.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.util.AEItemDefinition;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
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

	private void addRecipe(AEItemDefinition def, String msg)
	{
		offsets.add( def );
		details.put( def, msg );
	}

	public NEIWorldCraftingHandler() {

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.inWorldFluix ) )
			addRecipe( AEApi.instance().materials().materialFluixCrystal, GuiText.inWorldFluix.getLocal() );

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.inWorldSingularity ) )
			addRecipe( AEApi.instance().materials().materialQESingularity, GuiText.inWorldSingularity.getLocal() );

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.inWorldPurification ) )
		{
			addRecipe( AEApi.instance().materials().materialPureifiedCertusQuartzCrystal, GuiText.inWorldPurification.getLocal() );
			addRecipe( AEApi.instance().materials().materialPureifiedNetherQuartzCrystal, GuiText.inWorldPurification.getLocal() );
			addRecipe( AEApi.instance().materials().materialPureifiedFluixCrystal, GuiText.inWorldPurification.getLocal() );
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

	@Override
	public void drawBackground(int recipe)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void drawForeground(int recipe)
	{
		// TODO Auto-generated method stub

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
		return null;
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
		return null;
	}

	@Override
	public List<String> handleItemTooltip(GuiRecipe gui, ItemStack stack, List<String> currenttip, int recipe)
	{
		return null;
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

	@Override
	public IUsageHandler getUsageHandler(String inputId, Object... ingredients)
	{
		return null;
	}

	@Override
	public ICraftingHandler getRecipeHandler(String outputId, Object... results)
	{
		return null;
	}

}