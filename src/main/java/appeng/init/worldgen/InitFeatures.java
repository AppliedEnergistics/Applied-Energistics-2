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

import com.google.common.collect.ImmutableList;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;

import appeng.core.AEConfig;
import appeng.core.definitions.AEBlocks;

public final class InitFeatures {
    private InitFeatures() {
    }

    public static void init(Registry<Feature<?>> registry) {
        // Register the configured versions of our features
        registerQuartzOreFeature();
    }

    private static void registerQuartzOreFeature() {

        var targetList = ImmutableList.of(
                OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES,
                        AEBlocks.QUARTZ_ORE.block().defaultBlockState()),
                OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES,
                        AEBlocks.DEEPSLATE_QUARTZ_ORE.block().defaultBlockState()));

        // Tell Minecraft about our configured quartz ore feature
        var configuredQuartz = Registry.register(
                BuiltinRegistries.CONFIGURED_FEATURE,
                WorldgenIds.QUARTZ_ORE_KEY,
                Feature.ORE
                        .configured(new OreConfiguration(targetList, AEConfig.instance().getQuartzOresPerCluster())));
        Registry.register(
                BuiltinRegistries.PLACED_FEATURE,
                WorldgenIds.PLACED_QUARTZ_ORE_KEY,
                configuredQuartz.placed(
                        OrePlacements.commonOrePlacement(
                                AEConfig.instance().getQuartzOresPerCluster(),
                                HeightRangePlacement.triangle(VerticalAnchor.absolute(-34),
                                        VerticalAnchor.absolute(36)))));
    }
}
