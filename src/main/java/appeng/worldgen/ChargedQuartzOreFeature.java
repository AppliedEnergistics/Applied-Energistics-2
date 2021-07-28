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

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;

/**
 * Extends {@link OreFeature} by also allowing for a replacement chance. In addition, the feature will check every block
 * in the chunk.
 */
public class ChargedQuartzOreFeature extends Feature<ChargedQuartzOreConfig> {

    public static final ChargedQuartzOreFeature INSTANCE = new ChargedQuartzOreFeature(ChargedQuartzOreConfig.CODEC);

    private ChargedQuartzOreFeature(Codec<ChargedQuartzOreConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ChargedQuartzOreConfig> context) {
        var pos = context.origin();
        var level = context.level();
        var config = context.config();

        ChunkPos chunkPos = new ChunkPos(pos);

        MutableBlockPos bpos = new MutableBlockPos();
        int height = level.getHeight(Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
        ChunkAccess chunk = level.getChunk(pos);
        for (int y = 0; y < height; y++) {
            bpos.setY(y);
            for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
                bpos.setX(x);
                for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                    bpos.setZ(z);
                    if (chunk.getBlockState(bpos).getBlock() == config.target.getBlock()
                            && context.random().nextFloat() < config.chance) {
                        chunk.setBlockState(bpos, config.state, false);
                    }
                }
            }
        }

        return true;
    }
}
