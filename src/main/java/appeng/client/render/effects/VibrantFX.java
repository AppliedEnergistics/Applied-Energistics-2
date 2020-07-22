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
        final float f = this.rand.nextFloat() * 0.1F + 0.8F;
        this.particleRed = f * 0.7f;
        this.particleGreen = f * 0.89f;
        this.particleBlue = f * 0.9f;
        this.selectSpriteRandomly(sprite);
        this.setSize(0.04F, 0.04F);
        this.particleScale *= this.rand.nextFloat() * 0.6F + 1.9F;
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.maxAge = (int) (20.0D / (Math.random() * 0.8D + 0.1D));
    }

    @Override
    public IParticleRenderType getRenderType() {
        // FIXME Might be PARTICLE_SHEET_LIT
        return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public int getBrightnessForRender(final float par1) {
        // This just means full brightness
        return 15 << 20 | 15 << 4;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        // this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.particleScale *= 0.95;

        if (this.maxAge <= 0 || this.particleScale < 0.1) {
            this.setExpired();
        }
        this.maxAge--;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new VibrantFX(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
        }
    }

}
