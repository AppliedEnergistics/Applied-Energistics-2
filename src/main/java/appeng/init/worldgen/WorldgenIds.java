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

import appeng.core.AppEng;

/**
 * IDS used for world generation features.
 */
final class WorldgenIds {

    private WorldgenIds() {
    }

    /**
     * ID of the {@link net.minecraft.world.gen.feature.ConfiguredFeature} that generates quartz ore.
     */
    public static final ResourceLocation QUARTZ_ORE = AppEng.makeId("quartz_ore");

    /**
     * ID of the {@link net.minecraft.world.gen.feature.ConfiguredFeature} and
     * {@link net.minecraft.world.gen.feature.Feature} that generate charged quartz ore.
     */
    public static final ResourceLocation CHARGED_QUARTZ_ORE = AppEng.makeId("charged_quartz_ore");

}
