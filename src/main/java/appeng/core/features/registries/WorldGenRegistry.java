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

import appeng.api.features.IWorldGen;
import net.minecraft.world.dimension.Dimension;


public final class WorldGenRegistry implements IWorldGen
{

	public static final WorldGenRegistry INSTANCE = new WorldGenRegistry();
	private final TypeSet[] types;

	private WorldGenRegistry()
	{

		this.types = new TypeSet[WorldGenType.values().length];

		for( final WorldGenType type : WorldGenType.values() )
		{
			this.types[type.ordinal()] = new TypeSet();
		}
	}

	@Override
	public void disableWorldGenForProviderID( final WorldGenType type, final Dimension dimension )
	{
		if( type == null )
		{
			throw new IllegalArgumentException( "Bad Type Passed" );
		}

		if( dimension == null )
		{
			throw new IllegalArgumentException( "Bad Provider Passed" );
		}

		this.types[type.ordinal()].badProviders.add( dimension );
	}

	@Override
	public void enableWorldGenForDimension( final WorldGenType type, final int dimensionID )
	{
		if( type == null )
		{
			throw new IllegalArgumentException( "Bad Type Passed" );
		}

		this.types[type.ordinal()].enabledDimensions.add( dimensionID );
	}

	@Override
	public void disableWorldGenForDimension( final WorldGenType type, final int dimensionID )
	{
		if( type == null )
		{
			throw new IllegalArgumentException( "Bad Type Passed" );
		}

		this.types[type.ordinal()].badDimensions.add( dimensionID );
	}

	@Override
	public boolean isWorldGenEnabled( final WorldGenType type, final World w )
	{
		if( type == null )
		{
			throw new IllegalArgumentException( "Bad Type Passed" );
		}

		if( w == null )
		{
			throw new IllegalArgumentException( "Bad Provider Passed" );
		}

		final boolean isBadProvider = this.types[type.ordinal()].badProviders.contains( w.dimension );
		final boolean isBadDimension = this.types[type.ordinal()].badDimensions.contains( w.dimension.getType().getId() );
		final boolean isGoodDimension = this.types[type.ordinal()].enabledDimensions.contains( w.dimension.getType().getId() );

		if( isBadProvider || isBadDimension )
		{
			return false;
		}

		if( !isGoodDimension && type == WorldGenType.METEORITES )
		{
			return false;
		}

		return true;
	}

	private static class TypeSet
	{

		final HashSet<Dimension> badProviders = new HashSet<>();
		final HashSet<Integer> badDimensions = new HashSet<>();
		final HashSet<Integer> enabledDimensions = new HashSet<>();
	}
}
