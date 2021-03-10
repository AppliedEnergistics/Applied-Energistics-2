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
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VibrantFX extends SpriteTexturedParticle {

    public VibrantFX(final ClientWorld par1World, final double x, final double y, final double z, final double par8,
            final double par10, final double par12, IAnimatedSprite sprite) {
        super(par1World, x, y, z, par8, par10, par12);
        final float f = this.random.nextFloat() * 0.1F + 0.8F;
        this.rCol = f * 0.7f;
        this.gCol = f * 0.89f;
        this.bCol = f * 0.9f;
        this.pickSprite(sprite);
        this.setSize(0.04F, 0.04F);
        this.quadSize *= this.random.nextFloat() * 0.6F + 1.9F;
        this.xd = 0.0D;
        this.yd = 0.0D;
        this.zd = 0.0D;
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.lifetime = (int) (20.0D / (Math.random() * 0.8D + 0.1D));
    }

    @Override
    public IParticleRenderType getRenderType() {
        // FIXME Might be PARTICLE_SHEET_LIT
        return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public int getLightColor(final float par1) {
        // This just means full brightness
        return 15 << 20 | 15 << 4;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        // this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.quadSize *= 0.95;

        if (this.lifetime <= 0 || this.quadSize < 0.1) {
            this.remove();
        }
        this.lifetime--;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new VibrantFX(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
        }
    }

}
