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

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;

import appeng.client.render.effects.ParticleTypes;
import appeng.core.AppEng;

public final class InitParticleTypes {

    private InitParticleTypes() {
    }

    public static void init(Registry<ParticleType<?>> registry) {
        register(registry, ParticleTypes.CHARGED_ORE, "charged_ore_fx");
        register(registry, ParticleTypes.CRAFTING, "crafting_fx");
        register(registry, ParticleTypes.ENERGY, "energy_fx");
        register(registry, ParticleTypes.LIGHTNING_ARC, "lightning_arc_fx");
        register(registry, ParticleTypes.LIGHTNING, "lightning_fx");
        register(registry, ParticleTypes.MATTER_CANNON, "matter_cannon_fx");
        register(registry, ParticleTypes.VIBRANT, "vibrant_fx");
    }

    private static void register(Registry<ParticleType<?>> registry, ParticleType<?> type, String name) {
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId(name), type);
    }

}
