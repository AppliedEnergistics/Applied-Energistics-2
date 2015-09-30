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

package appeng.services.version;


import java.util.Scanner;
import java.util.regex.Pattern;


/**
 * can parse a version in form of rv2-beta-8 or rv2.beta.8
 */
public final class VersionParser
{
	private static final Pattern PATTERN_DOT = Pattern.compile( "\\." );
	private static final Pattern PATTERN_DASH = Pattern.compile( "-" );
	private static final Pattern PATTERN_REVISION = Pattern.compile( "[^0-9]+" );
	private static final Pattern PATTERN_BUILD = Pattern.compile( "[^0-9]+" );
	private static final Pattern PATTERN_NATURAL = Pattern.compile( "[0-9]+" );
	private static final Pattern PATTERN_VALID_REVISION = Pattern.compile( "^rv\\d+$" );

	/**
	 * Parses the {@link Version} out of a String
	 *
	 * @param raw String in form of rv2-beta-8 or rv2.beta.8
	 *
	 * @return {@link Version} encoded in the raw String
	 *
	 * @throws AssertionError if raw String does not match pattern of a {@link Version}
	 */
	public Version parse( final String raw )
	{
		final String transformed = this.transformDelimiter( raw );
		final String[] split = transformed.split( "_" );

		return this.parseVersion( split );
	}

	/**
	 * Replaces all "." and "-" into "_" to make them uniform
	 *
	 * @param raw raw version string containing "." or "-"
	 *
	 * @return transformed raw, where "." and "-" are replaced by "_"
	 */
	private String transformDelimiter( final String raw )
	{
		assert raw.contains( "." ) || raw.contains( "-" );

		final String withoutDot = PATTERN_DOT.matcher( raw ).replaceAll( "_" );
		final String withoutDash = PATTERN_DASH.matcher( withoutDot ).replaceAll( "_" );

		return withoutDash;
	}

	/**
	 * parses the {@link Version} out of the split.
	 * The split must have a length of 3,
	 * representing revision, channel and build.
	 *
	 * @param splitRaw raw version split with length of 3
	 *
	 * @return {@link Version} represented by the splitRaw
	 */
	private Version parseVersion( final String[] splitRaw )
	{
		assert splitRaw.length == 3;

		final String rawRevision = splitRaw[0];
		final String rawChannel = splitRaw[1];
		final String rawBuild = splitRaw[2];

		final int revision = this.parseRevision( rawRevision );
		final Channel channel = this.parseChannel( rawChannel );
		final int build = this.parseBuild( rawBuild );

		return new DefaultVersion( revision, channel, build );
	}

	/**
	 * A revision starts with the keyword "rv", followed by a natural number
	 *
	 * @param rawRevision String containing the revision number
	 *
	 * @return revision number
	 */
	private int parseRevision( final String rawRevision )
	{
		assert PATTERN_VALID_REVISION.matcher( rawRevision ).matches();

		final Scanner scanner = new Scanner( rawRevision );

		final int revision = scanner.useDelimiter( PATTERN_REVISION ).nextInt();

		scanner.close();

		return revision;
	}

	/**
	 * A channel is atm either one of {@link Channel#Alpha}, {@link Channel#Beta} or {@link Channel#Stable}
	 *
	 * @param rawChannel String containing the channel
	 *
	 * @return matching {@link Channel} to the String
	 */
	private Channel parseChannel( final String rawChannel )
	{
		assert rawChannel.equalsIgnoreCase( Channel.Alpha.name() ) || rawChannel.equalsIgnoreCase( Channel.Beta.name() ) || rawChannel.equalsIgnoreCase( Channel.Stable.name() );

		for( final Channel channel : Channel.values() )
		{
			if( channel.name().equalsIgnoreCase( rawChannel ) )
			{
				return channel;
			}
		}

		throw new IllegalArgumentException( "Raw channel " + rawChannel + " did not contain any of the pre-programmed types." );
	}

	/**
	 * A build is just a natural number
	 *
	 * @param rawBuild String containing the build number
	 *
	 * @return build number
	 */
	private int parseBuild( final String rawBuild )
	{
		assert PATTERN_NATURAL.matcher( rawBuild ).matches();

		final Scanner scanner = new Scanner( rawBuild );

		final int build = scanner.useDelimiter( PATTERN_BUILD ).nextInt();

		scanner.close();

		return build;
	}
}
