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
import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

public class FacadeConfig extends Configuration
{

	public static FacadeConfig instance;
	final Pattern replacementPattern;

	public FacadeConfig( File facadeFile ) {
		super( facadeFile );
		this.replacementPattern = Pattern.compile( "[^a-zA-Z0-9]" );
	}

	public boolean checkEnabled(Block id, int metadata, boolean automatic)
	{
		if ( id == null )
			return false;

		UniqueIdentifier blk = GameRegistry.findUniqueIdentifierFor( id );
		if ( blk == null )
		{
			for (Field f : Block.class.getFields())
			{
				try
				{
					if ( f.get( Block.class ) == id )
						return this.get( "minecraft", f.getName() + (metadata == 0 ? "" : "." + metadata), automatic ).getBoolean( automatic );
				}
				catch (Throwable e)
				{
					// :P
				}
			}
		}
		else
		{
			Matcher mod = this.replacementPattern.matcher( blk.modId );
			Matcher name = this.replacementPattern.matcher( blk.name );
			return this.get( mod.replaceAll( "" ), name.replaceAll( "" ) + (metadata == 0 ? "" : "." + metadata), automatic ).getBoolean( automatic );
		}

		return false;
	}
}
