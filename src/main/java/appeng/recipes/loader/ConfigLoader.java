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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;


/**
 * Loads the recipes from the config folder
 */
public final class ConfigLoader implements IRecipeLoader
{
	private final File generatedRecipesDir;
	private final File userRecipesDir;

	public ConfigLoader( final File generatedRecipesDir, final File userRecipesDir )
	{
		this.generatedRecipesDir = generatedRecipesDir;
		this.userRecipesDir = userRecipesDir;
	}

	@Override
	public BufferedReader getFile( @Nonnull final String relativeFilePath ) throws Exception
	{
		Preconditions.checkNotNull( relativeFilePath );
		Preconditions.checkArgument( !relativeFilePath.isEmpty(), "Supplying an empty String will result creating a reader of a folder." );

		final File generatedFile = new File( this.generatedRecipesDir, relativeFilePath );
		final File userFile = new File( this.userRecipesDir, relativeFilePath );

		final File toBeLoaded = ( userFile.exists() && userFile.isFile() ) ? userFile : generatedFile;

		return new BufferedReader( new InputStreamReader( new FileInputStream( toBeLoaded ), "UTF-8" ) );
	}
}
