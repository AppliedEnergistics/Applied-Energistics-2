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

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraftforge.event.world.BiomeLoadingEvent;

import appeng.api.features.AEWorldGen;
import appeng.api.features.AEWorldGenType;
import appeng.core.AEConfig;
import appeng.core.AELog;

public final class InitBiomeModifications {

    private InitBiomeModifications() {
    }

    public static void init(BiomeLoadingEvent e) {
        addQuartzWorldGen(e);
    }

    private static void addQuartzWorldGen(BiomeLoadingEvent e) {
        if (shouldGenerateIn(e.getName(), AEConfig.instance().isGenerateQuartzOre(),
                AEWorldGenType.CERTUS_QUARTZ,
                e.getCategory())) {

            var quartzOrePlaced = BuiltinRegistries.PLACED_FEATURE.getHolderOrThrow(WorldgenIds.PLACED_QUARTZ_ORE_KEY);
            e.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, quartzOrePlaced);
        }
    }

    public static boolean shouldGenerateIn(ResourceLocation id,
            boolean enabled,
            AEWorldGenType worldGenType,
            BiomeCategory category) {
        if (id == null) {
            return false; // We don't add to unnamed biomes
        }

        if (!enabled) {
            AELog.debug("Not generating %s in %s because it is disabled in the config", worldGenType, id);
            return false;
        }

        if (category == BiomeCategory.THEEND || category == BiomeCategory.NETHER
                || category == BiomeCategory.NONE) {
            AELog.debug("Not generating %s in %s because it's of category %s", worldGenType, id, category);
            return false;
        }

        if (AEWorldGen.isWorldGenDisabledForBiome(worldGenType, id)) {
            AELog.debug("Not generating %s in %s because the biome is blacklisted by another mod or the config",
                    worldGenType, id);
            return false;
        }

        return true;
    }

}
