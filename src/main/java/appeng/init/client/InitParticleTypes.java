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

package appeng.init.client;

import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.client.render.effects.ParticleTypes;

public final class InitParticleTypes {

    private InitParticleTypes() {
    }

    public static void init(IForgeRegistry<ParticleType<?>> registry) {
        registry.register(ParticleTypes.CHARGED_ORE);
        registry.register(ParticleTypes.CRAFTING);
        registry.register(ParticleTypes.ENERGY);
        registry.register(ParticleTypes.LIGHTNING_ARC);
        registry.register(ParticleTypes.LIGHTNING);
        registry.register(ParticleTypes.MATTER_CANNON);
        registry.register(ParticleTypes.VIBRANT);
    }

}
