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

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.data.DataGenerator;

import appeng.core.AppEng;
import appeng.datagen.providers.IAE2DataProvider;
import appeng.worldgen.meteorite.MeteoriteStructure;

public class BiomeTagsProvider extends net.minecraft.data.tags.BiomeTagsProvider implements IAE2DataProvider {
    public BiomeTagsProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void addTags() {
        tag(ConventionTags.HAS_QUARTZ_ORE).addTag(ConventionalBiomeTags.IN_OVERWORLD);
        tag(MeteoriteStructure.BIOME_TAG_KEY).addTag(ConventionalBiomeTags.IN_OVERWORLD);
    }
}
