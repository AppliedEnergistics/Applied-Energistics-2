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

import appeng.api.ids.AETags;
import appeng.datagen.providers.IAE2DataProvider;

public class FluidTagsProvider extends net.minecraft.data.tags.FluidTagsProvider implements IAE2DataProvider {

    public FluidTagsProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void addTags() {
        // Provide an empty fluid blacklist
        tag(AETags.ANNIHILATION_PLANE_FLUID_BLACKLIST);
    }
}
