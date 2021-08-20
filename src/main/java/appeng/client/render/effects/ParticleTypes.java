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

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

public final class ParticleTypes {

    private ParticleTypes() {
    }

    public static final SimpleParticleType CRAFTING = FabricParticleTypes.simple();
    public static final ParticleType<EnergyParticleData> ENERGY = new ParticleType<EnergyParticleData>(false,
            EnergyParticleData.DESERIALIZER) {
        @Override
        public Codec<EnergyParticleData> codec() {
            return null;
        }
    };
    public static final ParticleType<LightningArcParticleData> LIGHTNING_ARC = new ParticleType<LightningArcParticleData>(
            false, LightningArcParticleData.DESERIALIZER) {
        @Override
        public Codec<LightningArcParticleData> codec() {
            return null;
        }
    };
    public static final SimpleParticleType LIGHTNING = FabricParticleTypes.simple();
    public static final SimpleParticleType MATTER_CANNON = FabricParticleTypes.simple();
    public static final SimpleParticleType VIBRANT = FabricParticleTypes.simple();

}
