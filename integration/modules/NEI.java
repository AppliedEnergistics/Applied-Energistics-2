package appeng.integration.modules;

import java.lang.reflect.Method;

import appeng.integration.IIntegrationModule;
import appeng.integration.modules.helpers.NEIAEShapedRecipeHandler;
import appeng.integration.modules.helpers.NEIAEShapelessRecipeHandler;

public class NEI implements IIntegrationModule
{

	public static NEI instance;

	Class API;

	public NEI() throws ClassNotFoundException {
		API = Class.forName( "codechicken.nei.api.API" );
	}

	@Override
	public void Init() throws Throwable
	{
		Method registerRecipeHandler = API.getDeclaredMethod( "registerRecipeHandler", new Class[] { codechicken.nei.recipe.ICraftingHandler.class } );
		Method registerUsageHandler = API.getDeclaredMethod( "registerUsageHandler", new Class[] { codechicken.nei.recipe.IUsageHandler.class } );

		registerRecipeHandler.invoke( API, new NEIAEShapedRecipeHandler() );
		registerUsageHandler.invoke( API, new NEIAEShapedRecipeHandler() );

		registerRecipeHandler.invoke( API, new NEIAEShapelessRecipeHandler() );
		registerUsageHandler.invoke( API, new NEIAEShapelessRecipeHandler() );

		/*
		 * Method registerGuiOverlay = API.getDeclaredMethod( "registerGuiOverlay", new Class[] { Class.class,
		 * String.class, int.class, int.class } ); Class IOverlayHandler = Class.forName(
		 * "codechicken.nei.api.IOverlayHandler" ); Class DefaultOverlayHandler =
		 * NEICraftingTerminalOverlayHandler.class; Method registerGuiOverlayHandler = API.getDeclaredMethod(
		 * "registerGuiOverlayHandler", new Class[] { Class.class, IOverlayHandler, String.class } );
		 * registerGuiOverlay.invoke( API, GuiCraftingTerminal.class, "crafting", 6, 75 ); Constructor
		 * DefaultOverlayHandlerConstructor = DefaultOverlayHandler .getConstructor( new Class[] { int.class, int.class
		 * } ); registerGuiOverlayHandler.invoke( API, GuiCraftingTerminal.class,
		 * DefaultOverlayHandlerConstructor.newInstance( 6, 75 ), "crafting" );
		 */
	}

	@Override
	public void PostInit() throws Throwable
	{

	}

}
