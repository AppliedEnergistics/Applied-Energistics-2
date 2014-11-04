package appeng.integration.modules;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import appeng.client.gui.AEBaseMEGui;
import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.integration.BaseModule;
import appeng.integration.abstraction.INEI;
import appeng.integration.modules.NEIHelpers.NEIAEShapedRecipeHandler;
import appeng.integration.modules.NEIHelpers.NEIAEShapelessRecipeHandler;
import appeng.integration.modules.NEIHelpers.NEICraftingHandler;
import appeng.integration.modules.NEIHelpers.NEIFacadeRecipeHandler;
import appeng.integration.modules.NEIHelpers.NEIGrinderRecipeHandler;
import appeng.integration.modules.NEIHelpers.NEIInscriberRecipeHandler;
import appeng.integration.modules.NEIHelpers.NEIWorldCraftingHandler;
import appeng.integration.modules.NEIHelpers.TerminalCraftingSlotFinder;
import codechicken.nei.api.IStackPositioner;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerTooltipHandler;

public class NEI extends BaseModule implements INEI, IContainerTooltipHandler
{

	public static NEI instance;

	Class API;

	// recipe handler...
	Method registerRecipeHandler;
	Method registerUsageHandler;

	public NEI() throws ClassNotFoundException {
		TestClass( GuiContainerManager.class );
		TestClass( codechicken.nei.recipe.ICraftingHandler.class );
		TestClass( codechicken.nei.recipe.IUsageHandler.class );
		API = Class.forName( "codechicken.nei.api.API" );
	}

	public void registerRecipeHandler(Object o) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		registerRecipeHandler.invoke( API, o );
		registerUsageHandler.invoke( API, o );
	}

	@Override
	public void Init() throws Throwable
	{
		registerRecipeHandler = API.getDeclaredMethod( "registerRecipeHandler", codechicken.nei.recipe.ICraftingHandler.class );
		registerUsageHandler = API.getDeclaredMethod( "registerUsageHandler", codechicken.nei.recipe.IUsageHandler.class );

		registerRecipeHandler( new NEIAEShapedRecipeHandler() );
		registerRecipeHandler( new NEIAEShapelessRecipeHandler() );
		registerRecipeHandler( new NEIInscriberRecipeHandler() );
		registerRecipeHandler( new NEIWorldCraftingHandler() );
		registerRecipeHandler( new NEIGrinderRecipeHandler() );

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.Facades ) && AEConfig.instance.isFeatureEnabled( AEFeature.enableFacadeCrafting ) )
			registerRecipeHandler( new NEIFacadeRecipeHandler() );

		// large stack tooltips
		GuiContainerManager.addTooltipHandler( this );

		// crafting terminal...
		Method registerGuiOverlay = API.getDeclaredMethod( "registerGuiOverlay", Class.class, String.class, IStackPositioner.class );
		Class IOverlayHandler = Class.forName( "codechicken.nei.api.IOverlayHandler" );
		Class DefaultOverlayHandler = NEICraftingHandler.class;

		Method registerGuiOverlayHandler = API.getDeclaredMethod( "registerGuiOverlayHandler", Class.class, IOverlayHandler, String.class );
		registerGuiOverlay.invoke( API, GuiCraftingTerm.class, "crafting", new TerminalCraftingSlotFinder() );
		registerGuiOverlay.invoke( API, GuiPatternTerm.class, "crafting", new TerminalCraftingSlotFinder() );

		Constructor DefaultOverlayHandlerConstructor = DefaultOverlayHandler.getConstructor( int.class, int.class );
		registerGuiOverlayHandler.invoke( API, GuiCraftingTerm.class, DefaultOverlayHandlerConstructor.newInstance( 6, 75 ), "crafting" );
		registerGuiOverlayHandler.invoke( API, GuiPatternTerm.class, DefaultOverlayHandlerConstructor.newInstance( 6, 75 ), "crafting" );
	}

	@Override
	public void PostInit() throws Throwable
	{

	}

	@Override
	public void drawSlot(Slot s)
	{
		if ( s == null )
			return;

		ItemStack stack = s.getStack();

		if ( stack == null )
			return;

		Minecraft mc = Minecraft.getMinecraft();
		FontRenderer fontRenderer = mc.fontRenderer;
		int x = s.xDisplayPosition;
		int y = s.yDisplayPosition;

		GuiContainerManager.drawItems.renderItemAndEffectIntoGUI( fontRenderer, mc.getTextureManager(), stack, x, y );
		GuiContainerManager.drawItems.renderItemOverlayIntoGUI( fontRenderer, mc.getTextureManager(), stack, x, y, "" + stack.stackSize );
	}

	@Override
	public RenderItem setItemRender(RenderItem renderItem)
	{
		try
		{
			RenderItem ri = GuiContainerManager.drawItems;
			GuiContainerManager.drawItems = renderItem;
			return ri;
		}
		catch (Throwable t)
		{
			throw new RuntimeException( "Invalid version of NEI, please update", t );
		}
	}

	@Override
	public List<String> handleItemDisplayName(GuiContainer arg0, ItemStack arg1, List<String> current)
	{
		return current;
	}

	@Override
	public List<String> handleItemTooltip(GuiContainer guiScreen, ItemStack stack, int mouseX, int mouseY, List<String> currentToolTip)
	{
		if ( guiScreen instanceof AEBaseMEGui )
			return ((AEBaseMEGui) guiScreen).handleItemTooltip( stack, mouseX, mouseY, currentToolTip );

		return currentToolTip;
	}

	@Override
	public List<String> handleTooltip(GuiContainer arg0, int arg1, int arg2, List<String> current)
	{
		return current;
	}

}
