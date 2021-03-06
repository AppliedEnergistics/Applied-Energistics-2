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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.util.dynamic.RegistryLookupCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.chunk.VerticalBlockSample;

import appeng.core.Api;

/**
 * Chunk generator the spatial storage world.
 */
public class SpatialStorageChunkGenerator extends ChunkGenerator {

    /**
     * This codec is necessary to restore the actual instance of the Biome we use, since it is sources from the dynamic
     * registries and <em>must be the same object as in the registry!</em>.
     * <p>
     * If it was not the same object, then the Object->ID lookup would fail since it uses an identity hashmap
     * internally.
     */
    public static final Codec<SpatialStorageChunkGenerator> CODEC = RegistryLookupCodec.of(Registry.BIOME_KEY)
            .xmap(SpatialStorageChunkGenerator::new, SpatialStorageChunkGenerator::getBiomeRegistry).stable().codec();

    private final Registry<Biome> biomeRegistry;

    private final VerticalBlockSample columnSample;

    private final BlockState defaultBlockState;

    public SpatialStorageChunkGenerator(Registry<Biome> biomeRegistry) {
        super(createBiomeSource(biomeRegistry), createSettings());
        this.defaultBlockState = Api.instance().definitions().blocks().matrixFrame().block().getDefaultState();
        this.biomeRegistry = biomeRegistry;

        // Vertical sample is mostly used for Feature generation, for those purposes
        // we're all filled with matrix blocks
        BlockState[] columnSample = new BlockState[256];
        Arrays.fill(columnSample, this.defaultBlockState);
        this.columnSample = new VerticalBlockSample(0, columnSample);
    }

    @Override
    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    private static FixedBiomeSource createBiomeSource(Registry<Biome> biomeRegistry) {
        return new FixedBiomeSource(biomeRegistry.getOrThrow(SpatialStorageDimensionIds.BIOME_KEY));
    }

    public Registry<Biome> getBiomeRegistry() {
        return biomeRegistry;
    }

    private static StructuresConfig createSettings() {
        return new StructuresConfig(Optional.empty(), Collections.emptyMap());
    }

    @Override
    public void buildSurface(ChunkRegion region, Chunk chunk) {
        this.fillChunk(chunk);
        chunk.setShouldSave(false);
    }

    private void fillChunk(Chunk chunk) {
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
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world) {
        return 0;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        return this;
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, StructureAccessor accessor, Chunk chunk) {
        CompletableFuture<Chunk> chunkCompletableFuture = new CompletableFuture();
        chunkCompletableFuture.complete(chunk);
        return chunkCompletableFuture;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world) {
        return columnSample;
    }

    @Override
    public void generateFeatures(ChunkRegion region, StructureAccessor accessor) {
    }

    @Override
    public void carve(long seed, BiomeAccess access, Chunk chunk, GenerationStep.Carver carver) {
    }

}
