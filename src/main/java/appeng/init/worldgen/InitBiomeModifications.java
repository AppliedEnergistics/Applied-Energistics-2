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

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraftforge.event.world.BiomeLoadingEvent;

import appeng.api.features.AEFeature;
import appeng.api.features.IWorldGen;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.worldgen.meteorite.MeteoriteStructure;

public final class InitBiomeModifications {

    private InitBiomeModifications() {
    }

    public static void init(BiomeLoadingEvent e) {
        addMeteoriteWorldGen(e);
        addQuartzWorldGen(e);
    }

    private static void addMeteoriteWorldGen(BiomeLoadingEvent e) {
        if (shouldGenerateIn(e.getName(), AEFeature.METEORITE_WORLD_GEN, IWorldGen.WorldGenType.METEORITES,
                e.getCategory())) {
            e.getGeneration().withStructure(MeteoriteStructure.CONFIGURED_INSTANCE);
        }
    }

    private static void addQuartzWorldGen(BiomeLoadingEvent e) {
        if (shouldGenerateIn(e.getName(), AEFeature.CERTUS_QUARTZ_WORLD_GEN, IWorldGen.WorldGenType.CERTUS_QUARTZ,
                e.getCategory())) {

            ConfiguredFeature<?, ?> quartzOreFeature = getConfiguredFeature(WorldgenIds.QUARTZ_ORE);

            e.getGeneration().withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, quartzOreFeature);

            if (AEConfig.instance().isFeatureEnabled(AEFeature.CHARGED_CERTUS_ORE)) {
                ConfiguredFeature<?, ?> chargedQuartzOreFeature = getConfiguredFeature(WorldgenIds.CHARGED_QUARTZ_ORE);
                e.getGeneration().withFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION,
                        chargedQuartzOreFeature);
            }
        }
    }

    private static ConfiguredFeature<?, ?> getConfiguredFeature(ResourceLocation id) {
        return WorldGenRegistries.CONFIGURED_FEATURE.getOptional(id)
                .orElseThrow(() -> new RuntimeException("Configured feature " + id + " is not registered"));
    }

    private static boolean shouldGenerateIn(ResourceLocation id,
            AEFeature feature,
            IWorldGen.WorldGenType worldGenType,
            Biome.Category category) {
        if (id == null) {
            return false; // We don't add to unnamed biomes
        }

        if (!AEConfig.instance().isFeatureEnabled(feature)) {
            AELog.debug("Not generating %s in %s because the feature is disabled", feature, id);
            return false;
        }

        if (category == Biome.Category.THEEND || category == Biome.Category.NETHER
                || category == Biome.Category.NONE) {
            AELog.debug("Not generating %s in %s because it's of category %s", feature, id, category);
            return false;
        }

        if (Api.instance().registries().worldgen().isWorldGenDisabledForBiome(worldGenType, id)) {
            AELog.debug("Not generating %s in %s because the biome is blacklisted by another mod or the config",
                    feature, id);
            return false;
        }

        return true;
    }

}
