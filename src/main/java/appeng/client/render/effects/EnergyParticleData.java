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

import java.util.Locale;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

import appeng.api.util.AEPartLocation;

import net.minecraft.core.particles.ParticleOptions.Deserializer;

public class EnergyParticleData implements ParticleOptions {

    public static final EnergyParticleData FOR_BLOCK = new EnergyParticleData(false, AEPartLocation.INTERNAL);

    public final boolean forItem;

    public final AEPartLocation direction;

    public EnergyParticleData(boolean forItem, AEPartLocation direction) {
        this.forItem = forItem;
        this.direction = direction;
    }

    public static final Deserializer<EnergyParticleData> DESERIALIZER = new Deserializer<EnergyParticleData>() {
        @Override
        public EnergyParticleData fromCommand(ParticleType<EnergyParticleData> particleTypeIn, StringReader reader)
                throws CommandSyntaxException {
            reader.expect(' ');
            boolean forItem = reader.readBoolean();
            reader.expect(' ');
            AEPartLocation direction = AEPartLocation.valueOf(reader.readString().toUpperCase(Locale.ROOT));
            return new EnergyParticleData(forItem, direction);
        }

        @Override
        public EnergyParticleData fromNetwork(ParticleType<EnergyParticleData> particleTypeIn, FriendlyByteBuf buffer) {
            boolean forItem = buffer.readBoolean();
            AEPartLocation direction = AEPartLocation.values()[buffer.readByte()];
            return new EnergyParticleData(forItem, direction);
        }
    };

    @Override
    public ParticleType<?> getType() {
        return ParticleTypes.ENERGY;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeBoolean(forItem);
        buffer.writeByte((byte) direction.ordinal());
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %s", forItem ? "true" : "false",
                direction.name().toLowerCase(Locale.ROOT));
    }

}
