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

package appeng.core.features;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;


public class FeatureNameExtractor
{
	private static final Pattern PATTERN_ITEM_MULTI_PART = Pattern.compile( "ItemMultiPart", Pattern.LITERAL );
	private static final Pattern PATTERN_ITEM_MULTI_MATERIAL = Pattern.compile( "ItemMultiMaterial", Pattern.LITERAL );
	private static final Pattern PATTERN_QUARTZ = Pattern.compile( "Quartz", Pattern.LITERAL );

	private static final Pattern PATTERN_LOWERCASE = Pattern.compile( "([\\p{Upper}])([\\p{Upper}]+)" );
	private static final Pattern PATTERN_LOWERCASER = Pattern.compile( "(.)?([\\p{Upper}])" );

	private final Class<?> clazz;
	private final Optional<String> subName;

	public FeatureNameExtractor( final Class<?> clazz, final Optional<String> subName )
	{
		this.clazz = clazz;
		this.subName = subName;
	}

	public String get()
	{
		String ret;
		String name = this.clazz.getSimpleName().replaceFirst( "\\p{Upper}[\\p{Lower}]*(\\p{Upper})", "$1" );

		if( this.subName.isPresent() )
		{
			final String subName = this.subName.get();
			// simple hack to allow me to do get nice names for these without
			// mode code outside of AEBaseItem
			if( subName.startsWith( "P2PTunnel" ) )
			{
				ret = "p2ptunnel";
			}
			else if( subName.equals( "CertusQuartzTools" ) )
			{
				ret = PATTERN_QUARTZ.matcher( name ).replaceAll( "CertusQuartz" );
			}
			else if( subName.equals( "NetherQuartzTools" ) )
			{
				ret = PATTERN_QUARTZ.matcher( name ).replaceAll( "NetherQuartz" );
			}
			else
			{
				ret = name + '_' + subName;
			}
		}
		else
		{
			ret = name;
		}

		StringBuffer buffer = new StringBuffer();
		Matcher m = PATTERN_LOWERCASE.matcher( ret );
		while( m.find() )
		{
			m.appendReplacement( buffer, m.group( 1 ) + m.group( 2 ).toLowerCase() );
		}
		m.appendTail( buffer );
		m = PATTERN_LOWERCASER.matcher( buffer.toString() );
		buffer = new StringBuffer();
		while( m.find() )
		{
			m.appendReplacement( buffer, ( m.group( 1 ) != null ? m.group( 1 ) + '_' : "" ) + m.group( 2 ).toLowerCase() );
		}
		m.appendTail( buffer );
		ret = buffer.toString().replace( '.', '_' ).replaceAll( "_+", "_" );
		return ret;
	}

}
