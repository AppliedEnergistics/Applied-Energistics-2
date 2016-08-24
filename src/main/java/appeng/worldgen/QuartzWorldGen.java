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

package appeng.worldgen;


import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

import appeng.api.AEApi;
import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IBlocks;
import appeng.api.features.IWorldGen.WorldGenType;
import appeng.core.AEConfig;
import appeng.core.features.registries.WorldGenRegistry;


public final class QuartzWorldGen implements IWorldGenerator
{
	private final WorldGenMinable oreNormal;
	private final WorldGenMinable oreCharged;

	public QuartzWorldGen()
	{
		final IBlocks blocks = AEApi.instance().definitions().blocks();
		final IBlockDefinition oreDefinition = blocks.quartzOre();
		final IBlockDefinition chargedDefinition = blocks.quartzOreCharged();

		final Block ore = oreDefinition.maybeBlock().orElse( null );
		final Block charged = chargedDefinition.maybeBlock().orElse( null );

		this.oreNormal = new WorldGenMinable( ore.getDefaultState(), AEConfig.instance.quartzOresPerCluster );
		this.oreCharged = new WorldGenMinable( charged.getDefaultState(), AEConfig.instance.quartzOresPerCluster );
	}

	@Override
	public void generate( final Random r, final int chunkX, final int chunkZ, final World w, final IChunkGenerator chunkGenerator, final IChunkProvider chunkProvider )
	{
		int seaLevel = w.provider.getAverageGroundLevel() + 1;

		if( seaLevel < 20 )
		{
			final int x = ( chunkX << 4 ) + 8;
			final int z = ( chunkZ << 4 ) + 8;
			seaLevel = (int) w.getHorizon();
		}

		if( this.oreNormal == null || this.oreCharged == null )
		{
			return;
		}

		final double oreDepthMultiplier = AEConfig.instance.quartzOresClusterAmount * seaLevel / 64;
		final int scale = (int) Math.round( r.nextGaussian() * Math.sqrt( oreDepthMultiplier ) + oreDepthMultiplier );

		for( int x = 0; x < ( r.nextBoolean() ? scale * 2 : scale ) / 2; ++x )
		{
			final boolean isCharged = r.nextFloat() > AEConfig.instance.spawnChargedChance;
			final WorldGenMinable whichOre = isCharged ? this.oreCharged : this.oreNormal;

			if( WorldGenRegistry.INSTANCE.isWorldGenEnabled( isCharged ? WorldGenType.ChargedCertusQuartz : WorldGenType.CertusQuartz, w ) )
			{
				final int cx = chunkX * 16 + r.nextInt( 22 );
				final int cy = r.nextInt( 40 * seaLevel / 64 ) + r.nextInt( 22 * seaLevel / 64 ) + 12 * seaLevel / 64;
				final int cz = chunkZ * 16 + r.nextInt( 22 );
				whichOre.generate( w, r, new BlockPos( cx, cy, cz ) );
			}
		}
	}
}
