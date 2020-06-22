/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2020, AlgorithmX2, All rights reserved.
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
package appeng.worldgen.meteorite;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.Heightmap.Type;

import appeng.core.AppEng;
import appeng.worldgen.meteorite.fallout.FalloutMode;

/**
 * Makes decisions about spawning meteorites in the world.
 */
public class MeteoriteSpawner {

    private static final ResourceLocation TAG_VALID_SPAWN = new ResourceLocation(AppEng.MOD_ID,
            "meteorite/valid_spawn");

    private static final ResourceLocation TAG_INVALID_SPAWN = new ResourceLocation(AppEng.MOD_ID,
            "meteorite/invalid_spawn");

    private final Tag<Block> validSpawnTag;

    private final Tag<Block> invalidSpawnTag;

    private final Tag<Block> sandTag;

    public MeteoriteSpawner() {
        this.validSpawnTag = BlockTags.getCollection().getOrCreate(TAG_VALID_SPAWN);
        this.invalidSpawnTag = BlockTags.getCollection().getOrCreate(TAG_INVALID_SPAWN);
        this.sandTag = BlockTags.getCollection().getOrCreate(new ResourceLocation("minecraft:sand"));
    }

    public PlacedMeteoriteSettings trySpawnMeteoriteAtSuitableHeight(IWorldReader world, BlockPos startPos,
            float coreRadius, CraterType craterType, boolean pureCrater, boolean worldGen) {
        int stepSize = Math.min(5, (int) Math.ceil(coreRadius) + 1);
        int minY = 10 + stepSize;
        BlockPos.Mutable mutablePos = new BlockPos.Mutable(startPos);

        // Place the center on the first solid ground block,
        // but move it down a little to embed the meteorite more in the ground
        final boolean isOcean = world.getBiome(startPos).getCategory() == Category.OCEAN;
        final Type heightmapType;

        if (isOcean) {
            heightmapType = worldGen ? Heightmap.Type.OCEAN_FLOOR_WG : Heightmap.Type.OCEAN_FLOOR;
        } else {
            heightmapType = worldGen ? Heightmap.Type.WORLD_SURFACE_WG : Heightmap.Type.WORLD_SURFACE;
        }

        int startY = world.getHeight(heightmapType, startPos).getY() - stepSize / 2;
        mutablePos.setY(startY);

        while (mutablePos.getY() > minY) {
            PlacedMeteoriteSettings spawned = trySpawnMeteorite(world, mutablePos, coreRadius, craterType, pureCrater);
            if (spawned != null) {
                return spawned;
            }

            mutablePos.setY(mutablePos.getY() - stepSize);
        }

        return null;
    }

    @Nullable
    public PlacedMeteoriteSettings trySpawnMeteorite(IWorldReader world, BlockPos pos, float coreRadius,
            CraterType craterType, boolean pureCrater) {
        if (!areSurroundingsSuitable(world, pos)) {
            return null;
        }

        // we can spawn here!
        int skyMode = countBlockWithSkyLight(world, pos);
        boolean placeCrater = skyMode > 10;

        boolean solid = !isAirBelowSpawnPoint(world, pos);

        if (!solid || placeCrater) {
            // craterType = CraterType.NONE;
            // TODO: WHAT
        }

        FalloutMode fallout = getFalloutFromBaseBlock(world.getBlockState(pos));

        return new PlacedMeteoriteSettings(pos, coreRadius, craterType, fallout, pureCrater);
    }

    private static boolean isAirBelowSpawnPoint(IWorldReader w, BlockPos pos) {
        BlockPos.Mutable testPos = new BlockPos.Mutable(pos);
        for (int j = pos.getY() - 15; j < pos.getY() - 1; j++) {
            testPos.setY(j);
            if (w.isAirBlock(testPos)) {
                return true;
            }
        }
        return false;
    }

    private int countBlockWithSkyLight(IWorldReader w, BlockPos pos) {
        int skyMode = 0;

        BlockPos.Mutable testPos = new BlockPos.Mutable();
        for (int i = pos.getX() - 15; i < pos.getX() + 15; i++) {
            testPos.setX(i);
            for (int j = pos.getY() - 15; j < pos.getY() + 11; j++) {
                testPos.setY(j);
                for (int k = pos.getZ() - 15; k < pos.getZ() + 15; k++) {
                    testPos.setZ(k);
                    if (w.canBlockSeeSky(testPos)) {
                        skyMode++;
                    }
                }
            }
        }
        return skyMode;
    }

    private boolean areSurroundingsSuitable(IWorldReader w, BlockPos pos) {
        int realValidBlocks = 0;

        BlockPos.Mutable testPos = new BlockPos.Mutable();
        for (int i = pos.getX() - 6; i < pos.getX() + 6; i++) {
            testPos.setX(i);
            for (int j = pos.getY() - 6; j < pos.getY() + 6; j++) {
                testPos.setY(j);
                for (int k = pos.getZ() - 6; k < pos.getZ() + 6; k++) {
                    testPos.setZ(k);
                    Block block = w.getBlockState(testPos).getBlock();
                    if (block.isIn(validSpawnTag)) {
                        realValidBlocks++;
                    }
                }
            }
        }

        int validBlocks = 0;
        for (int i = pos.getX() - 15; i < pos.getX() + 15; i++) {
            testPos.setX(i);
            for (int j = pos.getY() - 15; j < pos.getY() + 15; j++) {
                testPos.setY(j);
                for (int k = pos.getZ() - 15; k < pos.getZ() + 15; k++) {
                    testPos.setZ(k);
                    Block testBlk = w.getBlockState(testPos).getBlock();
                    if (testBlk.isIn(invalidSpawnTag)) {
                        return false;
                    }
                    if (testBlk.isIn(validSpawnTag)) {
                        validBlocks++;
                    }
                }
            }
        }

        final int minBlocks = 200;
        return validBlocks > minBlocks && realValidBlocks > 80;
    }

    private FalloutMode getFalloutFromBaseBlock(BlockState blockState) {
        if (blockState.getBlock().isIn(sandTag)) {
            return FalloutMode.SAND;
        } else if (blockState.getBlock() == Blocks.TERRACOTTA) {
            return FalloutMode.TERRACOTTA;
        } else if (blockState.getMaterial() == Material.SNOW || blockState.getMaterial() == Material.SNOW_BLOCK
                || blockState.getMaterial() == Material.ICE || blockState.getMaterial() == Material.PACKED_ICE) {
            return FalloutMode.ICE_SNOW;
        } else {
            return FalloutMode.DEFAULT;
        }
    }

}
