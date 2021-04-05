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

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;

import appeng.core.AppEng;

public class MeteoriteStructure extends Structure<NoFeatureConfig> {

    public static final ResourceLocation ID = AppEng.makeId("meteorite");

    public static final Structure<NoFeatureConfig> INSTANCE = new MeteoriteStructure(NoFeatureConfig.field_236558_a_);

    public static final StructureFeature<NoFeatureConfig, ? extends Structure<NoFeatureConfig>> CONFIGURED_INSTANCE = INSTANCE
            .withConfiguration(NoFeatureConfig.field_236559_b_);

    public MeteoriteStructure(Codec<NoFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    protected boolean func_230363_a_(ChunkGenerator generator, BiomeProvider biomeSource, long seed,
            SharedSeedRandom randIn, int chunkX, int chunkZ, Biome biome, ChunkPos chunkPos2,
            NoFeatureConfig featureConfig) {
        return randIn.nextBoolean();
    }

    @Override
    public IStartFactory<NoFeatureConfig> getStartFactory() {
        return MeteoriteStructureStart::new;
    }

}
