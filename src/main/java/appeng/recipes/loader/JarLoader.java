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

package appeng.recipes.loader;


import appeng.api.recipes.IRecipeLoader;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class JarLoader implements IRecipeLoader
{

	private final String rootPath;

	public JarLoader( final String s )
	{
		this.rootPath = s;
	}

	@Override
	public BufferedReader getFile( @Nonnull final String s ) throws Exception
	{
		Preconditions.checkNotNull( s );
		Preconditions.checkArgument( !s.isEmpty() );

		return new BufferedReader( new InputStreamReader( this.getClass().getResourceAsStream( this.rootPath + s ), "UTF-8" ) );
	}
}
