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
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;

/**
 * The single biome used within the spatial storage world.
 */
public class SpatialStorageBiome {

    public static final Biome INSTANCE = new Biome.Builder()
            .func_242457_a(new BiomeGenerationSettings.Builder()
                    .func_242517_a(new ConfiguredSurfaceBuilder<>(SurfaceBuilder.NOPE, SurfaceBuilder.STONE_STONE_GRAVEL_CONFIG))
                    .func_242508_a())
            .precipitation(Biome.RainType.NONE).category(Biome.Category.NONE).depth(0).scale(1)
            // Copied from the vanilla void biome
            .temperature(0.5F).downfall(0.5F).func_235097_a_(new BiomeAmbience.Builder().setWaterColor(4159204)
                    .setWaterFogColor(329011).setFogColor(0).func_242539_d(0x111111).build())
            .func_242458_a(new MobSpawnInfo.Builder().func_242572_a(0).func_242577_b()).func_242455_a();

}
