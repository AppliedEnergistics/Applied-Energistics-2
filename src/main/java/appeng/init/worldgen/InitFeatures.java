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

import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.Predicates;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.core.AEConfig;
import appeng.core.definitions.AEBlocks;
import appeng.mixins.feature.ConfiguredFeaturesAccessor;

public final class InitFeatures {
    private InitFeatures() {
    }

    public static void init(IForgeRegistry<Feature<?>> registry) {
        // Register the configured versions of our features
        registerQuartzOreFeature();
    }

    private static void registerQuartzOreFeature() {

        var targetList = ImmutableList.of(
                OreConfiguration.target(Predicates.STONE_ORE_REPLACEABLES,
                        AEBlocks.QUARTZ_ORE.block().defaultBlockState()),
                OreConfiguration.target(Predicates.DEEPSLATE_ORE_REPLACEABLES,
                        AEBlocks.DEEPSLATE_QUARTZ_ORE.block().defaultBlockState()));
        var config = new OreConfiguration(targetList, AEConfig.instance().getQuartzOresPerCluster());

        // Tell Minecraft about our configured quartz ore feature
        ConfiguredFeaturesAccessor.register(WorldgenIds.QUARTZ_ORE.toString(), Feature.ORE
                .configured(config)
                .decorated(FeatureDecorator.RANGE.configured(new RangeDecoratorConfiguration(UniformHeight.of(
                        VerticalAnchor.aboveBottom(12),
                        VerticalAnchor.aboveBottom(72)))))
                .squared()
                .count(AEConfig.instance().getQuartzOresClusterAmount()));
    }
}
