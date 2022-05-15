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

import appeng.core.AEConfig;
import appeng.datagen.providers.tags.ConventionTags;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;

public final class InitBiomeModifications {

    private InitBiomeModifications() {
    }

    public static void init() {
        if (AEConfig.instance().isGenerateQuartzOre()) {
            BiomeModifications.addFeature(
                    BiomeSelectors.tag(ConventionTags.HAS_QUARTZ_ORE),
                    Decoration.UNDERGROUND_ORES,
                    WorldgenIds.PLACED_QUARTZ_ORE_KEY);
        }
    }

}
