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

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Contains the target point of the lightning arc (the source point is infered from the particle starting position).
 */
public class LightningArcParticleData implements IParticleData {

    public final Vector3d target;

    public LightningArcParticleData(Vector3d target) {
        this.target = target;
    }

    public static final IDeserializer<LightningArcParticleData> DESERIALIZER = new IDeserializer<LightningArcParticleData>() {
        @Override
        public LightningArcParticleData deserialize(ParticleType<LightningArcParticleData> particleTypeIn,
                StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float x = reader.readFloat();
            reader.expect(' ');
            float y = reader.readFloat();
            reader.expect(' ');
            float z = reader.readFloat();
            return new LightningArcParticleData(new Vector3d(x, y, z));
        }

        @Override
        public LightningArcParticleData read(ParticleType<LightningArcParticleData> particleTypeIn,
                PacketBuffer buffer) {
            float x = buffer.readFloat();
            float y = buffer.readFloat();
            float z = buffer.readFloat();
            return new LightningArcParticleData(new Vector3d(x, y, z));
        }
    };

    @Override
    public ParticleType<?> getType() {
        return ParticleTypes.LIGHTNING_ARC;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeFloat((float) target.x);
        buffer.writeFloat((float) target.y);
        buffer.writeFloat((float) target.z);
    }

    @Override
    public String getParameters() {
        return String.format(Locale.ROOT, "%.2f %.2f %.2f", target.x, target.y, target.z);
    }

}
