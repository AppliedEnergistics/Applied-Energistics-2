/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.client.render.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record EnergyParticleData(boolean forItem, Direction direction) implements ParticleOptions {
    public static final MapCodec<EnergyParticleData> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.BOOL.fieldOf("forItem").forGetter(EnergyParticleData::forItem),
            Direction.CODEC.fieldOf("direction").forGetter(EnergyParticleData::direction))
            .apply(builder, EnergyParticleData::new));

    public static final StreamCodec<FriendlyByteBuf, EnergyParticleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            EnergyParticleData::forItem,
            Direction.STREAM_CODEC,
            EnergyParticleData::direction,
            EnergyParticleData::new);

    public static final EnergyParticleData FOR_BLOCK = new EnergyParticleData(false, Direction.UP);

    @Override
    public ParticleType<?> getType() {
        return ParticleTypes.ENERGY;
    }
}
