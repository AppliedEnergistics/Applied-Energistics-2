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

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import net.minecraft.world.biome.provider.SingleBiomeProviderSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;

import appeng.api.AEApi;

public class StorageChunkGenerator extends ChunkGenerator<GenerationSettings> {

    private final BlockState defaultBlockState;

    public StorageChunkGenerator(final World world) {
        super(world, createBiomeProvider(), createSettings());
        this.defaultBlockState = AEApi.instance().definitions().blocks().matrixFrame().block().getDefaultState();
    }

    private static BiomeProvider createBiomeProvider() {
        SingleBiomeProviderSettings biomeSettings = new SingleBiomeProviderSettings(null);
        biomeSettings.setBiome(StorageCellBiome.INSTANCE);
        return new SingleBiomeProvider(biomeSettings);
    }

    private static GenerationSettings createSettings() {
        return new GenerationSettings();
    }

    @Override
    public void generateSurface(WorldGenRegion region, Chunk chunk) {
        this.fillChunk(chunk);
        chunk.setModified(false);
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
    public int getGroundHeight() {
        return 0;
    }

    @Override
    public void makeBase(WorldAccess worldIn, Chunk chunkIn) {
    }

    @Override
    public int getHeight(int p_222529_1_, int p_222529_2_, Heightmap.Type heightmapType) {
        return 0;
    }

    @Override
    public void decorate(WorldGenRegion region) {
        // Do not decorate chunks at all
    }

}
