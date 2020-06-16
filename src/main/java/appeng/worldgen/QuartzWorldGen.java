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
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.ReplaceBlockConfig;
import net.minecraft.world.gen.feature.ReplaceBlockFeature;

import appeng.api.AEApi;
import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IBlocks;
import appeng.api.features.IWorldGen.WorldGenType;
import appeng.core.features.registries.WorldGenRegistry;

public final class QuartzWorldGen extends ReplaceBlockFeature {
    /*
     * private final WorldGenMinable oreNormal; private final WorldGenMinable
     * oreCharged;
     */

    public QuartzWorldGen(Function<Dynamic<?>, ? extends ReplaceBlockConfig> serializer) {
        super(serializer);
        final IBlocks blocks = AEApi.instance().definitions().blocks();
        final IBlockDefinition oreDefinition = blocks.quartzOre();
        final IBlockDefinition chargedDefinition = blocks.quartzOreCharged();

        /*
         * this.oreNormal = oreDefinition.maybeBlock() .map( b -> new WorldGenMinable(
         * b.getDefaultState(), AEConfig.instance().getQuartzOresPerCluster() ) )
         * .orElse( null ); this.oreCharged = chargedDefinition.maybeBlock() .map( b ->
         * new WorldGenMinable( b.getDefaultState(),
         * AEConfig.instance().getQuartzOresPerCluster() ) ) .orElse( null );
         */
    }

    private static boolean shouldGenerate(final boolean isCharged, final World w) {
        return WorldGenRegistry.INSTANCE
                .isWorldGenEnabled(isCharged ? WorldGenType.CHARGED_CERTUS_QUARTZ : WorldGenType.CERTUS_QUARTZ, w);
    }

    @Override
    public boolean place(IWorld worldIn, ChunkGenerator<? extends GenerationSettings> generator, Random r, BlockPos pos,
            ReplaceBlockConfig config) {
        /*
         * if( this.oreNormal == null && this.oreCharged == null ) { return false; }
         * 
         * World w = worldIn.getWorld();
         * 
         * int seaLevel = w.getSeaLevel();
         * 
         * ChunkPos chunkPos = new ChunkPos( pos );
         * 
         * if( seaLevel < 20 ) { final int x = ( chunkPos.x << 4 ) + 8; final int z = (
         * chunkPos.z << 4 ) + 8; seaLevel = w.getHeight( Heightmap.Type.WORLD_SURFACE,
         * x, z ); }
         * 
         * final int oreDepthMultiplier =
         * AEConfig.instance().getQuartzOresClusterAmount() * seaLevel / 64; final int
         * scale = (int) Math.round( r.nextGaussian() * Math.sqrt( oreDepthMultiplier )
         * + oreDepthMultiplier );
         * 
         * for( int cnt = 0; cnt < ( r.nextBoolean() ? scale * 2 : scale ) / 2; ++cnt )
         * { boolean isCharged = false;
         * 
         * if( this.oreCharged != null ) { isCharged = r.nextFloat() >
         * AEConfig.instance().getSpawnChargedChance(); }
         * 
         * final WorldGenMinable whichOre = isCharged ? this.oreCharged :
         * this.oreNormal; if( whichOre != null && shouldGenerate( isCharged, w ) ) {
         * final int cx = chunkPos.x * 16 + r.nextInt( 16 ); final int cy = r.nextInt(
         * 40 * seaLevel / 64 ) + r.nextInt( 22 * seaLevel / 64 ) + 12 * seaLevel / 64;
         * final int cz = chunkPos.z * 16 + r.nextInt( 16 ); whichOre.generate( w, r,
         * new BlockPos( cx, cy, cz ) ); } }
         */
        return true;
    }

}
