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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.gen.surfacebuilder.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;

public class StorageCellBiome extends Biome {

    public static final StorageCellBiome INSTANCE = new StorageCellBiome();

    public StorageCellBiome() {
        super(new Biome.Settings()
                .surfaceBuilder(new ConfiguredSurfaceBuilder<>(SurfaceBuilder.NOPE, SurfaceBuilder.STONE_CONFIG))
                .precipitation(Precipitation.NONE).category(Category.NONE).depth(0).scale(1)
                // Copied from the vanilla void biome
                .temperature(0.5F).downfall(0.5F)
                .effects(new BiomeEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(0).build())
                .parent(null));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public int getSkyColor() {
        return 0x111111;
    }

}
