package appeng.integration.modules;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.integration.IIntegrationModule;
import appeng.integration.abstraction.INEI;
import appeng.integration.modules.NEIHelpers.NEIAEShapedRecipeHandler;
import appeng.integration.modules.NEIHelpers.NEIAEShapelessRecipeHandler;
import appeng.integration.modules.NEIHelpers.NEICraftingHandler;
import appeng.integration.modules.NEIHelpers.NEIGrinderRecipeHandler;
import appeng.integration.modules.NEIHelpers.NEIInscriberRecipeHandler;
import appeng.integration.modules.NEIHelpers.NEIWorldCraftingHandler;
import codechicken.nei.guihook.GuiContainerManager;

public class NEI implements IIntegrationModule, INEI
{

	public static NEI instance;

	Class API;

	// recipe handler...
	Method registerRecipeHandler;
	Method registerUsageHandler;

	public NEI() throws ClassNotFoundException {
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
		registerRecipeHandler = API.getDeclaredMethod( "registerRecipeHandler", new Class[] { codechicken.nei.recipe.ICraftingHandler.class } );
		registerUsageHandler = API.getDeclaredMethod( "registerUsageHandler", new Class[] { codechicken.nei.recipe.IUsageHandler.class } );

		registerRecipeHandler( new NEIAEShapedRecipeHandler() );
		registerRecipeHandler( new NEIAEShapelessRecipeHandler() );
		registerRecipeHandler( new NEIInscriberRecipeHandler() );
		registerRecipeHandler( new NEIWorldCraftingHandler() );
		registerRecipeHandler( new NEIGrinderRecipeHandler() );

		// crafting terminal...
		Method registerGuiOverlay = API.getDeclaredMethod( "registerGuiOverlay", new Class[] { Class.class, String.class, int.class, int.class } );
		Class IOverlayHandler = Class.forName( "codechicken.nei.api.IOverlayHandler" );
		Class DefaultOverlayHandler = NEICraftingHandler.class;

		Method registerGuiOverlayHandler = API.getDeclaredMethod( "registerGuiOverlayHandler", new Class[] { Class.class, IOverlayHandler, String.class } );
		registerGuiOverlay.invoke( API, GuiCraftingTerm.class, "crafting", 6, 75 );

		Constructor DefaultOverlayHandlerConstructor = DefaultOverlayHandler.getConstructor( new Class[] { int.class, int.class } );
		registerGuiOverlayHandler.invoke( API, GuiCraftingTerm.class, DefaultOverlayHandlerConstructor.newInstance( 6, 75 ), "crafting" );
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
	public RenderItem setItemRender(RenderItem aeri2)
	{
		RenderItem ri = GuiContainerManager.drawItems;
		GuiContainerManager.drawItems = aeri2;
		return ri;
	}

}
