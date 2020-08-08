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

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeAmbience;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The single biome used within the spatial storage world.
 */
public class SpatialStorageBiome extends Biome {

    public static final SpatialStorageBiome INSTANCE = new SpatialStorageBiome();

    public SpatialStorageBiome() {
        super(new Biome.Builder()
                .surfaceBuilder(
                        new ConfiguredSurfaceBuilder<>(SurfaceBuilder.NOPE, SurfaceBuilder.STONE_STONE_GRAVEL_CONFIG))
                .precipitation(RainType.NONE).category(Category.NONE).depth(0).scale(1)
                // Copied from the vanilla void biome
                .temperature(0.5F).downfall(0.5F).func_235097_a_(new BiomeAmbience.Builder().setWaterColor(4159204)
                        .setWaterFogColor(329011).setFogColor(0).build())
                .parent(null));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getSkyColor() {
        return 0x111111;
    }

}
