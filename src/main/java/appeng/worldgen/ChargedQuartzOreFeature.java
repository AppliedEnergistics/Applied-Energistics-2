/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

import com.mojang.serialization.Codec;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;

/**
 * Extends {@link net.minecraft.world.gen.feature.OreFeature} by also allowing for a replacement chance. In addition,
 * the feature will check every block in the chunk.
 */
public class ChargedQuartzOreFeature extends Feature<ChargedQuartzOreConfig> {

    public static final ChargedQuartzOreFeature INSTANCE = new ChargedQuartzOreFeature(ChargedQuartzOreConfig.CODEC);

    private ChargedQuartzOreFeature(Codec<ChargedQuartzOreConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(ISeedReader worldIn, ChunkGenerator generator, Random rand, BlockPos pos,
            ChargedQuartzOreConfig config) {
        ChunkPos chunkPos = new ChunkPos(pos);

        BlockPos.Mutable bpos = new BlockPos.Mutable();
        int height = worldIn.getHeight(Heightmap.Type.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
        IChunk chunk = worldIn.getChunk(pos);
        for (int y = 0; y < height; y++) {
            bpos.setY(y);
            for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
                bpos.setX(x);
                for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                    bpos.setZ(z);
                    if (chunk.getBlockState(bpos).getBlock() == config.target.getBlock()
                            && rand.nextFloat() < config.chance) {
                        chunk.setBlockState(bpos, config.state, false);
                    }
                }
            }
        }

        return true;
    }
}
