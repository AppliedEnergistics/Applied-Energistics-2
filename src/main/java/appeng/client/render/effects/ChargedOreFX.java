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

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.RedstoneParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChargedOreFX extends RedstoneParticle {

    private static final RedstoneParticleData PARTICLE_DATA = new RedstoneParticleData(0.21f, 0.61f, 1.0f, 1.0f);

    private ChargedOreFX(ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
            IAnimatedSprite spriteSet) {
        super(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, PARTICLE_DATA, spriteSet);
    }

    @Override
    public int getBrightnessForRender(final float par1) {
        int j1 = super.getBrightnessForRender(par1);
        j1 = Math.max(j1 >> 20, j1 >> 4);
        j1 += 3;
        if (j1 > 15) {
            j1 = 15;
        }
        return j1 << 20 | j1 << 4;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite p_i50477_1_) {
            this.spriteSet = p_i50477_1_;
        }

        @Override
        public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z,
                                     double xSpeed, double ySpeed, double zSpeed) {
            return new ChargedOreFX(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}
