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

package appeng.hooks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;

import cpw.mods.fml.common.IWorldGenerator;

import appeng.api.AEApi;
import appeng.api.features.IWorldGen.WorldGenType;
import appeng.core.AEConfig;
import appeng.core.features.registries.WorldGenRegistry;

final public class QuartzWorldGen implements IWorldGenerator
{

	final WorldGenMinable oreNormal;
	final WorldGenMinable oreCharged;

	public QuartzWorldGen() {
		Block normal = AEApi.instance().definitions().blocks().quartzOre().get().block();
		Block charged = AEApi.instance().definitions().blocks().quartzOreCharged().get().block();

		if ( normal != null && charged != null )
		{
			this.oreNormal = new WorldGenMinable( normal, 0, AEConfig.instance.quartzOresPerCluster, Blocks.stone );
			this.oreCharged = new WorldGenMinable( charged, 0, AEConfig.instance.quartzOresPerCluster, Blocks.stone );
		}
		else
			this.oreNormal = this.oreCharged = null;
	}

	@Override
	public void generate(Random r, int chunkX, int chunkZ, World w, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		int seaLevel = w.provider.getAverageGroundLevel() + 1;

		if ( seaLevel < 20 )
		{
			int x = (chunkX << 4) + 8;
			int z = (chunkZ << 4) + 8;
			seaLevel = w.getHeightValue( x, z );
		}

		if ( this.oreNormal == null || this.oreCharged == null )
			return;

		double oreDepthMultiplier = AEConfig.instance.quartzOresClusterAmount * seaLevel / 64;
		int scale = (int) Math.round( r.nextGaussian() * Math.sqrt( oreDepthMultiplier ) + oreDepthMultiplier );

		for (int x = 0; x < (r.nextBoolean() ? scale * 2 : scale) / 2; ++x)
		{
			boolean isCharged = r.nextFloat() > AEConfig.instance.spawnChargedChance;
			WorldGenMinable whichOre = isCharged ? this.oreCharged : this.oreNormal;

			if ( WorldGenRegistry.INSTANCE.isWorldGenEnabled( isCharged ? WorldGenType.ChargedCertusQuartz : WorldGenType.CertusQuartz, w ) )
			{
				int cx = chunkX * 16 + r.nextInt( 22 );
				int cy = r.nextInt( 40 * seaLevel / 64 ) + r.nextInt( 22 * seaLevel / 64 ) + 12 * seaLevel / 64;
				int cz = chunkZ * 16 + r.nextInt( 22 );
				whichOre.generate( w, r, cx, cy, cz );
			}
		}

	}
}
