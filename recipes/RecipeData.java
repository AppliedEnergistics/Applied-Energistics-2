package appeng.recipes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import appeng.recipes.handlers.CraftHandler;

public class RecipeData
{

	final public HashMap<String, String> aliases = new HashMap<String, String>();
	final public List<CraftHandler> Handlers = new LinkedList<CraftHandler>();

	public boolean crash = true;
	public boolean erroronmissing = true;

}
