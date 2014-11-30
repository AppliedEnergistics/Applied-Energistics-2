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


import com.google.common.base.Optional;


public class FeatureNameExtractor
{
	private final Class<?> clazz;
	private final Optional<String> subName;

	public FeatureNameExtractor( Class<?> clazz, Optional<String> subName )
	{
		this.clazz = clazz;
		this.subName = subName;
	}

	public String get()
	{
		String name = this.clazz.getSimpleName();

		if ( name.startsWith( "ItemMultiPart" ) )
		{
			name = name.replace( "ItemMultiPart", "ItemPart" );
		}
		else if ( name.startsWith( "ItemMultiMaterial" ) )
		{
			name = name.replace( "ItemMultiMaterial", "ItemMaterial" );
		}

		if ( this.subName.isPresent() )
		{
			final String subName = this.subName.get();
			// simple hack to allow me to do get nice names for these without
			// mode code outside of AEBaseItem
			if ( subName.startsWith( "P2PTunnel" ) )
			{
				return "ItemPart.P2PTunnel";
			}
			else if ( subName.equals( "CertusQuartzTools" ) )
			{
				return name.replace( "Quartz", "CertusQuartz" );
			}
			else if ( subName.equals( "NetherQuartzTools" ) )
			{
				return name.replace( "Quartz", "NetherQuartz" );
			}

			name += '.' + subName;
		}

		return name;
	}
}
