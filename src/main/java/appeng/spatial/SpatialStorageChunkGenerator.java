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

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Blockreader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;

import appeng.core.Api;

/**
 * Chunk generator the spatial storage world.
 */
public class SpatialStorageChunkGenerator extends ChunkGenerator {

    private final Blockreader columnSample;

    public static final SpatialStorageChunkGenerator INSTANCE = new SpatialStorageChunkGenerator();

    public static final Codec<SpatialStorageChunkGenerator> CODEC = RecordCodecBuilder
            .create((instance) -> instance.stable(INSTANCE));

    private final BlockState defaultBlockState;

    private SpatialStorageChunkGenerator() {
        super(createBiomeProvider(), createSettings());
        this.defaultBlockState = Api.instance().definitions().blocks().matrixFrame().block().getDefaultState();

        // Vertical sample is mostly used for Feature generation, for those purposes
        // we're all filled with matrix blocks
        BlockState[] columnSample = new BlockState[256];
        Arrays.fill(columnSample, this.defaultBlockState);
        this.columnSample = new Blockreader(columnSample);
    }

    @Override
    protected Codec<? extends ChunkGenerator> func_230347_a_() {
        return CODEC;
    }

    private static BiomeProvider createBiomeProvider() {
        return new SingleBiomeProvider(SpatialStorageBiome.INSTANCE);
    }

    private static DimensionStructuresSettings createSettings() {
        return new DimensionStructuresSettings(Optional.empty(), Collections.emptyMap());
    }

    @Override
    public void generateSurface(WorldGenRegion region, IChunk chunk) {
        this.fillChunk(chunk);
        chunk.setModified(false);
    }

    private void fillChunk(IChunk chunk) {
        BlockPos.Mutable mutPos = new BlockPos.Mutable();
        for (int cx = 0; cx < 16; cx++) {
            mutPos.setX(cx);
            for (int cz = 0; cz < 16; cz++) {
                // FIXME: It's likely a bad idea to fill Y in the inner-loop given the storage
                // layout of chunks
                mutPos.setZ(cz);
                for (int cy = 0; cy < 256; cy++) {
                    mutPos.setY(cy);
                    chunk.setBlockState(mutPos, defaultBlockState, false);
                }
            }
        }
    }

    @Override
    public int func_230356_f_() {
        return 0;
    }

    @Override
    public ChunkGenerator func_230349_a_(long p_230349_1_) {
        return this;
    }

    @Override
    public void func_230352_b_(IWorld world, StructureManager accessor, IChunk chunk) {
    }

    public IBlockReader func_230348_a_(int x, int z) {
        return columnSample;
    }

    @Override
    public int getHeight(int p_222529_1_, int p_222529_2_, Heightmap.Type heightmapType) {
        return 0;
    }

    @Override
    public void func_230351_a_(WorldGenRegion region, StructureManager accessor) {
    }

    @Override
    public void func_230350_a_(long seed, BiomeManager access, IChunk chunk, GenerationStage.Carving carver) {
    }

}
