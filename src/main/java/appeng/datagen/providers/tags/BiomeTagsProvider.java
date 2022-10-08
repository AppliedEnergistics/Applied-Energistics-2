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

package appeng.datagen.providers.tags;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.data.ExistingFileHelper;

import appeng.core.AppEng;
import appeng.datagen.providers.IAE2DataProvider;
import appeng.worldgen.meteorite.MeteoriteStructure;

public class BiomeTagsProvider extends net.minecraft.data.tags.BiomeTagsProvider implements IAE2DataProvider {
    public BiomeTagsProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, AppEng.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        // Add anything but nether, end and void biomes
        tag(MeteoriteStructure.BIOME_TAG_KEY)
                .add(Biomes.PLAINS)
                .add(Biomes.SUNFLOWER_PLAINS)
                .add(Biomes.SNOWY_PLAINS)
                .add(Biomes.ICE_SPIKES)
                .add(Biomes.DESERT)
                .add(Biomes.SWAMP)
                .add(Biomes.FOREST)
                .add(Biomes.FLOWER_FOREST)
                .add(Biomes.BIRCH_FOREST)
                .add(Biomes.DARK_FOREST)
                .add(Biomes.OLD_GROWTH_BIRCH_FOREST)
                .add(Biomes.OLD_GROWTH_PINE_TAIGA)
                .add(Biomes.OLD_GROWTH_SPRUCE_TAIGA)
                .add(Biomes.TAIGA)
                .add(Biomes.SNOWY_TAIGA)
                .add(Biomes.SAVANNA)
                .add(Biomes.SAVANNA_PLATEAU)
                .add(Biomes.WINDSWEPT_HILLS)
                .add(Biomes.WINDSWEPT_GRAVELLY_HILLS)
                .add(Biomes.WINDSWEPT_FOREST)
                .add(Biomes.WINDSWEPT_SAVANNA)
                .add(Biomes.JUNGLE)
                .add(Biomes.SPARSE_JUNGLE)
                .add(Biomes.BAMBOO_JUNGLE)
                .add(Biomes.BADLANDS)
                .add(Biomes.ERODED_BADLANDS)
                .add(Biomes.WOODED_BADLANDS)
                .add(Biomes.MEADOW)
                .add(Biomes.GROVE)
                .add(Biomes.SNOWY_SLOPES)
                .add(Biomes.FROZEN_PEAKS)
                .add(Biomes.JAGGED_PEAKS)
                .add(Biomes.STONY_PEAKS)
                .add(Biomes.RIVER)
                .add(Biomes.FROZEN_RIVER)
                .add(Biomes.BEACH)
                .add(Biomes.SNOWY_BEACH)
                .add(Biomes.STONY_SHORE)
                .add(Biomes.WARM_OCEAN)
                .add(Biomes.LUKEWARM_OCEAN)
                .add(Biomes.DEEP_LUKEWARM_OCEAN)
                .add(Biomes.OCEAN)
                .add(Biomes.DEEP_OCEAN)
                .add(Biomes.COLD_OCEAN)
                .add(Biomes.DEEP_COLD_OCEAN)
                .add(Biomes.FROZEN_OCEAN)
                .add(Biomes.DEEP_FROZEN_OCEAN)
                .add(Biomes.MUSHROOM_FIELDS)
                .add(Biomes.DRIPSTONE_CAVES)
                .add(Biomes.LUSH_CAVES);
    }

    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Biome Tags";
    }
}
