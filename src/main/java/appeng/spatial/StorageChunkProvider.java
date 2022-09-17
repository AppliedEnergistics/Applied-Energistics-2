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

package appeng.spatial;


import appeng.api.AEApi;
import appeng.core.AppEng;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkGeneratorOverworld;

import java.util.ArrayList;
import java.util.List;


public class StorageChunkProvider extends ChunkGeneratorOverworld {

    private final World world;

    public StorageChunkProvider(final World world, final long i) {
        super(world, i, false, null);
        this.world = world;
    }

    @Override
    public Chunk generateChunk(final int x, final int z) {
        final Chunk chunk = new Chunk(this.world, x, z);

        final byte[] biomes = chunk.getBiomeArray();
        Biome biome = AppEng.instance().getStorageBiome();
        byte biomeId = (byte) Biome.getIdForBiome(biome);

        for (int k = 0; k < biomes.length; ++k) {
            biomes[k] = biomeId;
        }

        AEApi.instance().definitions().blocks().matrixFrame().maybeBlock().ifPresent(block -> this.fillChunk(chunk, block.getDefaultState()));

        chunk.setModified(false);

        if (!chunk.isTerrainPopulated()) {
            chunk.setTerrainPopulated(true);
            chunk.resetRelightChecks();
        }

        return chunk;
    }

    private void fillChunk(Chunk chunk, IBlockState defaultState) {
        for (int cx = 0; cx < 16; cx++) {
            for (int cz = 0; cz < 16; cz++) {
                for (int cy = 0; cy < 256; cy++) {
                    chunk.setBlockState(new BlockPos(cx, cy, cz), defaultState);
                }
            }
        }
    }

    @Override
    public void populate(final int par2, final int par3) {

    }

    @Override
    public List getPossibleCreatures(final EnumCreatureType creatureType, final BlockPos pos) {
        return new ArrayList();
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return false;
    }

    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean p_180513_4_) {
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {

    }
}
