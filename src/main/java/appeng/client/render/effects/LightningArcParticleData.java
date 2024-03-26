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

package appeng.client.render.effects;

import java.util.Locale;

import org.joml.Vector3f;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleOptions.Deserializer;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Contains the target point of the lightning arc (the source point is inferred from the particle starting position).
 */
public record LightningArcParticleData(Vector3f target) implements ParticleOptions {
    public static final StreamCodec<ByteBuf, LightningArcParticleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VECTOR3F,
            d -> d.target,
            LightningArcParticleData::new);

    public static final Deserializer<LightningArcParticleData> DESERIALIZER = (particleTypeIn, reader, provider) -> {
        reader.expect(' ');
        float x = reader.readFloat();
        reader.expect(' ');
        float y = reader.readFloat();
        reader.expect(' ');
        float z = reader.readFloat();
        return new LightningArcParticleData(new Vector3f(x, y, z));
    };

    @Override
    public ParticleType<?> getType() {
        return ParticleTypes.LIGHTNING_ARC;
    }

    @Override
    public String writeToString(HolderLookup.Provider registries) {
        return String.format(Locale.ROOT, "%.2f %.2f %.2f", target.x, target.y, target.z);
    }

}
