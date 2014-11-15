/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

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
