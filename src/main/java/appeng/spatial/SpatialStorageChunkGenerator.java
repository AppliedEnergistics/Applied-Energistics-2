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

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.StructureSettings;

import appeng.core.definitions.AEBlocks;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.levelgen.GenerationStep.Carving;
import net.minecraft.world.level.levelgen.Heightmap.Types;

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
    public static final Codec<SpatialStorageChunkGenerator> CODEC = RegistryLookupCodec
            .create(Registry.BIOME_REGISTRY)
            .xmap(SpatialStorageChunkGenerator::new, SpatialStorageChunkGenerator::getBiomeRegistry).stable().codec();

    private final Registry<Biome> biomeRegistry;

    private final NoiseColumn columnSample;

    private final BlockState defaultBlockState;

    public SpatialStorageChunkGenerator(Registry<Biome> biomeRegistry) {
        super(createBiomeSource(biomeRegistry), createSettings());
        this.defaultBlockState = AEBlocks.MATRIX_FRAME.block().defaultBlockState();
        this.biomeRegistry = biomeRegistry;

        // Vertical sample is mostly used for Feature generation, for those purposes
        // we're all filled with matrix blocks
        BlockState[] columnSample = new BlockState[256];
        Arrays.fill(columnSample, this.defaultBlockState);
        this.columnSample = new NoiseColumn(columnSample);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    private static FixedBiomeSource createBiomeSource(Registry<Biome> biomeRegistry) {
        return new FixedBiomeSource(biomeRegistry.getOrThrow(SpatialStorageDimensionIds.BIOME_KEY));
    }

    public Registry<Biome> getBiomeRegistry() {
        return biomeRegistry;
    }

    private static StructureSettings createSettings() {
        return new StructureSettings(Optional.empty(), Collections.emptyMap());
    }

    @Override
    public void buildSurfaceAndBedrock(WorldGenRegion region, ChunkAccess chunk) {
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
    public ChunkGenerator withSeed(long p_230349_1_) {
        return this;
    }

    @Override
    public void fillFromNoise(LevelAccessor world, StructureFeatureManager accessor, ChunkAccess chunk) {
    }

    @Override
    public BlockGetter getBaseColumn(int x, int z) {
        return columnSample;
    }

    @Override
    public int getBaseHeight(int x, int z, Types heightmapType) {
        return 0;
    }

    @Override
    public void applyBiomeDecoration(WorldGenRegion region, StructureFeatureManager accessor) {
    }

    @Override
    public void applyCarvers(long seed, BiomeManager access, ChunkAccess chunk, Carving carver) {
    }

}
