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

import java.util.function.Predicate;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;

import appeng.api.features.AEWorldGen;
import appeng.api.features.AEWorldGenType;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.worldgen.meteorite.MeteoriteStructure;

public final class InitBiomeModifications {

    private InitBiomeModifications() {
    }

    public static void init() {
        if (AEConfig.instance().isGenerateMeteorites()) {
            BiomeModifications.addStructure(
                    shouldGenerateIn(AEWorldGenType.METEORITES),
                    MeteoriteStructure.KEY);
        }

        if (AEConfig.instance().isGenerateQuartzOre()) {
            BiomeModifications.addFeature(
                    shouldGenerateIn(AEWorldGenType.CERTUS_QUARTZ),
                    Decoration.UNDERGROUND_ORES,
                    WorldgenIds.PLACED_QUARTZ_ORE_KEY);
        }
    }

    /**
     * @return A predicate that returns true if the modifier should apply to the given biome.
     */
    private static Predicate<BiomeSelectionContext> shouldGenerateIn(AEWorldGenType type) {
        return context -> {
            var id = context.getBiomeKey().location();

            var category = context.getBiome().getBiomeCategory();
            if (category == Biome.BiomeCategory.THEEND || category == Biome.BiomeCategory.NETHER
                    || category == Biome.BiomeCategory.NONE) {
                AELog.debug("Not generating %s in %s because it's of category %s", type, id, category);
                return false;
            }

            if (AEWorldGen.isWorldGenDisabledForBiome(type, id)) {
                AELog.debug("Not generating %s in %s because the biome is blacklisted by another mod or the config",
                        type, id);
                return false;
            }

            return true;
        };
    }

}
