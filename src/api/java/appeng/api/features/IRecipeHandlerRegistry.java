package appeng.api.features;

import appeng.api.recipes.ICraftHandler;
import appeng.api.recipes.IRecipeHandler;
import appeng.api.recipes.ISubItemResolver;

public interface IRecipeHandlerRegistry
{

	/**
	 * Add a new Recipe Handler to the parser.
	 * 
	 * MUST BE CALLED IN PRE-INIT
	 * 
	 * @param name name of crafthandler
	 * @param handler class of crafthandler
	 */
	void addNewCraftHandler(String name, Class<? extends ICraftHandler> handler);

	/**
	 * Add a new resolver to the parser.
	 * 
	 * MUST BE CALLED IN PRE-INIT
	 * 
	 * @param sir sub item resolver
	 */
	void addNewSubItemResolver(ISubItemResolver sir);

	/**
	 * @param name name of crafting handler
	 * @return A recipe handler by name, returns null on failure.
	 */
	ICraftHandler getCraftHandlerFor(String name);

	/**
	 * @return a new recipe handler, which can be used to parse, and read recipe files.
	 */
	public IRecipeHandler createNewRecipehandler();

	/**
	 * resolve sub items by name.
	 * 
	 * @param nameSpace namespace of item
	 * @param itemName full name of item
	 * @return ResolverResult or ResolverResultSet
	 */
	Object resolveItem(String nameSpace, String itemName);

}
