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

package appeng.core;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import org.apache.commons.io.FileUtils;

import appeng.api.recipes.IRecipeHandler;
import appeng.api.recipes.IRecipeLoader;
import appeng.recipes.loader.ConfigLoader;
import appeng.recipes.loader.JarLoader;
import appeng.recipes.loader.RecipeResourceCopier;


/**
 * handles the decision if recipes should be loaded from jar, loaded from file system or force copied from jar
 *
 * @author thatsIch
 * @version rv3 - 12.05.2015
 * @since rv3 12.05.2015
 */
public class RecipeLoader implements Runnable
{
	private final IRecipeHandler handler;

	/**
	 * @param handler handler to load the recipes
	 *
	 * @throws NullPointerException if handler is <tt>null</tt>
	 */
	public RecipeLoader( @Nonnull IRecipeHandler handler )
	{
		Preconditions.checkNotNull( handler );

		this.handler = handler;
	}

	@Override
	public void run()
	{
		// setup copying
		final RecipeResourceCopier copier = new RecipeResourceCopier( "assets/appliedenergistics2/recipes/" );
		final File configDirectory = AppEng.instance().getConfigDirectory();
		final File generatedRecipesDir = new File( configDirectory, "generated-recipes" );
		final File userRecipesDir = new File( configDirectory, "user-recipes" );
		final File readmeGenDest = new File( generatedRecipesDir, "README.html" );
		final File readmeUserDest = new File( userRecipesDir, "README.html" );

        final IRecipeLoader oreDictLoader = new JarLoader("/assets/appliedenergistics2/oredict/");
        this.handler.parseRecipes(oreDictLoader, "vanilla.oredict" );
        this.handler.parseRecipes(oreDictLoader, "ae2.oredict" );

        // generates generated and user recipes dir
		// will clean the generated every time to keep it up to date
		// copies over the recipes in the jar over to the generated folder
		// copies over the readmes
		try
		{
			FileUtils.forceMkdir( generatedRecipesDir );
			FileUtils.forceMkdir( userRecipesDir );
			FileUtils.cleanDirectory( generatedRecipesDir );

			copier.copyTo( generatedRecipesDir );
			FileUtils.copyFile( readmeGenDest, readmeUserDest );

			// parse recipes prioritising the user scripts by using the generated as template
			this.handler.parseRecipes( new ConfigLoader( generatedRecipesDir, userRecipesDir ), "index.recipe" );
		}
		// on failure use jar parsing
		catch( IOException e )
		{
			AELog.error( e );
			this.handler.parseRecipes( new JarLoader( "/assets/appliedenergistics2/recipes/" ), "index.recipe" );
		}
		catch( URISyntaxException e )
		{
			AELog.error( e );
			this.handler.parseRecipes( new JarLoader( "/assets/appliedenergistics2/recipes/" ), "index.recipe" );
		}
	}
}
