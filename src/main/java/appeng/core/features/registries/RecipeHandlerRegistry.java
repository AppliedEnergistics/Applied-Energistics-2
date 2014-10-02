package appeng.core.features.registries;

import java.util.HashMap;
import java.util.LinkedList;

import appeng.api.features.IRecipeHandlerRegistry;
import appeng.api.recipes.ICraftHandler;
import appeng.api.recipes.IRecipeHandler;
import appeng.api.recipes.ISubItemResolver;
import appeng.core.AELog;
import appeng.recipes.RecipeHandler;

public class RecipeHandlerRegistry implements IRecipeHandlerRegistry
{

	final HashMap<String, Class<? extends ICraftHandler>> handlers = new HashMap<String, Class<? extends ICraftHandler>>();
	final LinkedList<ISubItemResolver> resolvers = new LinkedList<ISubItemResolver>();

	@Override
	public void addNewCraftHandler(String name, Class<? extends ICraftHandler> handler)
	{
		handlers.put( name.toLowerCase(), handler );
	}

	@Override
	public ICraftHandler getCraftHandlerFor(String name)
	{
		Class<? extends ICraftHandler> clz = handlers.get( name );
		if ( clz == null )
			return null;
		try
		{
			return clz.newInstance();
		}
		catch (Throwable e)
		{
			AELog.severe( "Error Caused when trying to construct " + clz.getName() );
			AELog.error( e );
			handlers.put( name, null ); // clear it..
			return null;
		}
	}

	@Override
	public IRecipeHandler createNewRecipehandler()
	{
		return new RecipeHandler();
	}

	@Override
	public void addNewSubItemResolver(ISubItemResolver sir)
	{
		resolvers.add( sir );
	}

	@Override
	public Object resolveItem(String nameSpace, String itemName)
	{
		for (ISubItemResolver sir : resolvers)
		{
			Object rr = null;

			try
			{
				rr = sir.resolveItemByName( nameSpace, itemName );
			}
			catch (Throwable t)
			{
				AELog.error( t );
			}

			if ( rr != null )
				return rr;
		}

		return null;
	}

}
