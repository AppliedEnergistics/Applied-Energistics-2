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

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.Predicates;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.core.AEConfig;
import appeng.core.definitions.AEBlocks;
import appeng.mixins.feature.ConfiguredFeaturesAccessor;
import appeng.worldgen.ChargedQuartzOreConfig;
import appeng.worldgen.ChargedQuartzOreFeature;

public final class InitFeatures {

    private InitFeatures() {
    }

    public static void init(IForgeRegistry<Feature<?>> registry) {
        // Tell Minecraft about our charged quartz ore feature
        ChargedQuartzOreFeature.INSTANCE.setRegistryName(WorldgenIds.CHARGED_QUARTZ_ORE);
        registry.register(ChargedQuartzOreFeature.INSTANCE);

        // Register the configured versions of our features
        registerQuartzOreFeature();
        registerChargedQuartzOreFeature();
    }

    private static void registerQuartzOreFeature() {
        // Tell Minecraft about our configured quartz ore feature
        BlockState quartzOreState = AEBlocks.QUARTZ_ORE.block().defaultBlockState();
        ConfiguredFeaturesAccessor.register(WorldgenIds.QUARTZ_ORE.toString(), Feature.ORE
                .configured(
                        new OreConfiguration(Predicates.NATURAL_STONE, quartzOreState,
                                AEConfig.instance().getQuartzOresPerCluster()))
                .decorated(FeatureDecorator.RANGE.configured(new RangeDecoratorConfiguration(UniformHeight.of(
                        VerticalAnchor.aboveBottom(12),
                        VerticalAnchor.aboveBottom(72)
                ))))
                .squared()
                .count(AEConfig.instance().getQuartzOresClusterAmount()));
    }

    private static void registerChargedQuartzOreFeature() {
        BlockState quartzOreState = AEBlocks.QUARTZ_ORE.block().defaultBlockState();
        BlockState chargedQuartzOreState = AEBlocks.QUARTZ_ORE_CHARGED.block()
                .defaultBlockState();
        ConfiguredFeaturesAccessor.register(WorldgenIds.CHARGED_QUARTZ_ORE.toString(),
                ChargedQuartzOreFeature.INSTANCE
                        .configured(new ChargedQuartzOreConfig(quartzOreState, chargedQuartzOreState,
                                AEConfig.instance().getSpawnChargedChance()))
                        .decorated(FeatureDecorator.NOPE.configured(NoneDecoratorConfiguration.INSTANCE)));
    }

}
