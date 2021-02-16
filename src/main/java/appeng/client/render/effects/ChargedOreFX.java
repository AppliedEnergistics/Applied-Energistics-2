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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.util.sat4j.core.Vec;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.RedDustParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class ChargedOreFX extends RedDustParticle {

    private static final DustParticleEffect PARTICLE_DATA = new DustParticleEffect(new Vec3f(0.21f, 0.61f, 1.0f), 1.0f);

    private ChargedOreFX(ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
            SpriteProvider spriteSet) {
        super(world, x, y, z, xSpeed, ySpeed, zSpeed, PARTICLE_DATA, spriteSet);
    }

    @Override
    public int getBrightness(final float par1) {
        int j1 = super.getBrightness(par1);
        j1 = Math.max(j1 >> 20, j1 >> 4);
        j1 += 3;
        if (j1 > 15) {
            j1 = 15;
        }
        return j1 << 20 | j1 << 4;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteSet;

        public Factory(SpriteProvider p_i50477_1_) {
            this.spriteSet = p_i50477_1_;
        }

        @Override
        public Particle createParticle(DefaultParticleType effect, ClientWorld world, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new ChargedOreFX(world, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}
