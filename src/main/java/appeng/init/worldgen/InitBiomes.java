/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.init.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;

import appeng.spatial.SpatialStorageDimensionIds;

public final class InitBiomes {

    private InitBiomes() {
    }

    public static void init(BootstrapContext<Biome> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var configuredCarvers = context.lookup(Registries.CONFIGURED_CARVER);

        Biome biome = new Biome.BiomeBuilder()
                .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, configuredCarvers).build())
                .hasPrecipitation(false)
                // Copied from the vanilla void biome
                .temperature(0.5F).downfall(0.5F)
                .specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(0)
                        .skyColor(0x111111).build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().creatureGenerationProbability(0).build()).build();

        context.register(SpatialStorageDimensionIds.BIOME_KEY, biome);
    }

}
