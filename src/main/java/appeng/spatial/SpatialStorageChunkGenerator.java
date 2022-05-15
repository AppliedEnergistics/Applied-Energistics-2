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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep.Carving;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import appeng.core.definitions.AEBlocks;

/**
 * Chunk generator the spatial storage level.
 */
public class SpatialStorageChunkGenerator extends ChunkGenerator {

    public static final int MIN_Y = 0;

    public static final int HEIGHT = 256;

    /**
     * This codec is necessary to restore the actual instance of the Biome we use, since it is sources from the dynamic
     * registries and <em>must be the same object as in the registry!</em>.
     * <p>
     * If it was not the same object, then the Object->ID lookup would fail since it uses an identity hashmap
     * internally.
     */
    public static final Codec<SpatialStorageChunkGenerator> CODEC = RecordCodecBuilder
            .create(instance -> commonCodec(instance)
                    .and(RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY)
                            .forGetter(source -> source.biomeRegistry))
                    .apply(instance, instance.stable(SpatialStorageChunkGenerator::new)));

    private final Registry<Biome> biomeRegistry;

    private final NoiseColumn columnSample;

    private final BlockState defaultBlockState;

    public SpatialStorageChunkGenerator(Registry<StructureSet> structureSets, Registry<Biome> biomeRegistry) {
        super(structureSets, Optional.of(HolderSet.direct()), createBiomeSource(biomeRegistry));
        this.defaultBlockState = AEBlocks.MATRIX_FRAME.block().defaultBlockState();
        this.biomeRegistry = biomeRegistry;

        // Vertical sample is mostly used for Feature generation, for those purposes
        // we're all filled with matrix blocks
        BlockState[] columnSample = new BlockState[HEIGHT];
        Arrays.fill(columnSample, this.defaultBlockState);
        this.columnSample = new NoiseColumn(MIN_Y, columnSample);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    private static FixedBiomeSource createBiomeSource(Registry<Biome> biomeRegistry) {
        return new FixedBiomeSource(biomeRegistry.getOrCreateHolder(SpatialStorageDimensionIds.BIOME_KEY));
    }

    @Override
    public int getGenDepth() {
        return 256;
    }

    @Override
    public int getMinY() {
        return MIN_Y;
    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState,
            ChunkAccess chunk) {
        this.fillChunk(chunk);
        chunk.setUnsaved(false);
    }

    private void fillChunk(ChunkAccess chunk) {
        MutableBlockPos mutPos = new MutableBlockPos();
        for (int cx = 0; cx < 16; cx++) {
            mutPos.setX(cx);
            for (int cz = 0; cz < 16; cz++) {
                // FIXME: It's likely a bad idea to fill Y in the inner-loop given the storage
                // layout of chunks
                mutPos.setZ(cz);
                for (int cy = 0; cy < HEIGHT; cy++) {
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
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState,
            StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getBaseHeight(int i, int j, Types types, LevelHeightAccessor levelHeightAccessor,
            RandomState randomState) {
        return MIN_Y;
    }

    @Override
    public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return columnSample;
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager,
            StructureManager structureManager, ChunkAccess chunkAccess, Carving carving) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
    }
}
