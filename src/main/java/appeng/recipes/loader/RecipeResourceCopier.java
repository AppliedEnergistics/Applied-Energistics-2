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


import com.google.common.base.Preconditions;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * copies recipes in jars onto file system includes the readme, needs to be modified if other files needs to be handled
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
	private static final String FILE_PROTOCOL = "file";
	private static final String CLASS_EXTENSION = ".class";
	private static final String JAR_PROTOCOL = "jar";
	private static final String UTF_8_ENCODING = "UTF-8";

	/**
	 * copy source in the jar
	 */
	private final String root;

	/**
	 * @param root source root folder of the recipes inside the jar.
	 * @throws NullPointerException if root is <tt>null</tt>
	 */
	public RecipeResourceCopier( @Nonnull final String root )
	{
		Preconditions.checkNotNull( root );

		this.root = root;
	}

	/**
	 * copies recipes found in the root to destination.
	 *
	 * @param identifier  only copy files which end with the identifier
	 * @param destination destination folder to which the recipes are copied to
	 * @throws URISyntaxException       {@see #getResourceListing}
	 * @throws IOException              {@see #getResourceListing} and if copying the detected resource to file is not possible
	 * @throws NullPointerException     if either parameter is <tt>null</tt>
	 * @throws IllegalArgumentException if destination is not a directory
	 */
	public void copyTo( @Nonnull final String identifier, @Nonnull final File destination ) throws URISyntaxException, IOException
	{
		Preconditions.checkNotNull( destination );
		Preconditions.checkArgument( destination.isDirectory() );

		this.copyTo( identifier, destination, this.root );
	}

	/**
	 * @param destination destination folder to which the recipes are copied to
	 * @param directory   the folder to copy.
	 * @throws URISyntaxException {@see #getResourceListing}
	 * @throws IOException        {@see #getResourceListing} and if copying the detected resource to file is not possible
	 * @see {RecipeResourceCopier#copyTo(File)}
	 */
	private void copyTo( @Nonnull final String identifier, @Nonnull final File destination, @Nonnull final String directory )
			throws URISyntaxException, IOException
	{
		assert identifier != null;
		assert destination != null;
		assert directory != null;

		final Class<? extends RecipeResourceCopier> copierClass = this.getClass();
		final String[] listing = this.getResourceListing( copierClass, directory );
		for( final String list : listing )
		{
			if( list.endsWith( identifier ) )
			{
				// generate folder before the file is copied so no empty folders will be generated
				FileUtils.forceMkdir( destination );

				this.copyFile( destination, directory, list );
			}
			else if( !list.contains( "." ) )
			{
				final File subDirectory = new File( destination, list );

				this.copyTo( identifier, subDirectory, directory + list + "/" );
			}
		}
	}

	/**
	 * Copies a single file inside a folder to the destination.
	 *
	 * @param destination folder to which the file is copied to
	 * @param directory   the directory containing the file
	 * @param fileName    the file to copy
	 * @throws IOException if copying the file is not possible
	 */
	private void copyFile( @Nonnull final File destination, @Nonnull final String directory, @Nonnull final String fileName ) throws IOException
	{
		assert destination != null;
		assert directory != null;
		assert fileName != null;

		final Class<? extends RecipeResourceCopier> copierClass = this.getClass();
		final InputStream inStream = copierClass.getResourceAsStream( '/' + directory + fileName );
		final File outFile = new File( destination, fileName );

		if( !outFile.exists() && inStream != null )
		{
			FileUtils.copyInputStreamToFile( inStream, outFile );
			inStream.close();
		}
	}

	/**
	 * List directory contents for a resource folder. Not recursive. This is basically a brute-force implementation. Works for regular files and also JARs.
	 *
	 * @param clazz Any java class that lives in the same place as the resources you want.
	 * @param path  Should end with "/", but not start with one.
	 * @return Just the name of each member item, not the full paths.
	 * @throws URISyntaxException            if it is a file path and the URL can not be converted to URI
	 * @throws IOException                   if jar path can not be decoded
	 * @throws UnsupportedOperationException if it is neither in jar nor in file path
	 */
	@Nonnull
	private String[] getResourceListing( @Nonnull final Class<?> clazz, @Nonnull final String path ) throws URISyntaxException, IOException
	{
		assert clazz != null;
		assert path != null;

		final ClassLoader classLoader = clazz.getClassLoader();
		if( classLoader == null )
		{
			throw new IllegalStateException( "ClassLoader was not found. It was probably loaded at a inappropriate time" );
		}

		URL dirURL = classLoader.getResource( path );
		if( dirURL != null )
		{
			final String protocol = dirURL.getProtocol();
			if( protocol.equals( FILE_PROTOCOL ) )
			{
				// A file path: easy enough

				final URI uriOfURL = dirURL.toURI();
				final File fileOfURI = new File( uriOfURL );
				final String[] filesAndDirectoriesOfURI = fileOfURI.list();

				if( filesAndDirectoriesOfURI == null )
				{
					throw new IllegalStateException( "Files and Directories were illegal. Either an abstract pathname does not denote a directory, or an I/O error occured." );
				}
				else
				{
					return filesAndDirectoriesOfURI;
				}
			}
		}

		if( dirURL == null )
		{
			/*
			 * In case of a jar file, we can't actually find a directory.
			 * Have to assume the same jar as clazz.
			 */
			final String className = clazz.getName();
			final Matcher matcher = DOT_COMPILE_PATTERN.matcher( className );
			final String me = matcher.replaceAll( "/" ) + CLASS_EXTENSION;
			dirURL = classLoader.getResource( me );
		}

		if( dirURL != null )
		{
			final String protocol = dirURL.getProtocol();
			if( protocol.equals( JAR_PROTOCOL ) )
			{
				/* A JAR path */
				final String dirPath = dirURL.getPath();
				final String jarPath = dirPath.substring( 5, dirPath.indexOf( '!' ) ); // strip out only
				// the JAR file
				final JarFile jar = new JarFile( URLDecoder.decode( jarPath, UTF_8_ENCODING ) );
				try
				{
					final Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar
					final Collection<String> result = new HashSet<String>( INITIAL_RESOURCE_CAPACITY ); // avoid duplicates

					// in case it is a
					// subdirectory
					while( entries.hasMoreElements() )
					{
						final JarEntry entry = entries.nextElement();
						final String entryFullName = entry.getName();
						if( entryFullName.startsWith( path ) )
						{ // filter according to the path
							String entryName = entryFullName.substring( path.length() );
							final int checkSubDir = entryName.indexOf( '/' );
							if( checkSubDir >= 0 )
							{
								// if it is a subdirectory, we just return the directory name
								entryName = entryName.substring( 0, checkSubDir );
							}
							result.add( entryName );
						}
					}

					return result.toArray( new String[result.size()] );
				}
				finally
				{
					jar.close();
				}
			}
		}

		throw new UnsupportedOperationException( "Cannot list files for URL " + dirURL );
	}
}
