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


import appeng.items.AEBaseItem;

import java.util.regex.Pattern;


/**
 * This class is used to rename items to match the persistent stored items.
 * <p>
 * This can be removed, when a new iteration of minecraft arrives or a new world is used.
 * Remember to differentiate the currently renamed items later on correctly.
 *
 * @deprecated only a temporary solution for a rename
 */
@Deprecated
public final class NameResolver
{
	private static final Pattern ITEM_MULTI_PART = Pattern.compile( "ItemMultiPart", Pattern.LITERAL );
	private static final Pattern ITEM_MULTI_MATERIAL = Pattern.compile( "ItemMultiMaterial", Pattern.LITERAL );
	private static final Pattern QUARTZ = Pattern.compile( "Quartz", Pattern.LITERAL );

	private final Class<? extends AEBaseItem> withOriginalName;

	public NameResolver( final Class<? extends AEBaseItem> withOriginalName )
	{
		this.withOriginalName = withOriginalName;
	}

	public String getName( final String subName )
	{
		String name = this.withOriginalName.getSimpleName();

		if( name.startsWith( "ItemMultiPart" ) )
		{
			name = ITEM_MULTI_PART.matcher( name ).replaceAll( "ItemPart" );
		}
		else if( name.startsWith( "ItemMultiMaterial" ) )
		{
			name = ITEM_MULTI_MATERIAL.matcher( name ).replaceAll( "ItemMaterial" );
		}

		if( subName != null )
		{
			// simple hack to allow me to do get nice names for these without
			// mode code outside of AEBaseItem
			if( subName.startsWith( "P2PTunnel" ) )
			{
				return "ItemPart.P2PTunnel";
			}

			if( subName.equals( "CertusQuartzTools" ) )
			{
				return QUARTZ.matcher( name ).replaceAll( "CertusQuartz" );
			}
			if( subName.equals( "NetherQuartzTools" ) )
			{
				return QUARTZ.matcher( name ).replaceAll( "NetherQuartz" );
			}

			name += '.' + subName;
		}

		return name;
	}
}
