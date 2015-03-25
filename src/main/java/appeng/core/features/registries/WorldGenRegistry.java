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

package appeng.core.features.registries;


import java.util.HashSet;

import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

import appeng.api.features.IWorldGen;


public class WorldGenRegistry implements IWorldGen
{

	static final public WorldGenRegistry INSTANCE = new WorldGenRegistry();
	final TypeSet[] types;

	private WorldGenRegistry()
	{

		this.types = new TypeSet[WorldGenType.values().length];

		for( WorldGenType type : WorldGenType.values() )
		{
			this.types[type.ordinal()] = new TypeSet();
		}
	}

	@Override
	public void disableWorldGenForProviderID( WorldGenType type, Class<? extends WorldProvider> provider )
	{
		if( type == null )
			throw new IllegalArgumentException( "Bad Type Passed" );

		if( provider == null )
			throw new IllegalArgumentException( "Bad Provider Passed" );

		this.types[type.ordinal()].badProviders.add( provider );
	}

	@Override
	public void enableWorldGenForDimension( WorldGenType type, int dimensionID )
	{
		if( type == null )
			throw new IllegalArgumentException( "Bad Type Passed" );

		this.types[type.ordinal()].enabledDimensions.add( dimensionID );
	}

	@Override
	public void disableWorldGenForDimension( WorldGenType type, int dimensionID )
	{
		if( type == null )
		{
			throw new IllegalArgumentException( "Bad Type Passed" );
		}

		this.types[type.ordinal()].badDimensions.add( dimensionID );
	}

	@Override
	public boolean isWorldGenEnabled( WorldGenType type, World w )
	{
		if( type == null )
			throw new IllegalArgumentException( "Bad Type Passed" );

		if( w == null )
			throw new IllegalArgumentException( "Bad Provider Passed" );

		boolean isBadProvider = this.types[type.ordinal()].badProviders.contains( w.provider.getClass() );
		boolean isBadDimension = this.types[type.ordinal()].badDimensions.contains( w.provider.dimensionId );
		boolean isGoodDimension = this.types[type.ordinal()].enabledDimensions.contains( w.provider.dimensionId );

		if( isBadProvider || isBadDimension )
		{
			return false;
		}

		if( !isGoodDimension && type == WorldGenType.Meteorites )
		{
			return false;
		}

		return true;
	}


	private static class TypeSet
	{

		final HashSet<Class<? extends WorldProvider>> badProviders = new HashSet<Class<? extends WorldProvider>>();
		final HashSet<Integer> badDimensions = new HashSet<Integer>();
		final HashSet<Integer> enabledDimensions = new HashSet<Integer>();
	}
}
