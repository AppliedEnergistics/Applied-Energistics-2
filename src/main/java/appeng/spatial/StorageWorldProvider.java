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


import appeng.client.render.SpatialSkyRender;
import appeng.core.AppEng;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class StorageWorldProvider extends WorldProvider {

    private final Biome biome;

    public StorageWorldProvider() {
        this.hasSkyLight = true;
        this.biome = AppEng.instance().getStorageBiome();
        this.biomeProvider = new BiomeProviderSingle(this.biome);
    }

    @Override
    public IChunkGenerator createChunkGenerator() {
        return new StorageChunkProvider(this.world, 0);
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
    @SideOnly(Side.CLIENT)
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
    @SideOnly(Side.CLIENT)
    public boolean isSkyColored() {
        return true;
    }

    @Override
    public boolean doesXZShowFog(final int par1, final int par2) {
        return false;
    }

    @Override
    public DimensionType getDimensionType() {
        return AppEng.instance().getStorageDimensionType();
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
    public Vec3d getSkyColor(final Entity cameraEntity, final float partialTicks) {
        return new Vec3d(0.07, 0.07, 0.07);
    }

    @Override
    public float getStarBrightness(final float par1) {
        return 0;
    }

    @Override
    public boolean canSnowAt(final BlockPos pos, final boolean checkLight) {
        return false;
    }

    @Override
    public BlockPos getSpawnCoordinate() {
        return new BlockPos(0, 0, 0);
    }

    @Override
    public boolean isBlockHighHumidity(final BlockPos pos) {
        return false;
    }

    @Override
    public boolean canDoLightning(final Chunk chunk) {
        return false;
    }

    @Override
    public Biome getBiomeForCoords(BlockPos pos) {
        return this.biome;
    }

}
