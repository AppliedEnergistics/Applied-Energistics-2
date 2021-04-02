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

package appeng.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.ReplaceBlockConfig;

/**
 * Extends a {@link ReplaceBlockConfig} with a chance.
 */
public class ChargedQuartzOreConfig implements IFeatureConfig {

    public static final Codec<ChargedQuartzOreConfig> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(BlockState.CODEC.fieldOf("target").forGetter((config) -> config.target),
                    BlockState.CODEC.fieldOf("state").forGetter((config) -> config.state),
                    Codec.FLOAT.fieldOf("chance").orElse(0f).forGetter((config) -> config.chance))
            .apply(instance, ChargedQuartzOreConfig::new));

    public final BlockState target;
    public final BlockState state;
    public final float chance;

    public ChargedQuartzOreConfig(BlockState target, BlockState state, float chance) {
        this.target = target;
        this.state = state;
        this.chance = chance;
    }

}
