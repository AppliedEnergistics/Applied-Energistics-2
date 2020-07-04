/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

import javax.annotation.Nullable;

import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.fabricmc.api.EnvType;
import net.minecraftforge.client.IRenderHandler;

import appeng.client.render.SpatialSkyRender;

public class StorageCellDimension extends Dimension {

    // A region file is 512x512 blocks (32x32 chunks),
    // to avoid creating the 4 regions around 0,0,0,
    // we move the origin to the middle of region 0,0
    public static final BlockPos REGION_CENTER = new BlockPos(512 / 2, 64, 512 / 2);

    public StorageCellDimension(World world, DimensionType dimensionType) {
        // FIXME: check light value
        super(world, dimensionType, 1.0f);
    }

    @Override
    public ChunkGenerator createChunkGenerator() {
        return new StorageChunkGenerator(this.world);
    }

    @Override
    public float calculateCelestialAngle(final long par1, final float par3) {
        return 0;
    }

    @Override
    public boolean isSurfaceWorld() {
        return false;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public float[] calcSunriseSunsetColors(final float celestialAngle, final float partialTicks) {
        return null;
    }

    @Override
    public Vec3d getFogColor(final float par1, final float par2) {
        return new Vec3d(0.07, 0.07, 0.07);
    }

    @Override
    public boolean canRespawnHere() {
        return false;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean isSkyColored() {
        return true;
    }

    @Override
    public boolean doesXZShowFog(final int par1, final int par2) {
        return false;
    }

    @Override
    public IRenderHandler getSkyRenderer() {
        return SpatialSkyRender.getInstance();
    }

    @Override
    public boolean isDaytime() {
        return false;
    }

    @Override
    public BlockPos getSpawnCoordinate() {
        return REGION_CENTER;
    }

    @Override
    public boolean isHighHumidity(final BlockPos pos) {
        return false;
    }

    @Override
    public boolean canDoLightning(final Chunk chunk) {
        return false;
    }

    @Override
    public boolean canDoRainSnowIce(Chunk chunk) {
        return false;
    }

    @Nullable
    @Override
    public BlockPos findSpawn(ChunkPos chunkPosIn, boolean checkValid) {
        return getSpawnCoordinate();
    }

    @Nullable
    @Override
    public BlockPos findSpawn(int posX, int posZ, boolean checkValid) {
        return getSpawnCoordinate();
    }

}
