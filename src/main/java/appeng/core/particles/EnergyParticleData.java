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

package appeng.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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

    public static void add(Level level, Vec3 center, RandomSource random) {
        var xOff = (random.nextFloat() - 0.5f) * 0.7f;
        var yOff = (random.nextFloat() - 0.5f) * 0.7f;
        var zOff = (random.nextFloat() - 0.5f) * 0.7f;
        level.addParticle(
                EnergyParticleData.FOR_BLOCK,
                false,
                true,
                center.x() + xOff,
                center.y() + yOff,
                center.z() + zOff,
                -xOff * 0.1,
                -yOff * 0.1,
                -zOff * 0.1);
    }
}
