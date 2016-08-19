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

package appeng.core;


import java.io.File;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;


public class FacadeConfig extends Configuration
{

	public static FacadeConfig instance;
	private final Pattern replacementPattern;

	public FacadeConfig( final File facadeFile )
	{
		super( facadeFile );
		this.replacementPattern = Pattern.compile( "[^a-zA-Z0-9]" );
	}

	public boolean checkEnabled( final Block id, final int metadata, final boolean automatic )
	{
		if( id == null )
		{
			return false;
		}

		final ResourceLocation blk = Item.REGISTRY.getNameForObject( Item.getItemFromBlock( id ) );
		if( blk == null )
		{
			for( final Field f : Block.class.getFields() )
			{
				try
				{
					if( f.get( Block.class ) == id )
					{
						return this.get( "minecraft", f.getName() + ( metadata == 0 ? "" : "/" + metadata ), automatic ).getBoolean( automatic );
					}
				}
				catch( final Throwable e )
				{
					// :P
				}
			}
		}
		else
		{
			final Matcher mod = this.replacementPattern.matcher( blk.getResourceDomain() );
			final Matcher name = this.replacementPattern.matcher( blk.getResourcePath() );
			return this.get( mod.replaceAll( "" ), name.replaceAll( "" ) + ( metadata == 0 ? "" : "." + metadata ), automatic ).getBoolean( automatic );
		}

		return false;
	}
}
