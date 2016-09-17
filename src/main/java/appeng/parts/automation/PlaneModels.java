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

package appeng.parts.automation;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.util.ResourceLocation;

import appeng.core.AppEng;


/**
 * Contains a mapping from a Plane's connections to the models to use for that state.
 */
class PlaneModels
{

	public static final ResourceLocation MODEL_CHASSIS_OFF = new ResourceLocation( AppEng.MOD_ID, "part/transition_plane_off" );
	public static final ResourceLocation MODEL_CHASSIS_ON = new ResourceLocation( AppEng.MOD_ID, "part/transition_plane_on" );
	public static final ResourceLocation MODEL_CHASSIS_HAS_CHANNEL = new ResourceLocation( AppEng.MOD_ID, "part/transition_plane_has_channel" );

	private final Map<PlaneConnections, List<ResourceLocation>> modelsOff;

	private final Map<PlaneConnections, List<ResourceLocation>> modelsOn;

	private final Map<PlaneConnections, List<ResourceLocation>> modelsHasChannel;

	public PlaneModels( String prefixOff, String prefixOn )
	{
		Map<PlaneConnections, List<ResourceLocation>> modelsOff = new HashMap<>();
		Map<PlaneConnections, List<ResourceLocation>> modelsOn = new HashMap<>();
		Map<PlaneConnections, List<ResourceLocation>> modelsHasChannel = new HashMap<>();

		for( PlaneConnections permutation : PlaneConnections.PERMUTATIONS )
		{
			ResourceLocation planeOff = new ResourceLocation( AppEng.MOD_ID, prefixOff + permutation.getFilenameSuffix() );
			ResourceLocation planeOn = new ResourceLocation( AppEng.MOD_ID, prefixOn + permutation.getFilenameSuffix() );

			modelsOff.put( permutation, ImmutableList.of( MODEL_CHASSIS_OFF, planeOff ) );
			modelsOn.put( permutation, ImmutableList.of( MODEL_CHASSIS_ON, planeOff ) );
			modelsHasChannel.put( permutation, ImmutableList.of( MODEL_CHASSIS_HAS_CHANNEL, planeOn ) );
		}

		this.modelsOff = ImmutableMap.copyOf( modelsOff );
		this.modelsOn = ImmutableMap.copyOf( modelsOn );
		this.modelsHasChannel = ImmutableMap.copyOf( modelsHasChannel );
	}

	public List<ResourceLocation> getModel( PlaneConnections connections, boolean hasPower, boolean hasChannel )
	{
		if( hasPower && hasChannel )
		{
			return modelsHasChannel.get( connections );
		}
		else if( hasPower )
		{
			return modelsOn.get( connections );
		}
		else
		{
			return modelsOff.get( connections );
		}
	}

	public List<ResourceLocation> getModels()
	{
		List<ResourceLocation> result = new ArrayList<>();
		modelsOff.values().forEach( result::addAll );
		modelsOn.values().forEach( result::addAll );
		modelsHasChannel.values().forEach( result::addAll );
		return result;
	}

}
