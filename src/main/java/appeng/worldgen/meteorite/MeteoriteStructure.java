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

package appeng.worldgen.meteorite;

import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature.StructureStartFactory;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import appeng.core.AppEng;

public class MeteoriteStructure extends StructureFeature<NoneFeatureConfiguration> {

    public static final ResourceLocation ID = AppEng.makeId("meteorite");

    public static final ResourceKey<ConfiguredStructureFeature<?, ?>> KEY = ResourceKey
            .create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, ID);

    public static final StructureFeature<NoneFeatureConfiguration> INSTANCE = new MeteoriteStructure(
            NoneFeatureConfiguration.CODEC);

    public static final ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> CONFIGURED_INSTANCE = INSTANCE
            .configured(NoneFeatureConfiguration.INSTANCE);

    public MeteoriteStructure(Codec<NoneFeatureConfiguration> configCodec) {
        super(configCodec);
    }

    @Override
    protected boolean isFeatureChunk(ChunkGenerator generator, BiomeSource biomeSource, long seed,
            WorldgenRandom randIn, ChunkPos chunkPos, Biome biome, ChunkPos chunkPos2,
            NoneFeatureConfiguration featureConfig, LevelHeightAccessor heightAccessor) {
        return randIn.nextBoolean();
    }

    @Override
    public StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return MeteoriteStructureStart::new;
    }

}
