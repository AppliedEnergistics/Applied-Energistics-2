/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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


import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

import appeng.core.AELog;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv3 - 10.08.2015
 * @since rv0
 */
public class RecipeHandlerRegistry implements IRecipeHandlerRegistry
{
	private final Collection<ISubItemResolver> resolvers = new ArrayList<>();

	@Override
	public void addNewSubItemResolver( final ISubItemResolver sir )
	{
		this.resolvers.add( sir );
	}

	@Nullable
	@Override
	public Object resolveItem( final String nameSpace, final String itemName )
	{
		for( final ISubItemResolver sir : this.resolvers )
		{
			Object rr = null;

			try
			{
				rr = sir.resolveItemByName( nameSpace, itemName );
			}
			catch( final Throwable t )
			{
				AELog.debug( t );
			}

			if( rr != null )
			{
				return rr;
			}
		}

		return null;
	}
}
