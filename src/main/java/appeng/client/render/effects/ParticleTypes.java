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

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;

public final class ParticleTypes {

    private ParticleTypes() {
    }

    public static final BasicParticleType CHARGED_ORE = FabricParticleTypes.simple(false);
    public static final BasicParticleType CRAFTING = FabricParticleTypes.simple(false);
    public static final ParticleType<EnergyParticleData> ENERGY = FabricParticleTypes.complex(false,
            EnergyParticleData.DESERIALIZER);
    public static final ParticleType<LightningArcParticleData> LIGHTNING_ARC = FabricParticleTypes.complex(false,
            LightningArcParticleData.DESERIALIZER);
    public static final BasicParticleType LIGHTNING = FabricParticleTypes.simple(false);
    public static final BasicParticleType MATTER_CANNON = FabricParticleTypes.simple(false);
    public static final BasicParticleType VIBRANT = FabricParticleTypes.simple(false);

    public static void registerClient() {
    }

}
