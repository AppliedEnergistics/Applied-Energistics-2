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

package appeng.worldgen;


import appeng.api.features.AEFeature;
import appeng.api.features.IWorldGen.WorldGenType;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.features.registries.WorldGenRegistry;
import appeng.core.worlddata.WorldData;
import appeng.hooks.TickHandler;
import appeng.util.IWorldCallable;
import appeng.util.Platform;
import appeng.worldgen.meteorite.ChunkOnly;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;


public final class MeteoriteWorldGen extends Feature<NoFeatureConfig>
{

	public static final MeteoriteWorldGen INSTANCE = new MeteoriteWorldGen();

	private MeteoriteWorldGen( )
	{
		super( NoFeatureConfig::deserialize );
		setRegistryName( AppEng.MOD_ID, "meteorite" );
	}

	private boolean tryMeteorite( final World w, int depth, final int x, final int z )
	{
		for( int tries = 0; tries < 20; tries++ )
		{
			final MeteoritePlacer mp = new MeteoritePlacer();

			if( mp.spawnMeteorite( new ChunkOnly( w, x >> 4, z >> 4 ), x, depth, z ) )
			{
				final int px = x >> 4;
				final int pz = z >> 4;

				for( int cx = px - 6; cx < px + 6; cx++ )
				{
					for( int cz = pz - 6; cz < pz + 6; cz++ )
					{
						if( w.getChunkProvider().isChunkLoaded( new ChunkPos( cx, cz ) ) )
						{
							if( px == cx && pz == cz )
							{
								continue;
							}

							if( WorldData.instance().spawnData().hasGenerated( w.getDimension(), cx, cz ) )
							{
								final MeteoritePlacer mp2 = new MeteoritePlacer();
								mp2.spawnMeteorite( new ChunkOnly( w, cx, cz ), mp.getSettings() );
							}
						}
					}
				}

				return true;
			}

			depth -= 15;
			if( depth < 40 )
			{
				return false;
			}
		}

		return false;
	}

	private Iterable<CompoundNBT> getNearByMeteorites( final World w, final int chunkX, final int chunkZ )
	{
		return WorldData.instance().spawnData().getNearByMeteorites( w.getDimension(), chunkX, chunkZ );
	}

	@ParametersAreNonnullByDefault
	@Override
	public boolean place( IWorld w, ChunkGenerator<? extends GenerationSettings> generator, Random r, BlockPos pos, NoFeatureConfig config )
	{
		if(!AEConfig.instance().isFeatureEnabled( AEFeature.METEORITE_WORLD_GEN )) return false;

		ChunkPos chunkPos = new ChunkPos( pos );
		if( WorldGenRegistry.INSTANCE.isWorldGenEnabled( WorldGenType.METEORITES, w.getWorld() ) )
		{
			final int x = r.nextInt( 16 ) + ( chunkPos.x << 4 );
			final int z = r.nextInt( 16 ) + ( chunkPos.z << 4 );
			final int depth = AEConfig.instance().getMeteoriteMaximumSpawnHeight() + r.nextInt( 20 );

			TickHandler.INSTANCE.addCallable( w.getWorld(), new MeteoriteSpawn( x, depth, z ) );
			return true;
		}
		else
		{
			WorldData.instance().compassData().service().updateArea( w.getWorld(), chunkPos.x, chunkPos.z );
			return false;
		}
	}

	private class MeteoriteSpawn implements IWorldCallable<Object>
	{

		private final int x;
		private final int z;
		private final int depth;

		public MeteoriteSpawn( final int x, final int depth, final int z )
		{
			this.x = x;
			this.z = z;
			this.depth = depth;
		}

		@Override
		public Object call( final World world ) throws Exception
		{
			final int chunkX = this.x >> 4;
			final int chunkZ = this.z >> 4;

			double minSqDist = Double.MAX_VALUE;

			// near by meteorites!
			for( final CompoundNBT data : MeteoriteWorldGen.this.getNearByMeteorites( world, chunkX, chunkZ ) )
			{
				final MeteoritePlacer mp = new MeteoritePlacer();
				mp.spawnMeteorite( new ChunkOnly( world, chunkX, chunkZ ), data );

				minSqDist = Math.min( minSqDist, mp.getSqDistance( this.x, this.z ) );
			}

			final boolean isCluster = ( minSqDist < 30 * 30 ) && Platform.getRandomFloat() < AEConfig.instance().getMeteoriteClusterChance();

			if( minSqDist > AEConfig.instance().getMinMeteoriteDistanceSq() || isCluster )
			{
				MeteoriteWorldGen.this.tryMeteorite( world, this.depth, this.x, this.z );
			}

			WorldData.instance().spawnData().setGenerated( world.getDimension(), chunkX, chunkZ );
			WorldData.instance().compassData().service().updateArea( world, chunkX, chunkZ );

			return null;
		}
	}
}
