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
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

@Environment(EnvType.CLIENT)
public class VibrantFX extends SpriteBillboardParticle {

    public VibrantFX(ClientWorld world, final double x, final double y, final double z, final double par8,
            final double par10, final double par12, SpriteProvider sprite) {
        super(world, x, y, z, par8, par10, par12);
        final float f = this.random.nextFloat() * 0.1F + 0.8F;
        this.colorRed = f * 0.7f;
        this.colorGreen = f * 0.89f;
        this.colorBlue = f * 0.9f;
        this.setSprite(sprite);
        this.setBoundingBoxSpacing(0.04F, 0.04F);
        this.scale *= this.random.nextFloat() * 0.6F + 1.9F;
        this.velocityX = 0.0D;
        this.velocityY = 0.0D;
        this.velocityZ = 0.0D;
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        this.maxAge = (int) (20.0D / (Math.random() * 0.8D + 0.1D));
    }

    @Override
    public ParticleTextureSheet getType() {
        // FIXME Might be PARTICLE_SHEET_LIT
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public int getBrightness(final float par1) {
        // This just means full brightness
        return 15 << 20 | 15 << 4;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        // this.moveEntity(this.velocityX, this.velocityY, this.velocityZ);
        this.scale *= 0.95;

        if (this.maxAge <= 0 || this.scale < 0.1) {
            this.markDead();
        }
        this.maxAge--;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteSet;

        public Factory(SpriteProvider spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(DefaultParticleType effect, ClientWorld world, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new VibrantFX(world, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
        }
    }

}
