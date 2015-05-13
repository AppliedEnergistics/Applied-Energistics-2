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


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import org.apache.commons.io.FileUtils;


/**
 * copies recipes in jars onto file system
 * includes the readme,
 * needs to be modified if other files needs to be handled
 *
 * @author thatsIch
 * @version rv3 - 11.05.2015
 * @since rv3 11.05.2015
 */
public class RecipeResourceCopier
{
	/**
	 * Most expected size of recipes found
	 */
	private static final int INITIAL_RESOURCE_CAPACITY = 20;
	private static final Pattern DOT_COMPILE_PATTERN = Pattern.compile( ".", Pattern.LITERAL );

	/**
	 * copy source in the jar
	 */
	private final String root;

	/**
	 * @param root source root folder of the recipes inside the jar.
	 *
	 * @throws NullPointerException if root is <tt>null</tt>
	 */
	public RecipeResourceCopier( @Nonnull String root )
	{
		Preconditions.checkNotNull( root );

		this.root = root;
	}

	/**
	 * copies recipes found in the root to destination.
	 *
	 * @param destination destination folder to which the recipes are copied to
	 *
	 * @throws URISyntaxException       {@see #getResourceListing}
	 * @throws IOException              {@see #getResourceListing} and if copying the detected resource to file is not possible
	 * @throws NullPointerException     if either parameter is <tt>null</tt>
	 * @throws IllegalArgumentException if destination is not a directory
	 */
	public void copyTo( @Nonnull File destination ) throws URISyntaxException, IOException
	{
		Preconditions.checkNotNull( destination );
		Preconditions.checkArgument( destination.isDirectory() );

		final String[] listing = this.getResourceListing( this.getClass(), this.root );
		for( String list : listing )
		{
			if( list.endsWith( ".recipe" ) || list.endsWith( ".html" ) )
			{
				final InputStream inStream = this.getClass().getResourceAsStream( '/' + this.root + list );
				final File outFile = new File( destination, list );
				if( !outFile.exists() )
				{
					if( inStream != null )
					{
						FileUtils.copyInputStreamToFile( inStream, outFile );
						inStream.close();
					}
				}
			}
		}
	}

	/**
	 * List directory contents for a resource folder. Not recursive.
	 * This is basically a brute-force implementation.
	 * Works for regular files and also JARs.
	 *
	 * @param clazz Any java class that lives in the same place as the resources you want.
	 * @param path  Should end with "/", but not start with one.
	 *
	 * @return Just the name of each member item, not the full paths.
	 *
	 * @throws URISyntaxException            if it is a file path and the URL can not be converted to URI
	 * @throws IOException                   if jar path can not be decoded
	 * @throws UnsupportedOperationException if it is neither in jar nor in file path
	 */
	@Nonnull
	private String[] getResourceListing( Class<?> clazz, String path ) throws URISyntaxException, IOException
	{
		assert clazz != null;
		assert path != null;

		URL dirURL = clazz.getClassLoader().getResource( path );
		if( dirURL != null && dirURL.getProtocol().equals( "file" ) )
		{
			// A file path: easy enough
			return new File( dirURL.toURI() ).list();
		}

		if( dirURL == null )
		{
		/*
		 * In case of a jar file, we can't actually find a directory.
         * Have to assume the same jar as clazz.
         */
			final String me = DOT_COMPILE_PATTERN.matcher( clazz.getName() ).replaceAll( "/" ) + ".class";
			dirURL = clazz.getClassLoader().getResource( me );
		}

		if( dirURL != null && dirURL.getProtocol().equals( "jar" ) )
		{
		/* A JAR path */
			final String jarPath = dirURL.getPath().substring( 5, dirURL.getPath().indexOf( '!' ) ); //strip out only the JAR file
			final JarFile jar = new JarFile( URLDecoder.decode( jarPath, "UTF-8" ) );
			try
			{
				final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
				final Collection<String> result = new HashSet<String>( INITIAL_RESOURCE_CAPACITY ); //avoid duplicates in case it is a subdirectory
				while( entries.hasMoreElements() )
				{
					final String name = entries.nextElement().getName();
					if( name.startsWith( path ) )
					{ //filter according to the path
						String entry = name.substring( path.length() );
						final int checkSubDir = entry.indexOf( '/' );
						if( checkSubDir >= 0 )
						{
							// if it is a subdirectory, we just return the directory name
							entry = entry.substring( 0, checkSubDir );
						}
						result.add( entry );
					}
				}

				return result.toArray( new String[result.size()] );
			}
			finally
			{
				jar.close();
			}
		}

		throw new UnsupportedOperationException( "Cannot list files for URL " + dirURL );
	}
}
