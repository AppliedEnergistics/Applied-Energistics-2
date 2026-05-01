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

package appeng.core.particles;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.joml.Vector3fc;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

/**
 * Contains the target point of the lightning arc (the source point is inferred from the particle starting position).
 */
public record LightningArcParticleData(Vector3fc target) implements ParticleOptions {
    public static final MapCodec<LightningArcParticleData> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ExtraCodecs.VECTOR3F.fieldOf("target").forGetter(LightningArcParticleData::target))
            .apply(builder, LightningArcParticleData::new));

    public static final StreamCodec<ByteBuf, LightningArcParticleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VECTOR3F,
            d -> d.target,
            LightningArcParticleData::new);

    @Override
    public ParticleType<?> getType() {
        return ParticleTypes.LIGHTNING_ARC;
    }
}
