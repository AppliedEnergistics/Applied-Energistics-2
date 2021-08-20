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

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

import appeng.client.render.effects.ChargedOreFX;
import appeng.client.render.effects.CraftingFx;
import appeng.client.render.effects.EnergyFx;
import appeng.client.render.effects.LightningArcFX;
import appeng.client.render.effects.LightningFX;
import appeng.client.render.effects.MatterCannonFX;
import appeng.client.render.effects.ParticleTypes;
import appeng.client.render.effects.VibrantFX;

public final class InitParticleFactories {

    private InitParticleFactories() {
    }

    public static void init() {
        var particles = ParticleFactoryRegistry.getInstance();
        particles.register(ParticleTypes.CHARGED_ORE, ChargedOreFX.Factory::new);
        particles.register(ParticleTypes.CRAFTING, CraftingFx.Factory::new);
        particles.register(ParticleTypes.ENERGY, EnergyFx.Factory::new);
        particles.register(ParticleTypes.LIGHTNING_ARC, LightningArcFX.Factory::new);
        particles.register(ParticleTypes.LIGHTNING, LightningFX.Factory::new);
        particles.register(ParticleTypes.MATTER_CANNON, MatterCannonFX.Factory::new);
        particles.register(ParticleTypes.VIBRANT, VibrantFX.Factory::new);
    }

}
