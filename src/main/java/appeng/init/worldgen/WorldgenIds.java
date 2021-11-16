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

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import appeng.core.AppEng;

/**
 * IDS used for level generation features.
 */
final class WorldgenIds {

    private WorldgenIds() {
    }

    /**
     * ID of the {@link ConfiguredFeature} that generates quartz ore.
     */
    public static final ResourceLocation QUARTZ_ORE = AppEng.makeId("quartz_ore");

    /**
     * Resource key of the {@link ConfiguredFeature} that generates quartz ore.
     */
    public static final ResourceKey<ConfiguredFeature<?, ?>> QUARTZ_ORE_KEY = ResourceKey
            .create(Registry.CONFIGURED_FEATURE_REGISTRY, QUARTZ_ORE);

    /**
     * Resource key of the {@link PlacedFeature} that generates quartz ore.
     */
    public static final ResourceKey<PlacedFeature> PLACED_QUARTZ_ORE_KEY = ResourceKey
            .create(Registry.PLACED_FEATURE_REGISTRY, QUARTZ_ORE);

}
