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


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.util.BlockPos;

import appeng.core.features.AEFeature;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;


public final class AELog
{
	private static final Logger SERVER = LogManager.getFormatterLogger( "AE2:S" );
	private static final Logger CLIENT = LogManager.getFormatterLogger( "AE2:C" );

	private AELog()
	{
	}

	public static void warning( final String format, final Object... data )
	{
		log( Level.WARN, format, data );
	}

	private static void log( final Level level, final String format, final Object... data )
	{
		if( AEConfig.instance == null || AEConfig.instance.isFeatureEnabled( AEFeature.Logging ) )
		{
			if( Platform.isServer() )
			{
				SERVER.log( level, format, data );
			}
			else
			{
				CLIENT.log( level, format, data );
			}
		}
	}

	public static void grinder( final String o )
	{
		if( AEConfig.instance.isFeatureEnabled( AEFeature.GrinderLogging ) )
		{
			log( Level.DEBUG, "grinder: " + o );
		}
	}

	public static void integration( final Throwable exception )
	{
		if( AEConfig.instance.isFeatureEnabled( AEFeature.IntegrationLogging ) )
		{
			error( exception );
		}
	}

	public static void error( final Throwable e )
	{
		if( AEConfig.instance.isFeatureEnabled( AEFeature.Logging ) )
		{
			severe( "Error: " + e.getClass().getName() + " : " + e.getMessage() );
			e.printStackTrace();
		}
	}

	public static void severe( final String format, final Object... data )
	{
		log( Level.ERROR, format, data );
	}

	public static void blockUpdate( final BlockPos pos, final AEBaseTile aeBaseTile )
	{
		if( AEConfig.instance.isFeatureEnabled( AEFeature.UpdateLogging ) )
		{
			info( aeBaseTile.getClass().getName() + " @ " + pos );
		}
	}

	public static void info( final String format, final Object... data )
	{
		log( Level.INFO, format, data );
	}

	public static void crafting( final String format, final Object... data )
	{
		if( AEConfig.instance.isFeatureEnabled( AEFeature.CraftingLog ) )
		{
			log( Level.INFO, format, data );
		}
	}

	public static void debug( final String format, final Object... data )
	{
		if( AEConfig.instance.isFeatureEnabled( AEFeature.DebugLogging ) )
		{
			log( Level.DEBUG, format, data );
		}
	}
}
