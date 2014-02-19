package appeng.recipes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import appeng.api.recipes.ICraftHandler;

public class RecipeData
{

	final public HashMap<String, String> aliases = new HashMap<String, String>();
	final public List<ICraftHandler> Handlers = new LinkedList<ICraftHandler>();

	public boolean crash = true;
	public boolean erroronmissing = true;

}
