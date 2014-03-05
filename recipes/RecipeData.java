package appeng.recipes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import appeng.api.recipes.ICraftHandler;

public class RecipeData
{

	final public HashMap<String, String> aliases = new HashMap<String, String>();
	final public HashMap<String, GroupIngredient> groups = new HashMap<String, GroupIngredient>();

	final public List<ICraftHandler> Handlers = new LinkedList<ICraftHandler>();

	public boolean crash = true;
	public boolean exceptions = true;
	public boolean erroronmissing = true;
	
	public Set<String> knownItem = new HashSet();

}
